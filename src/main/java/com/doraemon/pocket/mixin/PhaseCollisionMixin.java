package com.doraemon.pocket.mixin;

import com.doraemon.pocket.util.PhaseBlockRules;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class PhaseCollisionMixin {
	@Shadow
	protected abstract BlockState asBlockState();

	@Inject(
			method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
			at = @At("HEAD"),
			cancellable = true
	)
	private void doraemonPocket$removePhaseCollision(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (!(context instanceof EntityShapeContext entityShapeContext)) {
			return;
		}

		Entity entity = entityShapeContext.getEntity();
		if (!(entity instanceof LivingEntity livingEntity)) {
			return;
		}

		if (PhaseBlockRules.canIgnoreCollision(livingEntity, asBlockState(), pos)) {
			cir.setReturnValue(VoxelShapes.empty());
		}
	}
}
