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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PassLoopItem extends Item {
	private static final int MAX_SCAN_DISTANCE = 96;
	private static final int COOLDOWN_TICKS = 12;

	public PassLoopItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		if (player == null) {
			return ActionResult.PASS;
		}
		World world = context.getWorld();
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}
		if (!(world instanceof ServerWorld serverWorld)) {
			return ActionResult.PASS;
		}

		Direction side = context.getSide();
		Direction inward = side.getOpposite();
		BlockPos nearRoot = context.getBlockPos().offset(side);
		BlockPos farRoot = findCavity(serverWorld, context.getBlockPos(), inward);
		if (farRoot == null || nearRoot.equals(farRoot) || !LinkedPortalManager.canPlacePortal(serverWorld, nearRoot, LinkedPortalBlockEntity.PASS_LOOP_HEIGHT)) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.pass_loop.no_cavity"), true);
			return ActionResult.FAIL;
		}

		boolean placed = LinkedPortalManager.placePair(
				serverWorld,
				nearRoot,
				side,
				farRoot,
				inward,
				ModBlocks.PASS_LOOP_PORTAL,
				LinkedPortalBlockEntity.KIND_PASS_LOOP,
				LinkedPortalBlockEntity.PASS_LOOP_HEIGHT
		);
		if (!placed) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.pass_loop.no_cavity"), true);
			return ActionResult.FAIL;
		}

		player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		world.playSound(null, nearRoot, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.45F, 1.65F);
		world.playSound(null, farRoot, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.45F, 1.65F);
		consume(context.getStack(), player);
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.pass_loop.tooltip").formatted(Formatting.GRAY));
	}

	@Nullable
	private static BlockPos findCavity(ServerWorld world, BlockPos surfacePos, Direction inward) {
		boolean crossedSolid = false;
		for (int distance = 0; distance <= MAX_SCAN_DISTANCE; distance++) {
			BlockPos probe = surfacePos.offset(inward, distance);
			if (LinkedPortalManager.canReplacePortalSpot(world, probe)) {
				if (crossedSolid) {
					return probe;
				}
			} else {
				crossedSolid = true;
			}
		}
		return null;
	}

	private static void consume(ItemStack stack, PlayerEntity player) {
		if (!player.isCreative()) {
			stack.decrement(1);
		}
	}
}
