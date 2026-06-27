package com.doraemon.pocket.item;

import java.util.ArrayList;
import java.util.List;

import com.doraemon.pocket.block.CoconutFruitBlock;
import com.doraemon.pocket.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlantModificationLiquidItem extends Item {
	private static final int MIN_COCONUTS = 3;
	private static final int MAX_COCONUTS = 6;
	private static final int COOLDOWN_TICKS = 12;

	public PlantModificationLiquidItem(Settings settings) {
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

		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		if (state.isIn(BlockTags.LOGS)) {
			int grown = growCoconuts(serverWorld, pos, world.getRandom());
			if (grown <= 0) {
				player.sendMessage(Text.translatable("message.doraemon_pocket.plant_modification_liquid.no_coconut_space"), true);
				return ActionResult.FAIL;
			}

			player.sendMessage(Text.translatable("message.doraemon_pocket.plant_modification_liquid.coconuts", grown), true);
			world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 0.9F, 1.25F);
			player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
			consume(context.getStack(), player);
			return ActionResult.SUCCESS;
		}

		player.sendMessage(Text.translatable("message.doraemon_pocket.plant_modification_liquid.need_plant"), true);
		return ActionResult.FAIL;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.plant_modification_liquid.tooltip").formatted(Formatting.GRAY));
	}

	private static int growCoconuts(ServerWorld world, BlockPos origin, Random random) {
		List<CoconutPlacement> candidates = new ArrayList<>();
		for (int y = -2; y <= 5; y++) {
			BlockPos trunk = origin.up(y);
			if (!world.getBlockState(trunk).isIn(BlockTags.LOGS)) {
				continue;
			}

			for (Direction direction : Direction.Type.HORIZONTAL) {
				BlockPos fruit = trunk.offset(direction);
				Direction supportDirection = direction.getOpposite();
				if (canPlaceCoconut(world, fruit, supportDirection)) {
					candidates.add(new CoconutPlacement(fruit, supportDirection));
				}
			}
		}

		int target = MIN_COCONUTS + random.nextInt(MAX_COCONUTS - MIN_COCONUTS + 1);
		int placed = 0;
		while (!candidates.isEmpty() && placed < target) {
			CoconutPlacement placement = candidates.remove(random.nextInt(candidates.size()));
			if (!canPlaceCoconut(world, placement.pos(), placement.facing())) {
				continue;
			}

			world.setBlockState(
					placement.pos(),
					ModBlocks.COCONUT_FRUIT.getDefaultState().with(CoconutFruitBlock.FACING, placement.facing()),
					Block.NOTIFY_ALL
			);
			placed++;
		}
		return placed;
	}

	private static boolean canPlaceCoconut(ServerWorld world, BlockPos pos, Direction facing) {
		BlockState target = world.getBlockState(pos);
		if (!target.isAir() && !target.getCollisionShape(world, pos).isEmpty()) {
			return false;
		}
		return world.getBlockState(pos.offset(facing)).isIn(BlockTags.LOGS);
	}

	private static void consume(ItemStack stack, PlayerEntity player) {
		if (player.isCreative()) {
			return;
		}

		stack.decrement(1);
		ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
		if (!player.getInventory().insertStack(bottle)) {
			player.dropItem(bottle, false);
		}
	}

	private record CoconutPlacement(BlockPos pos, Direction facing) {
	}
}
