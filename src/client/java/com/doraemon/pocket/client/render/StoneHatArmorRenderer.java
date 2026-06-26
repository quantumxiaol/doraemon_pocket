package com.doraemon.pocket.client.render;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;

public final class StoneHatArmorRenderer {
	private static final Identifier TEXTURE = DoraemonPocket.id("textures/models/armor/stone_hat_layer_1.png");

	private StoneHatArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
			if (slot != EquipmentSlot.HEAD) {
				return;
			}

			contextModel.setVisible(false);
			contextModel.head.visible = true;
			contextModel.hat.visible = true;
			ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, contextModel, TEXTURE);
		}, ModItems.STONE_HAT);
	}
}
