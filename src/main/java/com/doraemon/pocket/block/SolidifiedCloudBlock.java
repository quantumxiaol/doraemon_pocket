package com.doraemon.pocket.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SolidifiedCloudBlock extends Block {
	private static final double BOUNCE_STRENGTH = 0.62D;

	public SolidifiedCloudBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		entity.handleFallDamage(fallDistance, 0.0F, world.getDamageSources().fall());
	}

	@Override
	public void onEntityLand(BlockView world, Entity entity) {
		if (entity.bypassesLandingEffects()) {
			super.onEntityLand(world, entity);
			return;
		}

		Vec3d velocity = entity.getVelocity();
		if (velocity.y < 0.0D) {
			entity.setVelocity(velocity.x, -velocity.y * BOUNCE_STRENGTH, velocity.z);
		}
	}
}
