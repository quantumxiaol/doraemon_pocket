package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.effect.EnvironmentalAdaptationStatusEffect;
import com.doraemon.pocket.effect.StunnedStatusEffect;
import com.doraemon.pocket.effect.UniversalUnderstandingStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModStatusEffects {
	public static final StatusEffect ENVIRONMENTAL_ADAPTATION = Registry.register(
			Registries.STATUS_EFFECT,
			DoraemonPocket.id("environmental_adaptation"),
			new EnvironmentalAdaptationStatusEffect()
	);
	public static final StatusEffect STUNNED = Registry.register(
			Registries.STATUS_EFFECT,
			DoraemonPocket.id("stunned"),
			new StunnedStatusEffect()
	);
	public static final StatusEffect UNIVERSAL_UNDERSTANDING = Registry.register(
			Registries.STATUS_EFFECT,
			DoraemonPocket.id("universal_understanding"),
			new UniversalUnderstandingStatusEffect()
	);

	private ModStatusEffects() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket status effects.");
	}
}
