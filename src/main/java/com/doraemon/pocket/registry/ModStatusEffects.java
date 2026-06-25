package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.effect.EnvironmentalAdaptationStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModStatusEffects {
	public static final StatusEffect ENVIRONMENTAL_ADAPTATION = Registry.register(
			Registries.STATUS_EFFECT,
			DoraemonPocket.id("environmental_adaptation"),
			new EnvironmentalAdaptationStatusEffect()
	);

	private ModStatusEffects() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket status effects.");
	}
}
