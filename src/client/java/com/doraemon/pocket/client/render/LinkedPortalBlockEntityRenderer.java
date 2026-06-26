package com.doraemon.pocket.client.render;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import com.doraemon.pocket.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class LinkedPortalBlockEntityRenderer implements BlockEntityRenderer<LinkedPortalBlockEntity> {
	private static final float DOOR_OPEN_DEGREES = 150.0F;
	private static final float ANIMATION_SPEED_PER_TICK = 0.28F;
	private static final int MAX_ANIMATION_STATES = 512;
	private static final Map<Long, DoorAnimationState> DOOR_ANIMATIONS = new HashMap<>();

	public LinkedPortalBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
	}

	@Override
	public void render(LinkedPortalBlockEntity portal, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		BlockState state = portal.getCachedState();
		if (!state.isOf(ModBlocks.ANYWHERE_DOOR_PORTAL) || state.get(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
			return;
		}

		Sprite lower = sprite("anywhere_door_portal_lower");
		Sprite upper = sprite("anywhere_door_portal_upper");
		VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getCutout());

		matrices.push();
		rotateToFacing(matrices, state.get(DoorBlock.FACING));
		renderFrame(matrices, consumer, lower, light);
		renderDoorPanel(matrices, consumer, lower, upper, getOpenProgress(portal, state.get(DoorBlock.OPEN)), state.get(DoorBlock.HINGE), light);
		matrices.pop();
	}

	@Override
	public boolean rendersOutsideBoundingBox(LinkedPortalBlockEntity blockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}

	private static void rotateToFacing(MatrixStack matrices, Direction facing) {
		float degrees = switch (facing) {
			case EAST -> 90.0F;
			case SOUTH -> 180.0F;
			case WEST -> 270.0F;
			default -> 0.0F;
		};
		matrices.translate(0.5D, 0.0D, 0.5D);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(degrees));
		matrices.translate(-0.5D, 0.0D, -0.5D);
	}

	private static void renderFrame(MatrixStack matrices, VertexConsumer consumer, Sprite sprite, int light) {
		int frame = 0x4b183b;
		renderCuboid(matrices, consumer, sprite, 0.00F, 0.00F, 0.88F, 0.10F, 2.08F, 1.08F, frame, light);
		renderCuboid(matrices, consumer, sprite, 0.90F, 0.00F, 0.88F, 1.00F, 2.08F, 1.08F, frame, light);
		renderCuboid(matrices, consumer, sprite, 0.00F, 1.92F, 0.88F, 1.00F, 2.08F, 1.08F, frame, light);
		renderCuboid(matrices, consumer, sprite, 0.00F, 0.00F, 0.88F, 1.00F, 0.08F, 1.08F, frame, light);
	}

	private static void renderDoorPanel(MatrixStack matrices, VertexConsumer consumer, Sprite lower, Sprite upper, float openProgress, DoorHinge hinge, int light) {
		matrices.push();
		float pivotX = hinge == DoorHinge.LEFT ? 0.10F : 0.90F;
		float angle = (hinge == DoorHinge.LEFT ? -DOOR_OPEN_DEGREES : DOOR_OPEN_DEGREES) * openProgress;
		matrices.translate(pivotX, 0.0F, 0.98F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));
		matrices.translate(-pivotX, 0.0F, -0.98F);

		renderCuboid(matrices, consumer, lower, 0.10F, 0.02F, 0.94F, 0.90F, 0.99F, 1.02F, 0xffffff, light);
		renderCuboid(matrices, consumer, upper, 0.10F, 1.00F, 0.94F, 0.90F, 1.92F, 1.02F, 0xffffff, light);
		renderCuboid(matrices, consumer, lower, 0.76F, 0.48F, 0.90F, 0.86F, 0.58F, 1.06F, 0xf6d84d, light);
		matrices.pop();
	}

	private static float getOpenProgress(LinkedPortalBlockEntity portal, boolean open) {
		if (portal.getWorld() == null) {
			return open ? 1.0F : 0.0F;
		}

		long time = portal.getWorld().getTime();
		long key = portal.getPos().asLong();
		DoorAnimationState animation = DOOR_ANIMATIONS.computeIfAbsent(key, ignored -> new DoorAnimationState(open ? 1.0F : 0.0F, time));
		long elapsed = Math.max(0L, time - animation.lastTick);
		animation.lastTick = time;

		float target = open ? 1.0F : 0.0F;
		if (elapsed > 0L && animation.progress != target) {
			float step = ANIMATION_SPEED_PER_TICK * elapsed;
			if (animation.progress < target) {
				animation.progress = Math.min(target, animation.progress + step);
			} else {
				animation.progress = Math.max(target, animation.progress - step);
			}
		}

		if (DOOR_ANIMATIONS.size() > MAX_ANIMATION_STATES) {
			Iterator<Map.Entry<Long, DoorAnimationState>> iterator = DOOR_ANIMATIONS.entrySet().iterator();
			while (DOOR_ANIMATIONS.size() > MAX_ANIMATION_STATES / 2 && iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		}

		return animation.progress;
	}

	private static Sprite sprite(String name) {
		return MinecraftClient.getInstance()
				.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
				.apply(DoraemonPocket.id("block/" + name));
	}

	private static void renderCuboid(MatrixStack matrices, VertexConsumer consumer, Sprite sprite, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int color, int light) {
		float r = ((color >> 16) & 0xff) / 255.0F;
		float g = ((color >> 8) & 0xff) / 255.0F;
		float b = (color & 0xff) / 255.0F;
		MatrixStack.Entry entry = matrices.peek();
		float u0 = sprite.getMinU();
		float u1 = sprite.getMaxU();
		float v0 = sprite.getMinV();
		float v1 = sprite.getMaxV();

		quad(entry, consumer, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, r, g, b, u0, v1, u1, v0, light, 0.0F, 0.0F, -1.0F);
		quad(entry, consumer, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, r, g, b, u0, v1, u1, v0, light, 0.0F, 0.0F, 1.0F);
		quad(entry, consumer, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, u0, v1, u1, v0, light, 0.0F, 1.0F, 0.0F);
		quad(entry, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, r, g, b, u0, v1, u1, v0, light, 0.0F, -1.0F, 0.0F);
		quad(entry, consumer, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, r, g, b, u0, v1, u1, v0, light, -1.0F, 0.0F, 0.0F);
		quad(entry, consumer, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, u0, v1, u1, v0, light, 1.0F, 0.0F, 0.0F);
	}

	private static void quad(MatrixStack.Entry entry, VertexConsumer consumer,
			float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4,
			float r, float g, float b, float u0, float v1, float u1, float v0, int light, float normalX, float normalY, float normalZ) {
		Matrix4f position = entry.getPositionMatrix();
		Matrix3f normal = entry.getNormalMatrix();
		vertex(consumer, position, normal, x1, y1, z1, r, g, b, u0, v1, light, normalX, normalY, normalZ);
		vertex(consumer, position, normal, x2, y2, z2, r, g, b, u1, v1, light, normalX, normalY, normalZ);
		vertex(consumer, position, normal, x3, y3, z3, r, g, b, u1, v0, light, normalX, normalY, normalZ);
		vertex(consumer, position, normal, x4, y4, z4, r, g, b, u0, v0, light, normalX, normalY, normalZ);
	}

	private static void vertex(VertexConsumer consumer, Matrix4f position, Matrix3f normal, float x, float y, float z, float r, float g, float b, float u, float v, int light, float normalX, float normalY, float normalZ) {
		consumer.vertex(position, x, y, z)
				.color(r, g, b, 1.0F)
				.texture(u, v)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(light)
				.normal(normal, normalX, normalY, normalZ)
				.next();
	}

	private static final class DoorAnimationState {
		private float progress;
		private long lastTick;

		private DoorAnimationState(float progress, long lastTick) {
			this.progress = progress;
			this.lastTick = lastTick;
		}
	}
}
