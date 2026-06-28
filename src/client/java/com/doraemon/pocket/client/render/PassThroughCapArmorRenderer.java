package com.doraemon.pocket.client.render;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class PassThroughCapArmorRenderer {
	private static final Identifier TEXTURE = DoraemonPocket.id("textures/models/armor/pass_through_cap_layer_1.png");
	private static ArmorEntityModel<LivingEntity> model;

	private PassThroughCapArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
			if (slot != EquipmentSlot.HEAD) {
				return;
			}

			ArmorEntityModel<LivingEntity> capModel = getModel();
			contextModel.copyBipedStateTo(capModel);
			capModel.setVisible(false);
			capModel.head.visible = true;
			capModel.hat.visible = true;
			ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, capModel, TEXTURE);
		}, ModItems.PASS_THROUGH_CAP);
	}

	private static ArmorEntityModel<LivingEntity> getModel() {
		if (model == null) {
			model = new ArmorEntityModel<>(
					MinecraftClient.getInstance().getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_OUTER_ARMOR)
			);
		}
		return model;
	}
}
