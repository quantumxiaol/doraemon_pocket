package com.doraemon.pocket.block;

import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import com.doraemon.pocket.portal.LinkedPortalManager;
import com.doraemon.pocket.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AnywhereDoorPortalBlock extends DoorBlock implements BlockEntityProvider {
	private static final VoxelShape CLICK_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

	public AnywhereDoorPortalBlock(Settings settings) {
		super(settings, BlockSetType.OAK);
		setDefaultState(getStateManager().getDefaultState()
				.with(FACING, Direction.NORTH)
				.with(OPEN, false)
				.with(HINGE, DoorHinge.LEFT)
				.with(POWERED, false)
				.with(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return CLICK_SHAPE;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof LinkedPortalBlockEntity portal)) {
			return ActionResult.PASS;
		}

		if (player.isSneaking()) {
			if (!world.isClient() && world instanceof ServerWorld serverWorld) {
				if (!player.isCreative()) {
					player.giveItemStack(new ItemStack(ModItems.ANYWHERE_DOOR));
				}
				LinkedPortalManager.removeLinkedPortal(serverWorld, portal);
				world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.55F, 1.4F);
			}
			return ActionResult.success(world.isClient());
		}

		if (!world.isClient() && world instanceof ServerWorld serverWorld) {
			BlockPos root = portal.getRootPos();
			BlockState rootState = world.getBlockState(root);
			if (rootState.isOf(this)) {
				boolean open = !rootState.get(OPEN);
				setOpen(player, world, rootState, root, open);
				syncPartnerOpen(serverWorld, portal, player, open);
			}
		}
		return ActionResult.success(world.isClient());
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!state.get(OPEN)) {
			return;
		}
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

	private void syncPartnerOpen(ServerWorld world, LinkedPortalBlockEntity portal, PlayerEntity player, boolean open) {
		RegistryKey<World> partnerKey = portal.getPartnerWorldKey();
		if (partnerKey == null || world.getServer() == null) {
			return;
		}

		ServerWorld partnerWorld = world.getServer().getWorld(partnerKey);
		if (partnerWorld == null) {
			return;
		}

		BlockPos partnerRoot = portal.getPartnerRoot();
		BlockState partnerState = partnerWorld.getBlockState(partnerRoot);
		if (partnerState.isOf(this) && partnerState.get(OPEN) != open) {
			setOpen(player, partnerWorld, partnerState, partnerRoot, open);
		}
	}
}
