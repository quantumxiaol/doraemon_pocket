package com.doraemon.pocket.portal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.doraemon.pocket.block.LinkedPortalBlock;
import com.doraemon.pocket.block.AnywhereDoorPortalBlock;
import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import com.doraemon.pocket.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class LinkedPortalManager {
	private static final int TELEPORT_COOLDOWN_TICKS = 20;
	private static final int MAX_COOLDOWN_ENTRIES = 512;
	private static final Set<String> REMOVING_PORTALS = new HashSet<>();
	private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();

	private LinkedPortalManager() {
	}

	public static boolean placePair(ServerWorld world, BlockPos firstRoot, Direction firstFacing, BlockPos secondRoot, Direction secondFacing, Block block, String kind, int height) {
		if (portalsOverlap(firstRoot, secondRoot, height) || !canPlacePortal(world, firstRoot, height) || !canPlacePortal(world, secondRoot, height)) {
			return false;
		}

		clearExistingPortal(world, firstRoot, height);
		clearExistingPortal(world, secondRoot, height);
		placePortal(world, firstRoot, firstFacing, secondRoot, block, kind, height);
		placePortal(world, secondRoot, secondFacing, firstRoot, block, kind, height);
		return true;
	}

	public static boolean canPlacePortal(World world, BlockPos root, int height) {
		for (int i = 0; i < height; i++) {
			if (!canReplacePortalSpot(world, root.up(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean canReplacePortalSpot(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir() || isPortalBlock(state)) {
			return true;
		}
		return state.getFluidState().isEmpty() && state.getCollisionShape(world, pos, ShapeContext.absent()).isEmpty();
	}

	public static void removeLinkedPortal(ServerWorld world, LinkedPortalBlockEntity source) {
		String sourceKey = key(world, source.getKind(), source.getRootPos());
		if (!REMOVING_PORTALS.add(sourceKey)) {
			return;
		}

		try {
			removePortalParts(world, source.getRootPos(), source.getHeight());

			if (source.getPartnerWorldKey() == null || world.getServer() == null) {
				return;
			}

			ServerWorld partnerWorld = world.getServer().getWorld(source.getPartnerWorldKey());
			if (partnerWorld == null) {
				return;
			}

			String partnerKey = key(partnerWorld, source.getKind(), source.getPartnerRoot());
			if (!REMOVING_PORTALS.add(partnerKey)) {
				return;
			}

			try {
				removePortalParts(partnerWorld, source.getPartnerRoot(), source.getHeight());
			} finally {
				REMOVING_PORTALS.remove(partnerKey);
			}
		} finally {
			REMOVING_PORTALS.remove(sourceKey);
		}
	}

	public static void removePortalPair(ServerWorld world, BlockPos firstRoot, BlockPos secondRoot, int height) {
		removePortalParts(world, firstRoot, height);
		removePortalParts(world, secondRoot, height);
	}

	public static void teleportEntity(ServerWorld world, Entity entity, LinkedPortalBlockEntity portal) {
		if (!entity.isAlive() || entity.isRemoved() || entity.hasVehicle()) {
			return;
		}
		if (!world.getRegistryKey().getValue().toString().equals(portal.getPartnerWorldId())) {
			return;
		}

		long time = world.getTime();
		Long lastTeleportTime = TELEPORT_COOLDOWNS.get(entity.getUuid());
		if (lastTeleportTime != null && time - lastTeleportTime < TELEPORT_COOLDOWN_TICKS) {
			return;
		}
		if (TELEPORT_COOLDOWNS.size() > MAX_COOLDOWN_ENTRIES) {
			TELEPORT_COOLDOWNS.entrySet().removeIf(entry -> time - entry.getValue() > TELEPORT_COOLDOWN_TICKS);
		}

		Direction exitFacing = portal.getFacing().getOpposite();
		Vec3d target = getExitPosition(portal.getPartnerRoot(), exitFacing, portal.getHeight());
		TELEPORT_COOLDOWNS.put(entity.getUuid(), time);
		entity.stopRiding();

		if (entity instanceof ServerPlayerEntity player) {
			float yaw = exitFacing.getAxis() == Direction.Axis.Y ? player.getYaw() : exitFacing.asRotation();
			player.teleport(world, target.x, target.y, target.z, yaw, player.getPitch());
		} else {
			entity.requestTeleport(target.x, target.y, target.z);
		}

		Vec3d exitImpulse = new Vec3d(exitFacing.getOffsetX(), exitFacing.getOffsetY(), exitFacing.getOffsetZ()).multiply(0.35D);
		if (exitFacing == Direction.UP) {
			exitImpulse = exitImpulse.add(0.0D, 0.15D, 0.0D);
		}
		entity.setVelocity(entity.getVelocity().multiply(0.15D).add(exitImpulse));
		entity.velocityModified = true;
		world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 0.55F, 1.25F);
	}

	private static void placePortal(ServerWorld world, BlockPos root, Direction facing, BlockPos partnerRoot, Block block, String kind, int height) {
		for (int i = 0; i < height; i++) {
			BlockPos partPos = root.up(i);
			BlockState state = createPortalState(block, facing, i == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
			world.setBlockState(partPos, state, Block.NOTIFY_ALL);

			if (world.getBlockEntity(partPos) instanceof LinkedPortalBlockEntity portal) {
				portal.configure(kind, root, world.getRegistryKey(), partnerRoot, facing, height);
			}
		}
	}

	private static BlockState createPortalState(Block block, Direction facing, DoubleBlockHalf half) {
		if (block instanceof AnywhereDoorPortalBlock) {
			Direction horizontalFacing = facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : facing;
			return block.getDefaultState()
					.with(DoorBlock.FACING, horizontalFacing)
					.with(DoorBlock.OPEN, false)
					.with(DoorBlock.HINGE, DoorHinge.LEFT)
					.with(DoorBlock.POWERED, false)
					.with(DoorBlock.HALF, half);
		}

		return block.getDefaultState()
				.with(LinkedPortalBlock.FACING, facing)
				.with(LinkedPortalBlock.HALF, half);
	}

	private static void clearExistingPortal(ServerWorld world, BlockPos root, int height) {
		for (int i = 0; i < height; i++) {
			BlockPos pos = root.up(i);
			if (world.getBlockEntity(pos) instanceof LinkedPortalBlockEntity portal) {
				removeLinkedPortal(world, portal);
			}
		}
	}

	private static void removePortalParts(ServerWorld world, BlockPos root, int height) {
		for (int i = 0; i < height; i++) {
			BlockPos pos = root.up(i);
			if (isPortalBlock(world.getBlockState(pos))) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
			}
		}
	}

	private static Vec3d getExitPosition(BlockPos root, Direction facing, int height) {
		double x = root.getX() + 0.5D + facing.getOffsetX() * 0.85D;
		double y = root.getY() + (height == LinkedPortalBlockEntity.ANYWHERE_DOOR_HEIGHT ? 0.05D : 0.5D) + facing.getOffsetY() * 0.85D;
		double z = root.getZ() + 0.5D + facing.getOffsetZ() * 0.85D;
		return new Vec3d(x, y, z);
	}

	private static boolean isPortalBlock(BlockState state) {
		return state.isOf(ModBlocks.ANYWHERE_DOOR_PORTAL) || state.isOf(ModBlocks.PASS_LOOP_PORTAL);
	}

	private static boolean portalsOverlap(BlockPos firstRoot, BlockPos secondRoot, int height) {
		for (int firstPart = 0; firstPart < height; firstPart++) {
			for (int secondPart = 0; secondPart < height; secondPart++) {
				if (firstRoot.up(firstPart).equals(secondRoot.up(secondPart))) {
					return true;
				}
			}
		}
		return false;
	}

	private static String key(ServerWorld world, String kind, BlockPos root) {
		return world.getRegistryKey().getValue() + ":" + kind + ":" + root.asLong();
	}
}
