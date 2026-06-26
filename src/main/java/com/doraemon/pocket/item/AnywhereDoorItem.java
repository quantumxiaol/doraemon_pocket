package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import com.doraemon.pocket.portal.LinkedPortalManager;
import com.doraemon.pocket.registry.ModBlocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AnywhereDoorItem extends Item {
	private static final String TARGET_WORLD_KEY = "AnywhereDoorTargetWorld";
	private static final String TARGET_POS_KEY = "AnywhereDoorTargetPos";
	private static final int COOLDOWN_TICKS = 20;

	public AnywhereDoorItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient()) {
			user.sendMessage(Text.translatable("message.doraemon_pocket.anywhere_door.need_block"), true);
		}
		return TypedActionResult.fail(stack);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		if (player == null) {
			return ActionResult.PASS;
		}

		ItemStack stack = context.getStack();
		World world = context.getWorld();
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}
		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.PASS;
		}

		Target target = readTarget(stack);
		if (target == null) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.anywhere_door.no_target"), true);
			return ActionResult.FAIL;
		}
		if (!serverWorld.getRegistryKey().getValue().toString().equals(target.worldId)) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.anywhere_door.same_dimension_only"), true);
			return ActionResult.FAIL;
		}

		Direction side = context.getSide();
		Direction originFacing = side.getAxis() == Direction.Axis.Y ? context.getHorizontalPlayerFacing() : side;
		BlockPos originRoot = getOriginRoot(context.getBlockPos(), side);
		BlockPos targetRoot = getTargetRoot(serverWorld, target.pos);
		if (targetRoot == null || originRoot.equals(targetRoot)) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.anywhere_door.no_space"), true);
			return ActionResult.FAIL;
		}

		boolean placed = LinkedPortalManager.placePair(
				serverWorld,
				originRoot,
				originFacing,
				targetRoot,
				originFacing.getOpposite(),
				ModBlocks.ANYWHERE_DOOR_PORTAL,
				LinkedPortalBlockEntity.KIND_ANYWHERE_DOOR,
				LinkedPortalBlockEntity.ANYWHERE_DOOR_HEIGHT
		);
		if (!placed) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.anywhere_door.no_space"), true);
			return ActionResult.FAIL;
		}

		player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		world.playSound(null, originRoot, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.8F, 1.25F);
		world.playSound(null, targetRoot, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.8F, 1.25F);
		consume(stack, player);
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.anywhere_door.tooltip").formatted(Formatting.GRAY));
	}

	public static void setTarget(ItemStack stack, ServerWorld world, BlockPos pos) {
		NbtCompound nbt = stack.getOrCreateNbt();
		nbt.putString(TARGET_WORLD_KEY, world.getRegistryKey().getValue().toString());
		nbt.putLong(TARGET_POS_KEY, pos.asLong());
	}

	@Nullable
	private static Target readTarget(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null || !nbt.contains(TARGET_WORLD_KEY) || !nbt.contains(TARGET_POS_KEY)) {
			return null;
		}

		return new Target(
				nbt.getString(TARGET_WORLD_KEY),
				BlockPos.fromLong(nbt.getLong(TARGET_POS_KEY))
		);
	}

	@Nullable
	private static BlockPos getTargetRoot(ServerWorld world, BlockPos target) {
		if (LinkedPortalManager.canPlacePortal(world, target, LinkedPortalBlockEntity.ANYWHERE_DOOR_HEIGHT)) {
			return target;
		}
		BlockPos above = target.up();
		return LinkedPortalManager.canPlacePortal(world, above, LinkedPortalBlockEntity.ANYWHERE_DOOR_HEIGHT) ? above : null;
	}

	private static BlockPos getOriginRoot(BlockPos clickedPos, Direction side) {
		return switch (side) {
			case UP -> clickedPos.up();
			case DOWN -> clickedPos.down();
			default -> clickedPos.offset(side);
		};
	}

	private static void consume(ItemStack stack, PlayerEntity player) {
		if (!player.isCreative()) {
			stack.decrement(1);
		}
	}

	private record Target(String worldId, BlockPos pos) {
	}
}
