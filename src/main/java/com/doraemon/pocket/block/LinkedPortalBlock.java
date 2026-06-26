package com.doraemon.pocket.block;

import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import com.doraemon.pocket.portal.LinkedPortalManager;
import com.doraemon.pocket.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LinkedPortalBlock extends BlockWithEntity {
	public static final DirectionProperty FACING = Properties.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

	private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape UP_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
	private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);

	public LinkedPortalBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return switch (state.get(FACING)) {
			case DOWN -> DOWN_SHAPE;
			case UP -> UP_SHAPE;
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
		};
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof LinkedPortalBlockEntity portal)) {
			return ActionResult.PASS;
		}

		if (player.isSneaking()) {
			if (!world.isClient() && world instanceof ServerWorld serverWorld) {
				if (!player.isCreative()) {
					player.giveItemStack(recoveredStack(portal));
				}
				LinkedPortalManager.removeLinkedPortal(serverWorld, portal);
				world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.55F, 1.4F);
			}
			return ActionResult.success(world.isClient());
		}

		if (!world.isClient() && world instanceof ServerWorld serverWorld) {
			LinkedPortalManager.teleportEntity(serverWorld, player, portal);
		}
		return ActionResult.success(world.isClient());
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!world.isClient() && world instanceof ServerWorld serverWorld && world.getBlockEntity(pos) instanceof LinkedPortalBlockEntity portal) {
			LinkedPortalManager.teleportEntity(serverWorld, entity, portal);
		}
	}

	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!world.isClient() && state.getBlock() != newState.getBlock() && world instanceof ServerWorld serverWorld
				&& world.getBlockEntity(pos) instanceof LinkedPortalBlockEntity portal) {
			LinkedPortalManager.removeLinkedPortal(serverWorld, portal);
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}

	@Override
	@Nullable
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LinkedPortalBlockEntity(pos, state);
	}

	private static ItemStack recoveredStack(LinkedPortalBlockEntity portal) {
		if (LinkedPortalBlockEntity.KIND_ANYWHERE_DOOR.equals(portal.getKind())) {
			return new ItemStack(ModItems.ANYWHERE_DOOR);
		}
		return new ItemStack(ModItems.PASS_LOOP);
	}
}
