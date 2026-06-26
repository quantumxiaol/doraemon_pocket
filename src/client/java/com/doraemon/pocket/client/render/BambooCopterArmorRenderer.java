package com.doraemon.pocket.client.render;

import com.doraemon.pocket.client.model.BambooCopterModel;
import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;

public final class BambooCopterArmorRenderer {
	private static BambooCopterModel model;

	private BambooCopterArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
			if (slot != EquipmentSlot.HEAD) {
				return;
			}

			BambooCopterModel bambooCopterModel = getModel();
			contextModel.copyBipedStateTo(bambooCopterModel);
			bambooCopterModel.setVisible(false);
			bambooCopterModel.hat.visible = true;
			ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, bambooCopterModel, BambooCopterModel.TEXTURE);
		}, ModItems.BAMBOO_COPTER);
	}

	private static BambooCopterModel getModel() {
		if (model == null) {
			model = BambooCopterModel.create(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(BambooCopterModel.LAYER));
		}
		return model;
	}
}
