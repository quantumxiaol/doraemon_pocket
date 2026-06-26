package com.doraemon.pocket.event;

import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

public final class StoneHatEvents {
	private static final double IGNORE_RANGE = 36.0D;

	private StoneHatEvents() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerManager().getPlayerList().forEach(StoneHatEvents::tickPlayer));
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(StoneHatEvents::allowDamage);
	}

	private static void tickPlayer(ServerPlayerEntity player) {
		ItemStack hat = player.getEquippedStack(EquipmentSlot.HEAD);
		if (!hat.isOf(ModItems.STONE_HAT)) {
			return;
		}

		if (player.isTouchingWater()) {
			unequipHat(player, hat);
			return;
		}

		if (player.age % 5 != 0) {
			return;
		}

		Box box = player.getBoundingBox().expand(IGNORE_RANGE);
		player.getWorld().getOtherEntities(player, box, entity -> entity instanceof MobEntity)
				.forEach(entity -> makeMobIgnorePlayer((MobEntity) entity, player));
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof PlayerEntity player) || !player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.STONE_HAT)) {
			return true;
		}

		Entity sourceEntity = source.getSource();
		if (sourceEntity instanceof ProjectileEntity projectile && projectile.getOwner() instanceof MobEntity) {
			projectile.discard();
			return false;
		}

		Entity attacker = source.getAttacker();
		if (attacker instanceof MobEntity) {
			return false;
		}

		return true;
	}

	private static void unequipHat(ServerPlayerEntity player, ItemStack hat) {
		ItemStack detachedHat = hat.copy();
		player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
		player.getInventory().offerOrDrop(detachedHat);
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 0.65F, 1.25F);

		if (player.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.SPLASH, player.getX(), player.getEyeY(), player.getZ(), 8, 0.25D, 0.2D, 0.25D, 0.02D);
		}
	}

	private static void makeMobIgnorePlayer(MobEntity mob, ServerPlayerEntity player) {
		boolean targetsPlayer = mob.getTarget() == player;
		boolean attackedByPlayer = mob.getAttacker() == player;
		Angerable angerable = mob instanceof Angerable angerableMob ? angerableMob : null;
		boolean angryAtPlayer = angerable != null && player.getUuid().equals(angerable.getAngryAt());

		if (!targetsPlayer && !attackedByPlayer && !angryAtPlayer) {
			return;
		}

		if (targetsPlayer) {
			mob.setTarget(null);
		}
		if (attackedByPlayer) {
			mob.setAttacker(null);
		}
		mob.setAttacking(false);

		if (angerable != null && angryAtPlayer) {
			angerable.stopAnger();
			angerable.setTarget(null);
			angerable.setAttacker(null);
		}

		if (mob instanceof CreeperEntity creeper) {
			creeper.setFuseSpeed(-1);
			creeper.getNavigation().stop();
		}
	}
}
