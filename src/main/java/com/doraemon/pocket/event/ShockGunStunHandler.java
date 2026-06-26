package com.doraemon.pocket.event;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;

public final class ShockGunStunHandler {
	private ShockGunStunHandler() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			wakeIfStunned(entity);
			return true;
		});
	}

	public static boolean wakeIfStunned(LivingEntity entity) {
		if (!entity.hasStatusEffect(ModStatusEffects.STUNNED)) {
			return false;
		}
		entity.removeStatusEffect(ModStatusEffects.STUNNED);
		return true;
	}
}
