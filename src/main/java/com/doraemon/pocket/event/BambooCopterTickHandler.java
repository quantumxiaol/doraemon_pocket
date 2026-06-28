package com.doraemon.pocket.event;

import com.doraemon.pocket.item.BambooCopterItem;
import com.doraemon.pocket.network.BambooCopterControl;
import com.doraemon.pocket.network.DoraemonPackets;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class BambooCopterTickHandler {
	private static final double ASCEND_ACCELERATION = 0.075D;
	private static final double ASCEND_TARGET_SPEED = 0.28D;
	private static final double MAX_ASCEND_SPEED = 0.55D;
	private static final double DESCEND_ACCELERATION = 0.055D;
	private static final double MAX_DESCEND_SPEED = -0.28D;
	private static final double HOVER_FALL_SPEED = -0.06D;
	private static final double HORIZONTAL_ACCELERATION = 0.055D;
	private static final double MAX_HORIZONTAL_SPEED = 0.48D;
	private static final double AIR_BRAKE = 0.88D;
	private static final int DURABILITY_DAMAGE_INTERVAL = 10;

	private BambooCopterTickHandler() {
	}

	public static void register() {
		PlayerGadgetTickDispatcher.registerPlayerTick((player, time) -> tick(player));
	}

	private static void tick(ServerPlayerEntity player) {
		ItemStack copter = BambooCopterItem.getEquippedStack(player);

		if (copter.isEmpty() || player.isSpectator()) {
			DoraemonPackets.forgetBambooCopterControl(player.getUuid());
			return;
		}

		BambooCopterControl control = DoraemonPackets.getBambooCopterControl(player);
		if (!isFlightActive(player, control)) {
			return;
		}

		applyFlight(player, control);
		damageCopter(player, copter);
	}

	private static boolean isFlightActive(ServerPlayerEntity player, BambooCopterControl control) {
		if (player.hasVehicle() || player.isFallFlying() || player.isClimbing() || player.isTouchingWater()) {
			return false;
		}

		return control.vertical() > 0 || !player.isOnGround();
	}

	private static void applyFlight(ServerPlayerEntity player, BambooCopterControl control) {
		Vec3d velocity = player.getVelocity();
		double nextXVelocity = velocity.x;
		double yVelocity = velocity.y;
		double nextYVelocity = yVelocity;
		double nextZVelocity = velocity.z;

		if (control.vertical() > 0) {
			nextYVelocity = Math.min(Math.max(yVelocity + ASCEND_ACCELERATION, ASCEND_TARGET_SPEED), MAX_ASCEND_SPEED);
		} else if (control.vertical() < 0) {
			nextYVelocity = Math.max(yVelocity - DESCEND_ACCELERATION, MAX_DESCEND_SPEED);
		} else if (!player.isOnGround() && yVelocity < HOVER_FALL_SPEED) {
			nextYVelocity = HOVER_FALL_SPEED;
		}

		Vec3d horizontalMovement = getHorizontalMovement(control, player.getYaw());
		if (horizontalMovement.lengthSquared() > 0.0D) {
			nextXVelocity += horizontalMovement.x * HORIZONTAL_ACCELERATION;
			nextZVelocity += horizontalMovement.z * HORIZONTAL_ACCELERATION;

			double horizontalSpeed = Math.sqrt(nextXVelocity * nextXVelocity + nextZVelocity * nextZVelocity);
			if (horizontalSpeed > MAX_HORIZONTAL_SPEED) {
				double scale = MAX_HORIZONTAL_SPEED / horizontalSpeed;
				nextXVelocity *= scale;
				nextZVelocity *= scale;
			}
		} else if (!player.isOnGround()) {
			nextXVelocity *= AIR_BRAKE;
			nextZVelocity *= AIR_BRAKE;
		}

		if (nextXVelocity != velocity.x || nextYVelocity != yVelocity || nextZVelocity != velocity.z) {
			player.setVelocity(nextXVelocity, nextYVelocity, nextZVelocity);
			player.velocityModified = true;
		}

		player.fallDistance = 0.0F;
	}

	private static Vec3d getHorizontalMovement(BambooCopterControl control, float yaw) {
		double forward = control.forward();
		double sideways = control.sideways();

		if (Math.abs(forward) < 0.01D && Math.abs(sideways) < 0.01D) {
			return Vec3d.ZERO;
		}

		double yawRadians = Math.toRadians(yaw);
		double sin = Math.sin(yawRadians);
		double cos = Math.cos(yawRadians);
		double x = sideways * cos - forward * sin;
		double z = forward * cos + sideways * sin;
		Vec3d movement = new Vec3d(x, 0.0D, z);

		if (movement.lengthSquared() > 1.0D) {
			return movement.normalize();
		}

		return movement;
	}

	private static void damageCopter(ServerPlayerEntity player, ItemStack copter) {
		if (player.isCreative() || player.age % DURABILITY_DAMAGE_INTERVAL != 0) {
			return;
		}

		copter.damage(1, player, brokenPlayer -> brokenPlayer.sendEquipmentBreakStatus(EquipmentSlot.HEAD));
	}
}
