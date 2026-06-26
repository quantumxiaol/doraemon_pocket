package com.doraemon.pocket.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class MomotaroObedienceHandler {
	private static final int DURATION_TICKS = 20 * 60;
	private static final double FOLLOW_START_DISTANCE_SQUARED = 4.5D * 4.5D;
	private static final double FOLLOW_STOP_DISTANCE_SQUARED = 3.0D * 3.0D;
	private static final double FOLLOW_MAX_DISTANCE_SQUARED = 14.0D * 14.0D;
	private static final double FOLLOW_SPEED = 1.05D;
	private static final float MIN_RIDE_WIDTH = 0.8F;
	private static final float MIN_RIDE_HEIGHT = 0.9F;
	private static final double RIDE_SPEED = 0.34D;
	private static final int REPATH_INTERVAL_TICKS = 10;
	private static final double OWNER_REPATH_DISTANCE_SQUARED = 2.0D * 2.0D;
	private static final int MAX_OBEDIENT_ENTITIES_PER_PLAYER = 16;
	private static final Map<UUID, ObedienceState> OBEDIENT_ENTITIES = new HashMap<>();

	private MomotaroObedienceHandler() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MomotaroObedienceHandler::tick);
		ServerLifecycleEvents.SERVER_STOPPING.register(MomotaroObedienceHandler::releaseLoadedStates);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> OBEDIENT_ENTITIES.clear());
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

		if (isPermanentlyTameable(entity)) {
			return applyPermanentTaming(player, entity);
		}

		if (!canTemporarilyControl(player, entity)) {
			return UseResult.FAILED;
		}
		if (countObedientEntities(player.getUuid()) >= MAX_OBEDIENT_ENTITIES_PER_PLAYER) {
			return UseResult.FAILED;
		}

		ObedienceState state = ObedienceState.capture(player, entity);
		OBEDIENT_ENTITIES.put(entityUuid, state);
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

		if (isPermanentlyTameable(entity)) {
			return applyPermanentTaming(player, entity);
		}

		if (!canTemporarilyControl(player, entity)) {
			return UseResult.FAILED;
		}
		if (countObedientEntities(player.getUuid()) >= MAX_OBEDIENT_ENTITIES_PER_PLAYER) {
			return UseResult.FAILED;
		}

		ObedienceState state = ObedienceState.capture(player, entity);
		OBEDIENT_ENTITIES.put(entityUuid, state);
		clearHostility(entity);
		return UseResult.APPLIED;
	}

	private static void tick(MinecraftServer server) {
		Iterator<Map.Entry<UUID, ObedienceState>> iterator = OBEDIENT_ENTITIES.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, ObedienceState> entry = iterator.next();
			ObedienceState state = entry.getValue();
			Entity entity = findEntity(server, state.worldKey, entry.getKey());

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
				followOwner(livingEntity, owner, state);
			}
		}
	}

	private static void releaseLoadedStates(MinecraftServer server) {
		for (Map.Entry<UUID, ObedienceState> entry : OBEDIENT_ENTITIES.entrySet()) {
			Entity entity = findEntity(server, entry.getValue().worldKey, entry.getKey());
			if (entity instanceof LivingEntity livingEntity) {
				release(livingEntity, entry.getValue());
			}
		}
		OBEDIENT_ENTITIES.clear();
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
		return entity instanceof MobEntity && !isPermanentlyTameable(entity);
	}

	private static boolean isPermanentlyTameable(LivingEntity entity) {
		return entity instanceof TameableEntity || entity instanceof AbstractHorseEntity;
	}

	private static UseResult applyPermanentTaming(ServerPlayerEntity player, LivingEntity entity) {
		if (entity instanceof TameableEntity tameable) {
			UUID ownerUuid = tameable.getOwnerUuid();
			if (tameable.isTamed() && ownerUuid != null && !ownerUuid.equals(player.getUuid())) {
				return UseResult.FAILED;
			}
			if (tameable.isTamed() && player.getUuid().equals(ownerUuid)) {
				return tryMount(player, entity) ? UseResult.MOUNTED : UseResult.ALREADY_ACTIVE;
			}
			tameable.setSitting(false);
			tameable.setOwner(player);
			clearHostility(entity);
			return UseResult.APPLIED;
		}

		if (entity instanceof AbstractHorseEntity horse) {
			UUID ownerUuid = horse.getOwnerUuid();
			if (horse.isTame() && ownerUuid != null && !ownerUuid.equals(player.getUuid())) {
				return UseResult.FAILED;
			}
			if (horse.isTame() && player.getUuid().equals(ownerUuid)) {
				return tryMount(player, entity) ? UseResult.MOUNTED : UseResult.ALREADY_ACTIVE;
			}
			horse.setTame(true);
			horse.setOwnerUuid(player.getUuid());
			horse.setAngry(false);
			clearHostility(entity);
			return UseResult.APPLIED;
		}

		return UseResult.FAILED;
	}

	private static void release(LivingEntity entity, ObedienceState state) {
		dismountOwner(entity, state.ownerUuid);

		if (entity instanceof MobEntity mob) {
			mob.getNavigation().stop();
			mob.setTarget(null);
			mob.setAttacking(false);
		}
		entity.setAttacker(null);

		entity.setStepHeight(state.stepHeight);
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
		mob.setStepHeight(Math.max(mob.getStepHeight(), 1.0F));
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

	private static void followOwner(LivingEntity entity, ServerPlayerEntity owner, ObedienceState state) {
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
			long time = mob.getWorld().getTime();
			boolean ownerMoved = state.lastOwnerTargetPos == null || state.lastOwnerTargetPos.squaredDistanceTo(owner.getPos()) >= OWNER_REPATH_DISTANCE_SQUARED;
			if (time >= state.nextRepathTick || mob.getNavigation().isIdle() || ownerMoved) {
				mob.getNavigation().startMovingTo(owner, FOLLOW_SPEED);
				state.nextRepathTick = time + REPATH_INTERVAL_TICKS;
				state.lastOwnerTargetPos = owner.getPos();
			}
		}
	}

	private static Entity findEntity(MinecraftServer server, RegistryKey<World> worldKey, UUID uuid) {
		ServerWorld world = server.getWorld(worldKey);
		if (world == null) {
			return null;
		}
		return world.getEntity(uuid);
	}

	private static int countObedientEntities(UUID ownerUuid) {
		int count = 0;
		for (ObedienceState state : OBEDIENT_ENTITIES.values()) {
			if (state.ownerUuid.equals(ownerUuid)) {
				count++;
			}
		}
		return count;
	}

	public enum UseResult {
		APPLIED,
		RELEASED,
		MOUNTED,
		ALREADY_ACTIVE,
		FAILED
	}

	private static final class ObedienceState {
		private final UUID ownerUuid;
		private final RegistryKey<World> worldKey;
		private final float stepHeight;
		private long expiresAt;
		private long nextRepathTick;
		private Vec3d lastOwnerTargetPos;

		private ObedienceState(UUID ownerUuid, RegistryKey<World> worldKey, long expiresAt, float stepHeight) {
			this.ownerUuid = ownerUuid;
			this.worldKey = worldKey;
			this.expiresAt = expiresAt;
			this.stepHeight = stepHeight;
		}

		private ObedienceState withRefreshedDuration(long refreshedExpiresAt) {
			expiresAt = refreshedExpiresAt;
			return this;
		}

		private static ObedienceState capture(ServerPlayerEntity player, LivingEntity entity) {
			return new ObedienceState(player.getUuid(), entity.getWorld().getRegistryKey(), entity.getWorld().getTime() + DURATION_TICKS, entity.getStepHeight());
		}
	}
}
