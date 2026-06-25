package com.doraemon.pocket.client.render;

import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

public final class DodgeCloakArmorRenderer {
	private DodgeCloakArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
			// Visual cloak model is intentionally deferred; this prevents a missing armor texture.
		}, ModItems.DODGE_CLOAK);
	}
}
