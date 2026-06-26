package com.doraemon.pocket.client.render;

import com.doraemon.pocket.client.model.DodgeCloakModel;
import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;

public final class DodgeCloakArmorRenderer {
	private static DodgeCloakModel model;

	private DodgeCloakArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
			if (slot != EquipmentSlot.CHEST) {
				return;
			}

			DodgeCloakModel dodgeCloakModel = getModel();
			contextModel.copyBipedStateTo(dodgeCloakModel);
			dodgeCloakModel.setVisible(false);
			dodgeCloakModel.body.visible = true;
			ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, dodgeCloakModel, DodgeCloakModel.TEXTURE);
		}, ModItems.DODGE_CLOAK);
	}

	private static DodgeCloakModel getModel() {
		if (model == null) {
			model = DodgeCloakModel.create(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(DodgeCloakModel.LAYER));
		}
		return model;
	}
}
