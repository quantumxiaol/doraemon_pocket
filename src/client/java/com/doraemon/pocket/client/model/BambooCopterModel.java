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

public class BambooCopterModel extends BipedEntityModel<LivingEntity> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(DoraemonPocket.id("bamboo_copter"), "main");
	public static final Identifier TEXTURE = DoraemonPocket.id("textures/entity/bamboo_copter.png");

	private BambooCopterModel(ModelPart root) {
		super(root);
	}

	public static BambooCopterModel create(ModelPart root) {
		return new BambooCopterModel(root);
	}

	public static void registerLayer() {
		EntityModelLayerRegistry.registerModelLayer(LAYER, BambooCopterModel::getTexturedModelData);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();

		root.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create(), ModelTransform.NONE);
		ModelPartData hat = root.addChild(EntityModelPartNames.HAT, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.RIGHT_ARM, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.LEFT_ARM, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create(), ModelTransform.NONE);
		root.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create(), ModelTransform.NONE);

		ModelPartData base = hat.addChild(
				"base",
				ModelPartBuilder.create()
						.uv(0, 0).cuboid(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F, Dilation.NONE)
						.uv(0, 7).cuboid(-2.5F, -1.75F, -2.5F, 5.0F, 1.0F, 5.0F, Dilation.NONE)
						.uv(17, 18).cuboid(-0.5F, -6.9F, -0.5F, 1.0F, 6.0F, 1.0F, new Dilation(-0.1F)),
				ModelTransform.pivot(0.0F, -7.5F, 0.0F)
		);
		base.addChild("anchor", ModelPartBuilder.create(), ModelTransform.NONE);

		ModelPartData fan = hat.addChild("fan", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -14.05F, 0.0F));
		ModelPartData leftFan = fan.addChild("left_fan", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.05F, 0.0F, 0.0873F, 0.0F, 0.0F));
		ModelPartData rightFan = fan.addChild("right_fan", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.05F, 0.0F, -0.0873F, 0.0F, 0.0F));

		leftFan.addChild(
				"left_fan_1",
				ModelPartBuilder.create().uv(16, 14).cuboid(-6.5F, -0.5F, -1.0F, 7.0F, 1.0F, 2.0F, new Dilation(-0.4F)),
				ModelTransform.of(-0.25F, 0.0F, 0.0F, 0.0F, -0.096F, 0.0F)
		);
		leftFan.addChild(
				"left_fan_2",
				ModelPartBuilder.create().uv(0, 16).cuboid(-6.5F, -0.5F, -1.0F, 7.0F, 1.0F, 2.0F, new Dilation(-0.405F)),
				ModelTransform.of(-0.25F, 0.0F, 0.0F, 0.0F, 0.096F, 0.0F)
		);
		rightFan.addChild(
				"right_fan_1",
				ModelPartBuilder.create().uv(15, 7).cuboid(-0.5F, -0.5F, -1.0F, 7.0F, 1.0F, 2.0F, new Dilation(-0.4F)),
				ModelTransform.of(0.25F, 0.0F, 0.0F, 0.0F, 0.096F, 0.0F)
		);
		rightFan.addChild(
				"right_fan_2",
				ModelPartBuilder.create().uv(0, 13).cuboid(-0.5F, -0.5F, -1.0F, 7.0F, 1.0F, 2.0F, new Dilation(-0.405F)),
				ModelTransform.of(0.25F, 0.0F, 0.0F, 0.0F, -0.096F, 0.0F)
		);

		return TexturedModelData.of(modelData, 64, 64);
	}
}
