package com.doraemon.pocket.mixin;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {
	@Inject(method = "wearsGoldArmor", at = @At("HEAD"), cancellable = true)
	private static void doraemonPocket$translationGummyCountsAsGoldArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (entity.hasStatusEffect(ModStatusEffects.UNIVERSAL_UNDERSTANDING)) {
			cir.setReturnValue(true);
		}
	}
}
