package com.doraemon.pocket.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class MomotaroObedienceHandler {
	private static final int DURATION_TICKS = 20 * 60;
	private static final double FOLLOW_START_DISTANCE_SQUARED = 4.5D * 4.5D;
	private static final double FOLLOW_STOP_DISTANCE_SQUARED = 3.0D * 3.0D;
	private static final double FOLLOW_MAX_DISTANCE_SQUARED = 14.0D * 14.0D;
	private static final double FOLLOW_SPEED = 1.05D;
	private static final float MIN_RIDE_WIDTH = 0.8F;
	private static final float MIN_RIDE_HEIGHT = 0.9F;
	private static final double RIDE_SPEED = 0.34D;
	private static final Map<UUID, ObedienceState> OBEDIENT_ENTITIES = new HashMap<>();

	private MomotaroObedienceHandler() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MomotaroObedienceHandler::tick);
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(MomotaroObedienceHandler::allowDamage);
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer) || !(entity instanceof LivingEntity livingEntity)) {
				return ActionResult.PASS;
			}
			return interactObedientEntity(serverPlayer, livingEntity);
		});
	}

	public static UseResult useOnEntity(ServerPlayerEntity player, LivingEntity entity, boolean releaseRequested) {
		if (!(entity instanceof MobEntity)) {
			return UseResult.FAILED;
		}

		UUID entityUuid = entity.getUuid();
		ObedienceState existing = OBEDIENT_ENTITIES.get(entityUuid);
		if (existing != null) {
			if (!existing.ownerUuid.equals(player.getUuid())) {
				return UseResult.FAILED;
			}
			if (releaseRequested) {
				release(entity, existing);
				OBEDIENT_ENTITIES.remove(entityUuid);
				return UseResult.RELEASED;
			}
			return tryMount(player, entity) ? UseResult.MOUNTED : UseResult.ALREADY_ACTIVE;
		}

		if (!canTemporarilyControl(player, entity)) {
			return UseResult.FAILED;
		}

		ObedienceState state = ObedienceState.capture(player, entity);
		OBEDIENT_ENTITIES.put(entityUuid, state);
		applyTemporaryTaming(player, entity);
		clearHostility(entity);
		return UseResult.APPLIED;
	}

	public static UseResult applyThrownDumpling(ServerPlayerEntity player, LivingEntity entity) {
		if (!(entity instanceof MobEntity)) {
			return UseResult.FAILED;
		}

		UUID entityUuid = entity.getUuid();
		ObedienceState existing = OBEDIENT_ENTITIES.get(entityUuid);
		if (existing != null) {
			if (!existing.ownerUuid.equals(player.getUuid())) {
				return UseResult.FAILED;
			}
			OBEDIENT_ENTITIES.put(entityUuid, existing.withRefreshedDuration(entity.getWorld().getTime() + DURATION_TICKS));
			clearHostility(entity);
			return UseResult.APPLIED;
		}

		if (!canTemporarilyControl(player, entity)) {
			return UseResult.FAILED;
		}

		ObedienceState state = ObedienceState.capture(player, entity);
		OBEDIENT_ENTITIES.put(entityUuid, state);
		applyTemporaryTaming(player, entity);
		clearHostility(entity);
		return UseResult.APPLIED;
	}

	private static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, ObedienceState>> iterator = OBEDIENT_ENTITIES.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, ObedienceState> entry = iterator.next();
			Entity entity = findEntity(server, entry.getKey());
			ObedienceState state = entry.getValue();

			if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isAlive()) {
				iterator.remove();
				continue;
			}

			if (livingEntity.getWorld().getTime() >= state.expiresAt) {
				release(livingEntity, state);
				iterator.remove();
				continue;
			}

			ServerPlayerEntity owner = server.getPlayerManager().getPlayer(state.ownerUuid);
			if (owner == null || owner.isRemoved() || owner.getWorld() != livingEntity.getWorld()) {
				continue;
			}

			clearHostility(livingEntity);
			if (livingEntity.getPassengerList().contains(owner)) {
				controlMountedEntity(livingEntity, owner);
			} else {
				followOwner(livingEntity, owner);
			}
		}
	}

	private static ActionResult interactObedientEntity(ServerPlayerEntity player, LivingEntity entity) {
		ObedienceState state = OBEDIENT_ENTITIES.get(entity.getUuid());
		if (state == null || !state.ownerUuid.equals(player.getUuid())) {
			return ActionResult.PASS;
		}

		if (player.isSneaking()) {
			release(entity, state);
			OBEDIENT_ENTITIES.remove(entity.getUuid());
			return ActionResult.SUCCESS;
		}

		return tryMount(player, entity) ? ActionResult.SUCCESS : ActionResult.PASS;
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayerEntity player)) {
			return true;
		}

		Entity attacker = source.getAttacker();
		if (!(attacker instanceof LivingEntity livingAttacker)) {
			return true;
		}

		ObedienceState state = OBEDIENT_ENTITIES.get(livingAttacker.getUuid());
		return state == null || !state.ownerUuid.equals(player.getUuid());
	}

	private static boolean canTemporarilyControl(ServerPlayerEntity player, LivingEntity entity) {
		if (entity instanceof TameableEntity tameable) {
			UUID ownerUuid = tameable.getOwnerUuid();
			return !tameable.isTamed() || ownerUuid == null || ownerUuid.equals(player.getUuid());
		}

		if (entity instanceof AbstractHorseEntity horse) {
			UUID ownerUuid = horse.getOwnerUuid();
			return !horse.isTame() || ownerUuid == null || ownerUuid.equals(player.getUuid());
		}

		return entity instanceof MobEntity;
	}

	private static void applyTemporaryTaming(ServerPlayerEntity player, LivingEntity entity) {
		if (entity instanceof TameableEntity tameable) {
			tameable.setSitting(false);
			tameable.setOwner(player);
			return;
		}

		if (entity instanceof AbstractHorseEntity horse) {
			horse.setTame(true);
			horse.setOwnerUuid(player.getUuid());
			horse.setAngry(false);
		}
	}

	private static void release(LivingEntity entity, ObedienceState state) {
		dismountOwner(entity, state.ownerUuid);

		if (entity instanceof MobEntity mob) {
			mob.getNavigation().stop();
			mob.setTarget(null);
			mob.setAttacking(false);
		}
		entity.setAttacker(null);

		if (entity instanceof TameableEntity tameable && state.tameableState != null) {
			tameable.setSitting(state.tameableState.sitting);
			tameable.setTamed(state.tameableState.tamed);
			tameable.setOwnerUuid(state.tameableState.ownerUuid);
		}

		if (entity instanceof AbstractHorseEntity horse && state.horseState != null) {
			horse.setTame(state.horseState.tame);
			horse.setOwnerUuid(state.horseState.ownerUuid);
			horse.setAngry(state.horseState.angry);
		}
	}

	private static boolean tryMount(ServerPlayerEntity player, LivingEntity entity) {
		if (!canRideWithMomotaro(entity)) {
			return false;
		}
		if (player.hasVehicle()) {
			return entity.getPassengerList().contains(player);
		}
		if (entity.hasPassengers() && !entity.getPassengerList().contains(player)) {
			return false;
		}

		clearHostility(entity);
		if (entity instanceof MobEntity mob) {
			mob.getNavigation().stop();
		}
		return player.startRiding(entity, true);
	}

	private static boolean canRideWithMomotaro(LivingEntity entity) {
		return entity instanceof MobEntity && entity.getWidth() >= MIN_RIDE_WIDTH && entity.getHeight() >= MIN_RIDE_HEIGHT;
	}

	private static void controlMountedEntity(LivingEntity entity, ServerPlayerEntity rider) {
		if (!(entity instanceof MobEntity mob)) {
			return;
		}

		mob.getNavigation().stop();
		float yaw = rider.getYaw();
		mob.setYaw(yaw);
		mob.setBodyYaw(yaw);
		mob.setHeadYaw(yaw);
		mob.getLookControl().lookAt(rider, 30.0F, 30.0F);

		float forward = MathHelper.clamp(rider.forwardSpeed, -1.0F, 1.0F);
		float sideways = MathHelper.clamp(rider.sidewaysSpeed, -1.0F, 1.0F);
		Vec3d currentVelocity = mob.getVelocity();
		double inputLength = Math.sqrt(forward * forward + sideways * sideways);
		if (inputLength < 0.01D) {
			mob.setVelocity(currentVelocity.x * 0.45D, currentVelocity.y, currentVelocity.z * 0.45D);
			mob.velocityModified = true;
			return;
		}

		double yawRadians = yaw * Math.PI / 180.0D;
		double forwardX = -MathHelper.sin((float) yawRadians);
		double forwardZ = MathHelper.cos((float) yawRadians);
		double sideX = MathHelper.cos((float) yawRadians);
		double sideZ = MathHelper.sin((float) yawRadians);
		double x = (forwardX * forward + sideX * sideways) / inputLength * RIDE_SPEED;
		double z = (forwardZ * forward + sideZ * sideways) / inputLength * RIDE_SPEED;

		mob.setVelocity(x, currentVelocity.y, z);
		mob.velocityModified = true;
	}

	private static void dismountOwner(LivingEntity entity, UUID ownerUuid) {
		for (Entity passenger : entity.getPassengerList()) {
			if (passenger.getUuid().equals(ownerUuid)) {
				passenger.stopRiding();
				return;
			}
		}
	}

	private static void clearHostility(LivingEntity entity) {
		entity.setAttacker(null);
		if (entity instanceof MobEntity mob) {
			mob.setTarget(null);
			mob.setAttacking(false);
		}
		if (entity instanceof Angerable angerable) {
			angerable.stopAnger();
			angerable.setTarget(null);
			angerable.setAttacker(null);
		}
		if (entity instanceof AbstractHorseEntity horse) {
			horse.setAngry(false);
		}
	}

	private static void followOwner(LivingEntity entity, ServerPlayerEntity owner) {
		if (!(entity instanceof MobEntity mob)) {
			return;
		}

		double distanceSquared = mob.squaredDistanceTo(owner);
		if (distanceSquared <= FOLLOW_STOP_DISTANCE_SQUARED) {
			mob.getNavigation().stop();
			return;
		}
		if (distanceSquared <= FOLLOW_MAX_DISTANCE_SQUARED && distanceSquared >= FOLLOW_START_DISTANCE_SQUARED) {
			mob.getLookControl().lookAt(owner, 30.0F, 30.0F);
			mob.getNavigation().startMovingTo(owner, FOLLOW_SPEED);
		}
	}

	private static Entity findEntity(MinecraftServer server, UUID uuid) {
		for (ServerWorld world : server.getWorlds()) {
			Entity entity = world.getEntity(uuid);
			if (entity != null) {
				return entity;
			}
		}
		return null;
	}

	public enum UseResult {
		APPLIED,
		RELEASED,
		MOUNTED,
		ALREADY_ACTIVE,
		FAILED
	}

	private record ObedienceState(UUID ownerUuid, long expiresAt, TameableState tameableState, HorseState horseState) {
		ObedienceState withRefreshedDuration(long refreshedExpiresAt) {
			return new ObedienceState(ownerUuid, refreshedExpiresAt, tameableState, horseState);
		}

		static ObedienceState capture(ServerPlayerEntity player, LivingEntity entity) {
			TameableState tameableState = entity instanceof TameableEntity tameable
					? new TameableState(tameable.isTamed(), tameable.getOwnerUuid(), tameable.isSitting())
					: null;
			HorseState horseState = entity instanceof AbstractHorseEntity horse
					? new HorseState(horse.isTame(), horse.getOwnerUuid(), horse.isAngry())
					: null;
			return new ObedienceState(player.getUuid(), entity.getWorld().getTime() + DURATION_TICKS, tameableState, horseState);
		}
	}

	private record TameableState(boolean tamed, UUID ownerUuid, boolean sitting) {
	}

	private record HorseState(boolean tame, UUID ownerUuid, boolean angry) {
	}
}
