package com.doraemon.pocket.event;

import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import com.doraemon.pocket.portal.LinkedPortalManager;
import com.doraemon.pocket.registry.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class AnywhereDoorTickHandler {
	private AnywhereDoorTickHandler() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerManager().getPlayerList().forEach(AnywhereDoorTickHandler::tickPlayer));
	}

	private static void tickPlayer(ServerPlayerEntity player) {
		if (player.isSpectator() || player.hasVehicle()) {
			return;
		}

		ServerWorld world = player.getServerWorld();
		BlockPos base = player.getBlockPos();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos candidate = base.add(x, y, z);
					if (tryTeleportAt(world, player, candidate)) {
						return;
					}
				}
			}
		}
	}

	private static boolean tryTeleportAt(ServerWorld world, ServerPlayerEntity player, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (!state.isOf(ModBlocks.ANYWHERE_DOOR_PORTAL)) {
			return false;
		}

		BlockPos root = state.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
		BlockState rootState = world.getBlockState(root);
		if (!rootState.isOf(ModBlocks.ANYWHERE_DOOR_PORTAL) || !rootState.get(DoorBlock.OPEN)) {
			return false;
		}

		Box trigger = new Box(
				root.getX() + 0.08D,
				root.getY(),
				root.getZ() + 0.08D,
				root.getX() + 0.92D,
				root.getY() + 2.0D,
				root.getZ() + 0.92D
		);
		if (!player.getBoundingBox().intersects(trigger)) {
			return false;
		}

		if (world.getBlockEntity(root) instanceof LinkedPortalBlockEntity portal) {
			LinkedPortalManager.teleportEntity(world, player, portal);
			return true;
		}
		return false;
	}
}
