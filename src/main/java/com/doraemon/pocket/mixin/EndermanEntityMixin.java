package com.doraemon.pocket.mixin;

import com.doraemon.pocket.registry.ModStatusEffects;
import com.doraemon.pocket.util.GadgetMobRules;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
public abstract class EndermanEntityMixin {
	@Inject(method = "isPlayerStaring", at = @At("HEAD"), cancellable = true)
	private void doraemonPocket$translationGummyStopsStaringAggro(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		if (player.hasStatusEffect(ModStatusEffects.UNIVERSAL_UNDERSTANDING) || GadgetMobRules.hasDevilsPassport(player)) {
			cir.setReturnValue(false);
		}
	}
}
