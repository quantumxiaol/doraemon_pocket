package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.screen.FourDimensionalPocketScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public final class ModScreenHandlers {
	public static final ScreenHandlerType<FourDimensionalPocketScreenHandler> FOUR_DIMENSIONAL_POCKET = Registry.register(
			Registries.SCREEN_HANDLER,
			DoraemonPocket.id("four_dimensional_pocket"),
			new ScreenHandlerType<>(FourDimensionalPocketScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
	);

	private ModScreenHandlers() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket screen handlers.");
	}
}
