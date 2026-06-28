package com.doraemon.pocket.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class DodgeCloakEvents {
	private static final float BASE_DODGE_CHANCE = 0.32F;
	private static final float PROJECTILE_DODGE_CHANCE = 1.0F;
	private static final double PROJECTILE_DEFLECT_SPEED = 3.4D;
	private static final double PROJECTILE_DEFLECT_DISTANCE = 3.0D;
	private static final int DEFAULT_DODGE_DURABILITY_COST = 1;
	private static final int EXPLOSION_DODGE_DURABILITY_COST = 8;
	private static final int DEFLECTED_PROJECTILE_PROTECTION_TICKS = 200;
	private static final Map<UUID, DeflectedProjectile> DEFLECTED_PROJECTILES = new HashMap<>();

	private DodgeCloakEvents() {
	}

	public static void register() {
		PlayerGadgetTickDispatcher.registerServerTick((server, time) -> {
			if (time % 20 == 0) {
				cleanupDeflectedProjectiles(time);
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> DEFLECTED_PROJECTILES.clear());
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DodgeCloakEvents::allowDamage);
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return true;
		}

		if (EnvironmentalAdaptationEvents.isEnvironmentalDamage(source)) {
			return true;
		}

		if (source.getSource() instanceof ProjectileEntity projectile && isProtectedDeflectedProjectile(entity, projectile)) {
			projectile.discard();
			return false;
		}

		CloakStack cloakStack = findCloak(entity);
		if (cloakStack == null) {
			return true;
		}

		boolean explosionDamage = source.isIn(DamageTypeTags.IS_EXPLOSION);
		boolean projectileDamage = !explosionDamage && (source.isIn(DamageTypeTags.IS_PROJECTILE) || source.getSource() instanceof ProjectileEntity);
		if (!projectileDamage && !explosionDamage && source.getAttacker() == null) {
			return true;
		}

		float chance = projectileDamage ? PROJECTILE_DODGE_CHANCE : BASE_DODGE_CHANCE;
		Random random = entity.getWorld().getRandom();

		if (random.nextFloat() >= chance) {
			return true;
		}

		if (projectileDamage && source.getSource() instanceof ProjectileEntity projectile) {
			deflectProjectile(entity, projectile, random);
			rememberDeflectedProjectile(entity, projectile);
		}

		playDodgeFeedback(entity);
		damageCloak(entity, cloakStack, explosionDamage ? EXPLOSION_DODGE_DURABILITY_COST : DEFAULT_DODGE_DURABILITY_COST);
		return false;
	}

	private static CloakStack findCloak(LivingEntity entity) {
		ItemStack chest = entity.getEquippedStack(EquipmentSlot.CHEST);
		if (chest.isOf(ModItems.DODGE_CLOAK)) {
			return new CloakStack(chest, EquipmentSlot.CHEST, null);
		}

		ItemStack mainHand = entity.getMainHandStack();
		if (mainHand.isOf(ModItems.DODGE_CLOAK)) {
			return new CloakStack(mainHand, null, Hand.MAIN_HAND);
		}

		ItemStack offHand = entity.getOffHandStack();
		if (offHand.isOf(ModItems.DODGE_CLOAK)) {
			return new CloakStack(offHand, null, Hand.OFF_HAND);
		}

		return null;
	}

	private static void deflectProjectile(LivingEntity entity, ProjectileEntity projectile, Random random) {
		Vec3d baseDirection = projectile.getVelocity().lengthSquared() > 0.001D
				? projectile.getVelocity().normalize().negate()
				: entity.getRotationVec(1.0F);
		Vec3d jitter = new Vec3d(random.nextDouble() - 0.5D, random.nextDouble() * 0.35D, random.nextDouble() - 0.5D).multiply(0.55D);
		Vec3d deflected = baseDirection.add(jitter).normalize().multiply(PROJECTILE_DEFLECT_SPEED).add(0.0D, 0.35D, 0.0D);
		Vec3d deflectedDirection = deflected.normalize();

		projectile.setOwner(entity);
		projectile.refreshPositionAfterTeleport(entity.getEyePos().add(deflectedDirection.multiply(PROJECTILE_DEFLECT_DISTANCE)));
		projectile.setVelocity(deflected);
		if (projectile instanceof ExplosiveProjectileEntity explosiveProjectile) {
			explosiveProjectile.powerX = deflected.x * 0.1D;
			explosiveProjectile.powerY = deflected.y * 0.1D;
			explosiveProjectile.powerZ = deflected.z * 0.1D;
		}
		projectile.velocityModified = true;
	}

	private static void rememberDeflectedProjectile(LivingEntity entity, ProjectileEntity projectile) {
		DEFLECTED_PROJECTILES.put(projectile.getUuid(), new DeflectedProjectile(
				entity.getUuid(),
				((ServerWorld) entity.getWorld()).getServer().getTicks() + DEFLECTED_PROJECTILE_PROTECTION_TICKS
		));
	}

	private static boolean isProtectedDeflectedProjectile(LivingEntity entity, ProjectileEntity projectile) {
		DeflectedProjectile deflectedProjectile = DEFLECTED_PROJECTILES.get(projectile.getUuid());
		if (deflectedProjectile == null) {
			return false;
		}

		if (((ServerWorld) entity.getWorld()).getServer().getTicks() > deflectedProjectile.expiresAt()) {
			DEFLECTED_PROJECTILES.remove(projectile.getUuid());
			return false;
		}

		return deflectedProjectile.protectedEntity().equals(entity.getUuid());
	}

	private static void cleanupDeflectedProjectiles(long currentTime) {
		Iterator<Map.Entry<UUID, DeflectedProjectile>> iterator = DEFLECTED_PROJECTILES.entrySet().iterator();
		while (iterator.hasNext()) {
			if (currentTime > iterator.next().getValue().expiresAt()) {
				iterator.remove();
			}
		}
	}

	private static void playDodgeFeedback(LivingEntity entity) {
		entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 0.65F, 1.65F);

		if (entity.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.POOF, entity.getX(), entity.getEyeY(), entity.getZ(), 12, 0.35D, 0.45D, 0.35D, 0.02D);
		}
	}

	private static void damageCloak(LivingEntity entity, CloakStack cloakStack, int amount) {
		cloakStack.stack.damage(amount, entity, wearer -> {
			if (wearer instanceof ServerPlayerEntity player && cloakStack.hand != null) {
				player.sendToolBreakStatus(cloakStack.hand);
			} else if (cloakStack.slot != null) {
				wearer.sendEquipmentBreakStatus(cloakStack.slot);
			}
		});
	}

	private record CloakStack(ItemStack stack, EquipmentSlot slot, Hand hand) {
	}

	private record DeflectedProjectile(UUID protectedEntity, long expiresAt) {
	}
}
