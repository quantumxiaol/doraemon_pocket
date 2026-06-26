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

public final class StoneHatArmorRenderer {
	private static final Identifier TEXTURE = DoraemonPocket.id("textures/models/armor/stone_hat_layer_1.png");
	private static ArmorEntityModel<LivingEntity> model;

	private StoneHatArmorRenderer() {
	}

	public static void register() {
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, contextModel) -> {
			if (slot != EquipmentSlot.HEAD) {
				return;
			}

			ArmorEntityModel<LivingEntity> stoneHatModel = getModel();
			contextModel.copyBipedStateTo(stoneHatModel);
			stoneHatModel.setVisible(false);
			stoneHatModel.head.visible = true;
			stoneHatModel.hat.visible = true;
			ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, stoneHatModel, TEXTURE);
		}, ModItems.STONE_HAT);
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
