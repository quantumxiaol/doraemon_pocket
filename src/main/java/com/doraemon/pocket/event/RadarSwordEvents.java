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
	private static final int PROJECTILE_DURABILITY_COST = 8;
	private static final int COUNTER_DURABILITY_COST = 10;
	private static final int DEFLECTED_PROJECTILE_COOLDOWN_TICKS = 20;
	private static final double DEFLECT_SPEED = 2.8D;
	private static final float COUNTER_DAMAGE = 7.0F;
	private static final Map<UUID, Long> DEFLECTED_PROJECTILES = new HashMap<>();

	private RadarSwordEvents() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % 20 == 0) {
				cleanupDeflectedProjectiles(server.getTicks());
			}
			server.getPlayerManager().getPlayerList().forEach(RadarSwordEvents::tickPlayer);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> DEFLECTED_PROJECTILES.clear());
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(RadarSwordEvents::allowDamage);
	}

	private static void tickPlayer(ServerPlayerEntity player) {
		RadarSwordStack swordStack = findRadarSword(player);
		if (swordStack == null) {
			return;
		}

		long time = player.getServerWorld().getServer().getTicks();
		if (player.age % 2 == 0) {
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

		if (player.age % 5 == 0) {
			counterNearestPrimedCreeper(player, swordStack);
		}
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
			deflectProjectile(player, projectile);
			playParryFeedback(player);
			damageRadarSword(player, swordStack, PROJECTILE_DURABILITY_COST);
			return false;
		}

		Entity attacker = source.getAttacker();
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

	private static boolean shouldDeflectProjectile(ServerPlayerEntity player, ProjectileEntity projectile, long time) {
		if (!projectile.isAlive() || projectile.getOwner() == player || DEFLECTED_PROJECTILES.getOrDefault(projectile.getUuid(), 0L) > time) {
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

	private static void turnPlayerToFace(ServerPlayerEntity player, LivingEntity target) {
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

	private static void cleanupDeflectedProjectiles(long time) {
		Iterator<Map.Entry<UUID, Long>> iterator = DEFLECTED_PROJECTILES.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue() <= time) {
				iterator.remove();
			}
		}
	}

	private record RadarSwordStack(ItemStack stack, Hand hand) {
	}
}
