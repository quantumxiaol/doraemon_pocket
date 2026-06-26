package com.doraemon.pocket.event;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;

public final class EnvironmentalAdaptationEvents {
	private EnvironmentalAdaptationEvents() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(EnvironmentalAdaptationEvents::allowDamage);
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!entity.hasStatusEffect(ModStatusEffects.ENVIRONMENTAL_ADAPTATION)) {
			return true;
		}

		if (!isEnvironmentalDamage(source)) {
			return true;
		}

		if (source.isOf(DamageTypes.OUT_OF_WORLD)) {
			entity.addVelocity(0.0D, 0.28D, 0.0D);
			entity.velocityModified = true;
		}

		if (isFireEnvironmentDamage(source)) {
			entity.extinguish();
		}

		return false;
	}

	public static boolean isEnvironmentalDamage(DamageSource source) {
		return source.isOf(DamageTypes.DROWN)
				|| source.isOf(DamageTypes.FREEZE)
				|| isFireEnvironmentDamage(source)
				|| source.isOf(DamageTypes.OUT_OF_WORLD)
				|| source.isOf(DamageTypes.IN_WALL)
				|| source.isOf(DamageTypes.DRY_OUT)
				|| source.isOf(DamageTypes.CACTUS)
				|| source.isOf(DamageTypes.SWEET_BERRY_BUSH);
	}

	private static boolean isFireEnvironmentDamage(DamageSource source) {
		return source.isOf(DamageTypes.IN_FIRE)
				|| source.isOf(DamageTypes.ON_FIRE)
				|| source.isOf(DamageTypes.LAVA)
				|| source.isOf(DamageTypes.HOT_FLOOR);
	}
}
