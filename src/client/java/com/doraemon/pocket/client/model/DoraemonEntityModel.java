package com.doraemon.pocket.client.model;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.entity.DoraemonEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class DoraemonEntityModel<T extends DoraemonEntity> extends SinglePartEntityModel<T> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(DoraemonPocket.id("doraemon"), "main");
	public static final Identifier TEXTURE = DoraemonPocket.id("textures/entity/doraemon.png");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart rotor;

	public DoraemonEntityModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftArm = this.root.getChild("left_arm");
		this.rightArm = this.root.getChild("right_arm");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
		this.rotor = this.root.getChild("rotor");
	}

	public static void registerLayer() {
		EntityModelLayerRegistry.registerModelLayer(LAYER, DoraemonEntityModel::getTexturedModelData);
	}

	@Override
	public ModelPart getPart() {
		return root;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		head.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE * 0.45F;
		head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE * 0.35F;
		rotor.yaw = animationProgress * (entity.isOnGround() ? 0.55F : 1.55F);

		float swing = Math.min(0.85F, limbDistance);
		rightArm.pitch = MathHelper.cos(limbAngle * 0.6662F + MathHelper.PI) * 0.45F * swing - 0.45F;
		leftArm.pitch = MathHelper.cos(limbAngle * 0.6662F) * 0.45F * swing - 0.45F;
		rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 0.35F * swing;
		leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + MathHelper.PI) * 0.35F * swing;
		root.pivotY = 24.0F + MathHelper.sin(animationProgress * 0.12F) * 0.7F;
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot().addChild("root", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData head = root.addChild(
				"head",
				ModelPartBuilder.create()
						.uv(0, 0).cuboid(-6.0F, -24.0F, -6.0F, 12.0F, 11.0F, 12.0F, Dilation.NONE)
						.uv(0, 32).cuboid(-5.2F, -21.9F, -6.35F, 10.4F, 7.2F, 0.35F, Dilation.NONE)
						.uv(0, 32).cuboid(-3.8F, -23.2F, -6.65F, 3.4F, 4.1F, 0.35F, Dilation.NONE)
						.uv(0, 32).cuboid(0.4F, -23.2F, -6.65F, 3.4F, 4.1F, 0.35F, Dilation.NONE)
						.uv(56, 20).cuboid(-1.4F, -21.8F, -6.95F, 1.0F, 1.4F, 0.35F, Dilation.NONE)
						.uv(56, 20).cuboid(0.8F, -21.8F, -6.95F, 1.0F, 1.4F, 0.35F, Dilation.NONE)
						.uv(56, 0).cuboid(-1.35F, -20.4F, -7.05F, 2.7F, 2.4F, 0.5F, Dilation.NONE)
						.uv(56, 20).cuboid(-0.12F, -18.2F, -7.0F, 0.24F, 3.0F, 0.35F, Dilation.NONE)
						.uv(56, 20).cuboid(-5.2F, -19.2F, -7.0F, 4.0F, 0.18F, 0.25F, Dilation.NONE)
						.uv(56, 20).cuboid(-5.0F, -18.1F, -7.0F, 4.0F, 0.18F, 0.25F, Dilation.NONE)
						.uv(56, 20).cuboid(-4.6F, -17.0F, -7.0F, 3.6F, 0.18F, 0.25F, Dilation.NONE)
						.uv(56, 20).cuboid(1.2F, -19.2F, -7.0F, 4.0F, 0.18F, 0.25F, Dilation.NONE)
						.uv(56, 20).cuboid(1.0F, -18.1F, -7.0F, 4.0F, 0.18F, 0.25F, Dilation.NONE)
						.uv(56, 20).cuboid(1.0F, -17.0F, -7.0F, 3.6F, 0.18F, 0.25F, Dilation.NONE)
						.uv(56, 0).cuboid(-4.2F, -15.8F, -6.8F, 8.4F, 2.6F, 0.35F, Dilation.NONE)
						.uv(84, 20).cuboid(-2.2F, -13.9F, -6.95F, 4.4F, 1.2F, 0.35F, Dilation.NONE),
				ModelTransform.NONE
		);
		head.addChild(
				"nose_highlight",
				ModelPartBuilder.create().uv(0, 32).cuboid(0.35F, -20.0F, -7.22F, 0.55F, 0.55F, 0.12F, Dilation.NONE),
				ModelTransform.NONE
		);

		root.addChild(
				"body",
				ModelPartBuilder.create()
						.uv(0, 0).cuboid(-4.5F, -13.0F, -3.0F, 9.0F, 9.0F, 6.0F, Dilation.NONE)
						.uv(0, 32).cuboid(-3.6F, -11.6F, -3.35F, 7.2F, 6.6F, 0.35F, Dilation.NONE)
						.uv(56, 20).cuboid(-3.1F, -6.9F, -3.6F, 6.2F, 0.2F, 0.3F, Dilation.NONE)
						.uv(84, 0).cuboid(-1.4F, -13.2F, -3.75F, 2.8F, 2.8F, 0.5F, Dilation.NONE)
						.uv(56, 20).cuboid(-1.0F, -12.2F, -4.02F, 2.0F, 0.18F, 0.25F, Dilation.NONE)
						.uv(56, 20).cuboid(-0.12F, -10.8F, -4.02F, 0.24F, 1.2F, 0.25F, Dilation.NONE),
				ModelTransform.NONE
		);

		root.addChild(
				"left_arm",
				ModelPartBuilder.create()
						.uv(0, 0).cuboid(0.0F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F, Dilation.NONE)
						.uv(0, 32).cuboid(4.0F, -2.3F, -2.3F, 4.0F, 4.0F, 4.0F, Dilation.NONE),
				ModelTransform.of(4.1F, -10.5F, 0.0F, 0.0F, 0.0F, -0.55F)
		);
		root.addChild(
				"right_arm",
				ModelPartBuilder.create()
						.uv(0, 0).cuboid(-5.0F, -1.5F, -1.5F, 5.0F, 3.0F, 3.0F, Dilation.NONE)
						.uv(0, 32).cuboid(-8.0F, -2.3F, -2.3F, 4.0F, 4.0F, 4.0F, Dilation.NONE),
				ModelTransform.of(-4.1F, -10.5F, 0.0F, 0.0F, 0.0F, 0.55F)
		);
		root.addChild(
				"left_leg",
				ModelPartBuilder.create().uv(0, 32).cuboid(-1.2F, -4.0F, -2.4F, 4.2F, 4.0F, 4.8F, Dilation.NONE),
				ModelTransform.pivot(1.2F, 0.0F, 0.0F)
		);
		root.addChild(
				"right_leg",
				ModelPartBuilder.create().uv(0, 32).cuboid(-3.0F, -4.0F, -2.4F, 4.2F, 4.0F, 4.8F, Dilation.NONE),
				ModelTransform.pivot(-1.2F, 0.0F, 0.0F)
		);

		ModelPartData rotor = root.addChild("rotor", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -25.1F, 0.0F));
		rotor.addChild(
				"stem",
				ModelPartBuilder.create()
						.uv(84, 0).cuboid(-0.35F, -3.1F, -0.35F, 0.7F, 3.2F, 0.7F, Dilation.NONE)
						.uv(84, 0).cuboid(-0.8F, -3.6F, -0.8F, 1.6F, 0.6F, 1.6F, Dilation.NONE),
				ModelTransform.NONE
		);
		rotor.addChild(
				"blade",
				ModelPartBuilder.create()
						.uv(84, 0).cuboid(-8.0F, -3.9F, -0.55F, 16.0F, 0.25F, 1.1F, Dilation.NONE)
						.uv(84, 0).cuboid(-0.55F, -3.88F, -8.0F, 1.1F, 0.25F, 16.0F, Dilation.NONE),
				ModelTransform.NONE
		);

		return TexturedModelData.of(modelData, 128, 64);
	}
}
