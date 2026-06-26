package com.doraemon.pocket.client.model;

import com.doraemon.pocket.DoraemonPocket;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class DodgeCloakModel extends BipedEntityModel<LivingEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(DoraemonPocket.id("dodge_cloak"), "main");
	public static final Identifier TEXTURE = DoraemonPocket.id("textures/entity/dodge_cloak.png");

	private DodgeCloakModel(ModelPart root) {
		super(root);
	}

	public static DodgeCloakModel create(ModelPart root) {
		return new DodgeCloakModel(root);
	}

	public static void registerLayer() {
		EntityModelLayerRegistry.registerModelLayer(LAYER, DodgeCloakModel::getTexturedModelData);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		ModelPartData body = root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.NONE);

		body.addChild(
				"cloak",
				ModelPartBuilder.create()
						.uv(0, 0).cuboid(-5.0F, 0.0F, 2.35F, 10.0F, 15.0F, 1.0F, new Dilation(-0.15F)),
				ModelTransform.NONE
		);

		return TexturedModelData.of(modelData, 32, 32);
	}
}
