package com.doraemon.pocket.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class GourmetTableClothBlock extends Block {
	private static final int EXPERIENCE_LEVEL_COST = 1;
	private static final VoxelShape SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
	private static final List<Item> FOOD_POOL = List.of(
			Items.COOKED_BEEF,
			Items.COOKED_PORKCHOP,
			Items.COOKED_CHICKEN,
			Items.COOKED_SALMON,
			Items.GOLDEN_CARROT,
			Items.PUMPKIN_PIE,
			Items.RABBIT_STEW,
			Items.MUSHROOM_STEW,
			Items.BAKED_POTATO
	);

	public GourmetTableClothBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos floor = pos.down();
		return world.getBlockState(floor).isSideSolidFullSquare(world, floor, Direction.UP);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		return canPlaceAt(state, world, pos) ? super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos) : Blocks.AIR.getDefaultState();
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!player.isSneaking()) {
			return ActionResult.PASS;
		}

		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		if (!player.isCreative() && player.experienceLevel < EXPERIENCE_LEVEL_COST) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.gourmet_table_cloth.need_xp"), true);
			return ActionResult.SUCCESS;
		}

		if (!player.isCreative()) {
			player.addExperienceLevels(-EXPERIENCE_LEVEL_COST);
		}

		Random random = world.getRandom();
		Item food = FOOD_POOL.get(random.nextInt(FOOD_POOL.size()));
		ItemStack stack = new ItemStack(food, food == Items.RABBIT_STEW || food == Items.MUSHROOM_STEW ? 1 : 1 + random.nextInt(2));
		ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.35D, pos.getZ() + 0.5D, stack);
		itemEntity.setVelocity((random.nextDouble() - 0.5D) * 0.04D, 0.12D, (random.nextDouble() - 0.5D) * 0.04D);
		world.spawnEntity(itemEntity);
		world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.BLOCKS, 0.9F, 1.6F);
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D, 8, 0.35D, 0.05D, 0.35D, 0.02D);
		}
		return ActionResult.SUCCESS;
	}
}
