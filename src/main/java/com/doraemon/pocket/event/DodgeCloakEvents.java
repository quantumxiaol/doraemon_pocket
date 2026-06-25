package com.doraemon.pocket.event;

import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
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
	private static final double PROJECTILE_DEFLECT_SPEED = 2.2D;

	private DodgeCloakEvents() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DodgeCloakEvents::allowDamage);
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return true;
		}

		CloakStack cloakStack = findCloak(entity);
		if (cloakStack == null) {
			return true;
		}

		boolean projectileDamage = source.isIn(DamageTypeTags.IS_PROJECTILE) || source.getSource() instanceof ProjectileEntity;
		float chance = projectileDamage ? PROJECTILE_DODGE_CHANCE : BASE_DODGE_CHANCE;
		Random random = entity.getWorld().getRandom();

		if (random.nextFloat() >= chance) {
			return true;
		}

		if (projectileDamage && source.getSource() instanceof ProjectileEntity projectile) {
			deflectProjectile(entity, projectile, random);
		}

		playDodgeFeedback(entity);
		damageCloak(entity, cloakStack);
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
		Vec3d deflected = baseDirection.add(jitter).normalize().multiply(PROJECTILE_DEFLECT_SPEED);

		projectile.setOwner(entity);
		projectile.refreshPositionAfterTeleport(entity.getEyePos().add(deflected.normalize().multiply(1.2D)));
		projectile.setVelocity(deflected);
		if (projectile instanceof ExplosiveProjectileEntity explosiveProjectile) {
			explosiveProjectile.powerX = deflected.x * 0.1D;
			explosiveProjectile.powerY = deflected.y * 0.1D;
			explosiveProjectile.powerZ = deflected.z * 0.1D;
		}
		projectile.velocityModified = true;
	}

	private static void playDodgeFeedback(LivingEntity entity) {
		entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 0.65F, 1.65F);

		if (entity.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.POOF, entity.getX(), entity.getEyeY(), entity.getZ(), 12, 0.35D, 0.45D, 0.35D, 0.02D);
		}
	}

	private static void damageCloak(LivingEntity entity, CloakStack cloakStack) {
		cloakStack.stack.damage(1, entity, wearer -> {
			if (wearer instanceof ServerPlayerEntity player && cloakStack.hand != null) {
				player.sendToolBreakStatus(cloakStack.hand);
			} else if (cloakStack.slot != null) {
				wearer.sendEquipmentBreakStatus(cloakStack.slot);
			}
		});
	}

	private record CloakStack(ItemStack stack, EquipmentSlot slot, Hand hand) {
	}
}
