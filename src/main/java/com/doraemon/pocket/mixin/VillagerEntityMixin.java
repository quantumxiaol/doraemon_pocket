package com.doraemon.pocket.mixin;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {
	@Inject(method = "prepareOffersFor", at = @At("TAIL"))
	private void doraemonPocket$translationGummyLowersPrices(PlayerEntity player, CallbackInfo ci) {
		if (!player.hasStatusEffect(ModStatusEffects.UNIVERSAL_UNDERSTANDING)) {
			return;
		}

		VillagerEntity villager = (VillagerEntity) (Object) this;
		for (TradeOffer offer : villager.getOffers()) {
			offer.setSpecialPrice(-999);
		}
	}
}
