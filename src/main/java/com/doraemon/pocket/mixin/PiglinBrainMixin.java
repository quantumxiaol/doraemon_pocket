package com.doraemon.pocket.mixin;

import com.doraemon.pocket.registry.ModStatusEffects;
import com.doraemon.pocket.item.DevilsPassportItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBrain.class)
public abstract class PiglinBrainMixin {
	@Inject(method = "wearsGoldArmor", at = @At("HEAD"), cancellable = true)
	private static void doraemonPocket$translationGummyCountsAsGoldArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (entity.hasStatusEffect(ModStatusEffects.UNIVERSAL_UNDERSTANDING) || entity instanceof PlayerEntity player && DevilsPassportItem.isActive(player)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "onGuardedBlockInteracted", at = @At("HEAD"), cancellable = true)
	private static void doraemonPocket$devilsPassportCancelsGuardedBlockAnger(PlayerEntity player, boolean blockOpen, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
		if (DevilsPassportItem.isActive(player)) {
			ci.cancel();
		}
	}
}
