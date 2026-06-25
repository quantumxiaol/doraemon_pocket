package com.doraemon.pocket.client.render;

import com.doraemon.pocket.client.model.BambooCopterModel;
import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;

public final class BambooCopterArmorRenderer {
	private BambooCopterArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
			if (slot != EquipmentSlot.HEAD) {
				return;
			}

			BambooCopterModel model = BambooCopterModel.create(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(BambooCopterModel.LAYER));
			contextModel.copyBipedStateTo(model);
			model.setVisible(false);
			model.hat.visible = true;
			ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, model, BambooCopterModel.TEXTURE);
		}, ModItems.BAMBOO_COPTER);
	}
}
