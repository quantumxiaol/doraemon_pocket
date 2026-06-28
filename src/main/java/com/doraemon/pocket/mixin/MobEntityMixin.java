package com.doraemon.pocket.mixin;

import com.doraemon.pocket.util.GadgetMobRules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
	@Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
	private void doraemonPocket$gadgetRejectsTarget(LivingEntity target, CallbackInfo ci) {
		if (target instanceof PlayerEntity player && GadgetMobRules.shouldRejectMobTarget(player)) {
			ci.cancel();
		}
	}
}
