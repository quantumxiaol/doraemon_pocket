package com.doraemon.pocket.util;

import com.doraemon.pocket.item.PassThroughCapItem;
import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public final class PhaseBlockRules {
	private static final double BLOCK_SCAN_EPSILON = 1.0E-7D;

	private PhaseBlockRules() {
	}

	public static boolean touchesSwimmableSolidBlock(BlockView world, Box box) {
		return scanBlocks(world, box, ScanMode.SWIMMABLE_SOLID);
	}

	public static boolean touchesForbiddenBlock(BlockView world, Box box) {
		return scanBlocks(world, box, ScanMode.FORBIDDEN);
	}

	public static boolean touchesUnswimmableSolidBlock(BlockView world, Box box) {
		return scanBlocks(world, box, ScanMode.UNSWIMMABLE_SOLID);
	}

	public static boolean touchesCapPhaseableSolidBlock(BlockView world, Box box) {
		return scanBlocks(world, box, ScanMode.CAP_PHASEABLE_SOLID);
	}

	public static boolean touchesCapUnphaseableSolidBlock(BlockView world, Box box) {
		return scanBlocks(world, box, ScanMode.CAP_UNPHASEABLE_SOLID);
	}

	public static boolean canIgnoreCollision(LivingEntity entity, BlockState state, BlockPos pos) {
		if (state.isAir() || isForbiddenBlock(state)) {
			return false;
		}

		boolean canPhase = entity.hasStatusEffect(ModStatusEffects.UNDERGROUND_SWIMMING) && isSwimmableBlock(state)
				|| PassThroughCapItem.isEquipped(entity) && isCapPhaseableBlock(state);
		if (!canPhase) {
			return false;
		}

		if (SpatialPhaseManager.isActive(entity)) {
			return true;
		}

		int feetY = MathHelper.floor(entity.getY());
		return pos.getY() >= feetY || entity.isSneaking() && pos.getY() >= feetY - 1;
	}

	public static boolean isSwimmableBlock(BlockState state) {
		if (isForbiddenBlock(state)) {
			return false;
		}

		Block block = state.getBlock();
		return state.isIn(BlockTags.BASE_STONE_OVERWORLD)
				|| state.isIn(BlockTags.BASE_STONE_NETHER)
				|| state.isIn(BlockTags.DIRT)
				|| state.isIn(BlockTags.SAND)
				|| state.isIn(BlockTags.TERRACOTTA)
				|| state.isIn(BlockTags.COAL_ORES)
				|| state.isIn(BlockTags.IRON_ORES)
				|| state.isIn(BlockTags.COPPER_ORES)
				|| state.isIn(BlockTags.GOLD_ORES)
				|| state.isIn(BlockTags.DIAMOND_ORES)
				|| state.isIn(BlockTags.REDSTONE_ORES)
				|| state.isIn(BlockTags.LAPIS_ORES)
				|| state.isIn(BlockTags.EMERALD_ORES)
				|| block == Blocks.GRAVEL
				|| block == Blocks.CLAY
				|| block == Blocks.END_STONE
				|| block == Blocks.CALCITE
				|| block == Blocks.TUFF
				|| block == Blocks.DRIPSTONE_BLOCK
				|| block == Blocks.SOUL_SAND
				|| block == Blocks.SOUL_SOIL;
	}

	public static boolean isCapPhaseableBlock(BlockState state) {
		return !isForbiddenBlock(state);
	}

	public static boolean isForbiddenBlock(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.BEDROCK
				|| block == Blocks.OBSIDIAN
				|| block == Blocks.CRYING_OBSIDIAN
				|| block == Blocks.REINFORCED_DEEPSLATE
				|| block == Blocks.BARRIER
				|| block == Blocks.END_PORTAL_FRAME
				|| block == Blocks.END_PORTAL
				|| block == Blocks.NETHER_PORTAL;
	}

	private static boolean scanBlocks(BlockView world, Box box, ScanMode mode) {
		int minX = MathHelper.floor(box.minX);
		int maxX = MathHelper.floor(box.maxX - BLOCK_SCAN_EPSILON);
		int minY = MathHelper.floor(box.minY);
		int maxY = MathHelper.floor(box.maxY - BLOCK_SCAN_EPSILON);
		int minZ = MathHelper.floor(box.minZ);
		int maxZ = MathHelper.floor(box.maxZ - BLOCK_SCAN_EPSILON);
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					mutable.set(x, y, z);
					BlockState state = world.getBlockState(mutable);
					if (state.isAir() || state.getCollisionShape(world, mutable).isEmpty()) {
						continue;
					}

					boolean forbidden = isForbiddenBlock(state);
					boolean swimmable = isSwimmableBlock(state);
					boolean capPhaseable = isCapPhaseableBlock(state);
					if (mode == ScanMode.FORBIDDEN && forbidden) {
						return true;
					}
					if (mode == ScanMode.SWIMMABLE_SOLID && swimmable) {
						return true;
					}
					if (mode == ScanMode.UNSWIMMABLE_SOLID && !swimmable) {
						return true;
					}
					if (mode == ScanMode.CAP_PHASEABLE_SOLID && capPhaseable) {
						return true;
					}
					if (mode == ScanMode.CAP_UNPHASEABLE_SOLID && !capPhaseable) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private enum ScanMode {
		SWIMMABLE_SOLID,
		FORBIDDEN,
		UNSWIMMABLE_SOLID,
		CAP_PHASEABLE_SOLID,
		CAP_UNPHASEABLE_SOLID
	}
}
