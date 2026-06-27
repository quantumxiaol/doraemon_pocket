package com.doraemon.pocket.block;

import com.doraemon.pocket.block.entity.WeatherBoxBlockEntity;
import com.doraemon.pocket.item.WeatherCardItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WeatherBoxBlock extends BlockWithEntity implements BlockEntityProvider {
	public WeatherBoxBlock(Settings settings) {
		super(settings);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new WeatherBoxBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof WeatherBoxBlockEntity weatherBox)) {
			return ActionResult.PASS;
		}

		ItemStack heldStack = player.getStackInHand(hand);
		if (world.isClient()) {
			return weatherBox.hasCard() || heldStack.getItem() instanceof WeatherCardItem ? ActionResult.SUCCESS : ActionResult.PASS;
		}

		if (weatherBox.hasCard()) {
			ejectCard(world, pos, weatherBox.removeCard());
			world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 0.65F, 1.25F);
			return ActionResult.SUCCESS;
		}

		if (!(heldStack.getItem() instanceof WeatherCardItem weatherCard) || !(world instanceof ServerWorld serverWorld)) {
			return ActionResult.PASS;
		}

		ItemStack inserted = heldStack.copy();
		inserted.setCount(1);
		weatherBox.setCard(inserted);
		if (!player.isCreative()) {
			heldStack.decrement(1);
		}

		weatherCard.applyWeather(serverWorld);
		player.sendMessage(weatherCard.getAppliedText(), true);
		world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 0.9F, weatherCard.getPitch());
		return ActionResult.SUCCESS;
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof WeatherBoxBlockEntity weatherBox && weatherBox.hasCard()) {
			ejectCard(world, pos, weatherBox.removeCard());
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	private static void ejectCard(World world, BlockPos pos, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}

		ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, stack);
		entity.setVelocity(0.0D, 0.12D, 0.0D);
		world.spawnEntity(entity);
	}
}
