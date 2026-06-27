package com.doraemon.pocket.block;

import com.doraemon.pocket.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class CoconutFruitBlock extends HorizontalFacingBlock {
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(4.0D, 3.0D, 1.0D, 12.0D, 12.0D, 9.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(4.0D, 3.0D, 7.0D, 12.0D, 12.0D, 15.0D);
	private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(1.0D, 3.0D, 4.0D, 9.0D, 12.0D, 12.0D);
	private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(7.0D, 3.0D, 4.0D, 15.0D, 12.0D, 12.0D);

	public CoconutFruitBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(FACING)) {
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
			default -> SOUTH_SHAPE;
		};
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return world.getBlockState(pos.offset(state.get(FACING))).isIn(BlockTags.LOGS);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		return canPlaceAt(state, world, pos) ? super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos) : Blocks.AIR.getDefaultState();
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		ItemStack coconut = new ItemStack(ModItems.COCONUT);
		if (!player.getInventory().insertStack(coconut)) {
			player.dropItem(coconut, false);
		}
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
		world.playSound(null, pos, SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, SoundCategory.BLOCKS, 0.8F, 0.9F + world.getRandom().nextFloat() * 0.25F);
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.syncWorldEvent(2005, pos, 0);
		}
		return ActionResult.SUCCESS;
	}
}
