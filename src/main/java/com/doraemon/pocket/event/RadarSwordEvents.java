package com.doraemon.pocket.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RadarSwordEvents {
	private static final double PROJECTILE_SCAN_RANGE = 6.0D;
	private static final double COUNTER_RANGE_SQUARED = 3.0D * 3.0D;
	private static final double CREEPER_COUNTER_RANGE_SQUARED = 4.0D * 4.0D;
	private static final double DRAGON_CONTACT_SCAN_RANGE = 4.0D;
	private static final int PROJECTILE_SCAN_INTERVAL_TICKS = 4;
	private static final int CREEPER_SCAN_INTERVAL_TICKS = 2;
	private static final int DRAGON_SCAN_INTERVAL_TICKS = 4;
	private static final int PROJECTILE_DURABILITY_COST = 8;
	private static final int COUNTER_DURABILITY_COST = 10;
	private static final int DRAGON_DURABILITY_COST = 16;
	private static final int CREEPER_EXPLOSION_DURABILITY_COST = 40;
	private static final int DEFLECTED_PROJECTILE_COOLDOWN_TICKS = 20;
	private static final int DRAGON_PARRY_COOLDOWN_TICKS = 8;
	private static final double DEFLECT_SPEED = 2.8D;
	private static final float COUNTER_DAMAGE = 7.0F;
	private static final float DRAGON_COUNTER_DAMAGE = 4.0F;
	private static final Map<UUID, Long> DEFLECTED_PROJECTILES = new HashMap<>();
	private static final Map<UUID, Long> DRAGON_PARRY_COOLDOWNS = new HashMap<>();

	private RadarSwordEvents() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long time = server.getTicks();
			if (time % 20 == 0) {
				cleanupCaches(time);
			}
			server.getPlayerManager().getPlayerList().forEach(player -> tickPlayer(player, time));
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			DEFLECTED_PROJECTILES.clear();
			DRAGON_PARRY_COOLDOWNS.clear();
		});
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(RadarSwordEvents::allowDamage);
	}

	private static void tickPlayer(ServerPlayerEntity player, long time) {
		RadarSwordStack swordStack = findRadarSword(player);
		if (swordStack == null) {
			return;
		}

		if (shouldRunScan(player, time, PROJECTILE_SCAN_INTERVAL_TICKS)) {
			Box box = player.getBoundingBox().expand(PROJECTILE_SCAN_RANGE);
			for (Entity entity : player.getWorld().getOtherEntities(player, box, entity -> entity instanceof ProjectileEntity)) {
				ProjectileEntity projectile = (ProjectileEntity) entity;
				if (shouldDeflectProjectile(player, projectile, time)) {
					deflectProjectile(player, projectile);
					playParryFeedback(player);
					damageRadarSword(player, swordStack, PROJECTILE_DURABILITY_COST);
					return;
				}
			}
		}

		if (shouldRunScan(player, time, CREEPER_SCAN_INTERVAL_TICKS)) {
			counterNearestPrimedCreeper(player, swordStack);
		}

		if (shouldRunScan(player, time, DRAGON_SCAN_INTERVAL_TICKS)) {
			counterNearbyDragonPart(player, swordStack, time);
		}
	}

	private static boolean shouldRunScan(ServerPlayerEntity player, long time, int interval) {
		return (time + player.getId()) % interval == 0;
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayerEntity player)) {
			return true;
		}

		RadarSwordStack swordStack = findRadarSword(player);
		if (swordStack == null) {
			return true;
		}

		if (source.isIn(DamageTypeTags.IS_PROJECTILE) && source.getSource() instanceof ProjectileEntity projectile) {
			long time = player.getServerWorld().getServer().getTicks();
			if (isFriendlyOrRecentlyDeflectedProjectile(player, projectile, time)) {
				nudgeProjectileAway(player, projectile);
				return false;
			}
			deflectProjectile(player, projectile);
			playParryFeedback(player);
			damageRadarSword(player, swordStack, PROJECTILE_DURABILITY_COST);
			return false;
		}

		Entity attacker = source.getAttacker();
		Entity dragonSource = findDragonSource(source);
		if (dragonSource != null) {
			parryDragonContact(player, swordStack, dragonSource, player.getServerWorld().getServer().getTicks());
			return false;
		}

		if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
			CreeperEntity creeper = findCreeperExplosionSource(source);
			if (creeper != null && creeper.squaredDistanceTo(player) <= CREEPER_COUNTER_RANGE_SQUARED) {
				turnPlayerToFace(player, creeper);
				player.swingHand(swordStack.hand, true);
				playParryFeedback(player);
				damageRadarSword(player, swordStack, CREEPER_EXPLOSION_DURABILITY_COST);
				return false;
			}
			return true;
		}

		if (attacker instanceof MobEntity mobAttacker && attacker.squaredDistanceTo(player) <= COUNTER_RANGE_SQUARED) {
			turnPlayerToFace(player, mobAttacker);
			player.swingHand(swordStack.hand, true);
			playParryFeedback(player);
			counterAttack(player, mobAttacker);
			damageRadarSword(player, swordStack, COUNTER_DURABILITY_COST);
			return false;
		}

		return true;
	}

	private static RadarSwordStack findRadarSword(ServerPlayerEntity player) {
		ItemStack mainHand = player.getMainHandStack();
		if (mainHand.isOf(ModItems.RADAR_SWORD)) {
			return new RadarSwordStack(mainHand, Hand.MAIN_HAND);
		}

		ItemStack offHand = player.getOffHandStack();
		if (offHand.isOf(ModItems.RADAR_SWORD)) {
			return new RadarSwordStack(offHand, Hand.OFF_HAND);
		}

		return null;
	}

	private static void counterNearestPrimedCreeper(ServerPlayerEntity player, RadarSwordStack swordStack) {
		Box box = player.getBoundingBox().expand(Math.sqrt(CREEPER_COUNTER_RANGE_SQUARED));
		CreeperEntity nearestCreeper = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Entity entity : player.getWorld().getOtherEntities(player, box, entity -> entity instanceof CreeperEntity)) {
			CreeperEntity creeper = (CreeperEntity) entity;
			double distance = creeper.squaredDistanceTo(player);
			if (shouldCounterPrimedCreeper(player, creeper, distance) && distance < nearestDistance) {
				nearestCreeper = creeper;
				nearestDistance = distance;
			}
		}

		if (nearestCreeper == null) {
			return;
		}

		nearestCreeper.setFuseSpeed(-1);
		nearestCreeper.getNavigation().stop();
		turnPlayerToFace(player, nearestCreeper);
		player.swingHand(swordStack.hand, true);
		playParryFeedback(player);
		counterAttack(player, nearestCreeper);
		damageRadarSword(player, swordStack, COUNTER_DURABILITY_COST);
	}

	private static boolean shouldCounterPrimedCreeper(ServerPlayerEntity player, CreeperEntity creeper, double squaredDistance) {
		if (!creeper.isAlive() || squaredDistance > CREEPER_COUNTER_RANGE_SQUARED) {
			return false;
		}
		if (creeper.getFuseSpeed() > 0 || creeper.isIgnited()) {
			return true;
		}
		return creeper.getTarget() == player && squaredDistance <= COUNTER_RANGE_SQUARED;
	}

	private static void counterNearbyDragonPart(ServerPlayerEntity player, RadarSwordStack swordStack, long time) {
		if (DRAGON_PARRY_COOLDOWNS.getOrDefault(player.getUuid(), 0L) > time) {
			return;
		}

		Box box = player.getBoundingBox().expand(DRAGON_CONTACT_SCAN_RANGE);
		Entity nearestDragonSource = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Entity entity : player.getWorld().getOtherEntities(player, box, RadarSwordEvents::isDragonSourceEntity)) {
			double distance = entity.squaredDistanceTo(player);
			if (distance < nearestDistance) {
				nearestDragonSource = entity;
				nearestDistance = distance;
			}
		}

		Box dragonSearchBox = player.getBoundingBox().expand(32.0D);
		for (EnderDragonEntity dragon : player.getWorld().getEntitiesByClass(EnderDragonEntity.class, dragonSearchBox, EnderDragonEntity::isAlive)) {
			for (EnderDragonPart part : dragon.getBodyParts()) {
				if (!part.getBoundingBox().intersects(box)) {
					continue;
				}
				double distance = part.squaredDistanceTo(player);
				if (distance < nearestDistance) {
					nearestDragonSource = part;
					nearestDistance = distance;
				}
			}
		}

		if (nearestDragonSource != null) {
			parryDragonContact(player, swordStack, nearestDragonSource, time);
		}
	}

	private static boolean isDragonSourceEntity(Entity entity) {
		return entity instanceof EnderDragonEntity || entity instanceof EnderDragonPart;
	}

	private static Entity findDragonSource(DamageSource source) {
		Entity direct = source.getSource();
		if (isDragonSourceEntity(direct)) {
			return direct;
		}

		Entity attacker = source.getAttacker();
		return isDragonSourceEntity(attacker) ? attacker : null;
	}

	private static CreeperEntity findCreeperExplosionSource(DamageSource source) {
		if (source.getSource() instanceof CreeperEntity creeper) {
			return creeper;
		}
		if (source.getAttacker() instanceof CreeperEntity creeper) {
			return creeper;
		}
		return null;
	}

	private static boolean shouldDeflectProjectile(ServerPlayerEntity player, ProjectileEntity projectile, long time) {
		if (!projectile.isAlive() || isFriendlyOrRecentlyDeflectedProjectile(player, projectile, time)) {
			return false;
		}

		Vec3d toProjectile = projectile.getPos().subtract(player.getEyePos());
		if (toProjectile.lengthSquared() < 0.001D) {
			return true;
		}

		Vec3d look = player.getRotationVec(1.0F).normalize();
		Vec3d directionToProjectile = toProjectile.normalize();
		if (look.dotProduct(directionToProjectile) < 0.12D) {
			return false;
		}

		Vec3d projectileVelocity = projectile.getVelocity();
		if (projectileVelocity.lengthSquared() < 0.001D) {
			return true;
		}

		Vec3d toPlayer = player.getEyePos().subtract(projectile.getPos()).normalize();
		return projectileVelocity.normalize().dotProduct(toPlayer) > 0.2D;
	}

	private static boolean isFriendlyOrRecentlyDeflectedProjectile(ServerPlayerEntity player, ProjectileEntity projectile, long time) {
		return projectile.getOwner() == player || DEFLECTED_PROJECTILES.getOrDefault(projectile.getUuid(), 0L) > time;
	}

	private static void nudgeProjectileAway(ServerPlayerEntity player, ProjectileEntity projectile) {
		if (!projectile.isAlive()) {
			return;
		}

		Vec3d direction = projectile.getVelocity().lengthSquared() > 0.001D
				? projectile.getVelocity().normalize()
				: player.getRotationVec(1.0F).normalize();
		projectile.refreshPositionAfterTeleport(player.getEyePos().add(direction.multiply(2.2D)));
		projectile.setVelocity(direction.multiply(Math.max(DEFLECT_SPEED, projectile.getVelocity().length())));
		projectile.velocityModified = true;
	}

	private static void deflectProjectile(ServerPlayerEntity player, ProjectileEntity projectile) {
		Vec3d direction = player.getRotationVec(1.0F).normalize();
		double speed = Math.max(DEFLECT_SPEED, projectile.getVelocity().length() * 1.35D);
		Vec3d deflected = direction.multiply(speed).add(0.0D, 0.18D, 0.0D);

		projectile.setOwner(player);
		projectile.refreshPositionAfterTeleport(player.getEyePos().add(direction.multiply(1.4D)));
		projectile.setVelocity(deflected);
		if (projectile instanceof ExplosiveProjectileEntity explosiveProjectile) {
			explosiveProjectile.powerX = deflected.x * 0.1D;
			explosiveProjectile.powerY = deflected.y * 0.1D;
			explosiveProjectile.powerZ = deflected.z * 0.1D;
		}
		projectile.velocityModified = true;
		DEFLECTED_PROJECTILES.put(projectile.getUuid(), (long) player.getServerWorld().getServer().getTicks() + DEFLECTED_PROJECTILE_COOLDOWN_TICKS);
	}

	private static void turnPlayerToFace(ServerPlayerEntity player, Entity target) {
		Vec3d offset = target.getPos().subtract(player.getPos());
		float yaw = (float) (MathHelper.atan2(offset.z, offset.x) * 57.2957763671875D) - 90.0F;
		player.setYaw(yaw);
		player.setBodyYaw(yaw);
		player.setHeadYaw(yaw);
	}

	private static void counterAttack(ServerPlayerEntity player, LivingEntity target) {
		target.damage(player.getDamageSources().playerAttack(player), COUNTER_DAMAGE);
		Vec3d knockback = target.getPos().subtract(player.getPos()).normalize().multiply(1.25D).add(0.0D, 0.25D, 0.0D);
		target.addVelocity(knockback);
		target.velocityModified = true;
		if (target instanceof MobEntity mob) {
			mob.setTarget(null);
		}
	}

	private static void parryDragonContact(ServerPlayerEntity player, RadarSwordStack swordStack, Entity dragonSource, long time) {
		if (DRAGON_PARRY_COOLDOWNS.getOrDefault(player.getUuid(), 0L) > time) {
			return;
		}

		turnPlayerToFace(player, dragonSource);
		player.swingHand(swordStack.hand, true);
		playParryFeedback(player);
		knockPlayerAwayFromDragon(player, dragonSource);
		damageDragon(player, dragonSource);
		damageRadarSword(player, swordStack, DRAGON_DURABILITY_COST);
		DRAGON_PARRY_COOLDOWNS.put(player.getUuid(), time + DRAGON_PARRY_COOLDOWN_TICKS);
	}

	private static void knockPlayerAwayFromDragon(ServerPlayerEntity player, Entity dragonSource) {
		Vec3d away = player.getPos().subtract(dragonSource.getPos());
		if (away.lengthSquared() < 0.001D) {
			away = player.getRotationVec(1.0F).negate();
		}

		Vec3d controlledKnockback = away.normalize().multiply(0.72D).add(0.0D, 0.20D, 0.0D);
		player.setVelocity(controlledKnockback);
		player.velocityModified = true;
	}

	private static void damageDragon(ServerPlayerEntity player, Entity dragonSource) {
		EnderDragonEntity dragon = null;
		if (dragonSource instanceof EnderDragonEntity enderDragon) {
			dragon = enderDragon;
		} else if (dragonSource instanceof EnderDragonPart dragonPart) {
			dragon = dragonPart.owner;
		}

		if (dragon != null && dragon.isAlive()) {
			dragon.damage(player.getDamageSources().playerAttack(player), DRAGON_COUNTER_DAMAGE);
		}
	}

	private static void damageRadarSword(ServerPlayerEntity player, RadarSwordStack radarSwordStack, int amount) {
		ItemStack stack = radarSwordStack.stack;
		if (player.isCreative()) {
			return;
		}

		if (stack.getDamage() + amount >= stack.getMaxDamage()) {
			player.setStackInHand(radarSwordStack.hand, createDepletedRadarSword(stack));
			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 0.8F, 0.9F);
			player.sendToolBreakStatus(radarSwordStack.hand);
			return;
		}

		stack.setDamage(stack.getDamage() + amount);
	}

	private static ItemStack createDepletedRadarSword(ItemStack poweredSword) {
		ItemStack depletedSword = new ItemStack(ModItems.DEPLETED_RADAR_SWORD);
		if (poweredSword.hasNbt()) {
			depletedSword.setNbt(poweredSword.getNbt().copy());
		}
		depletedSword.setDamage(0);
		return depletedSword;
	}

	private static void playParryFeedback(ServerPlayerEntity player) {
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 0.7F, 1.9F);
		if (player.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getEyeY(), player.getZ(), 8, 0.35D, 0.3D, 0.35D, 0.04D);
		}
	}

	private static void cleanupCaches(long time) {
		Iterator<Map.Entry<UUID, Long>> iterator = DEFLECTED_PROJECTILES.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue() <= time) {
				iterator.remove();
			}
		}

		iterator = DRAGON_PARRY_COOLDOWNS.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue() <= time) {
				iterator.remove();
			}
		}
	}

	private record RadarSwordStack(ItemStack stack, Hand hand) {
	}
}
