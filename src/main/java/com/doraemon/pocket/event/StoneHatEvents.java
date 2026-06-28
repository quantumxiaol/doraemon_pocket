package com.doraemon.pocket.event;

import com.doraemon.pocket.registry.ModItems;
import com.doraemon.pocket.util.GadgetMobRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

public final class StoneHatEvents {
	private static final double IGNORE_RANGE = 36.0D;
	private static final double CLOSE_SUPPRESS_RANGE = 18.0D;
	private static final double PROJECTILE_SUPPRESS_RANGE = 18.0D;
	private static final int PROJECTILE_SUPPRESS_INTERVAL_TICKS = 4;
	private static final int CLOSE_SUPPRESS_INTERVAL_TICKS = 4;
	private static final int WIDE_IGNORE_INTERVAL_TICKS = 20;

	private StoneHatEvents() {
	}

	public static void register() {
		PlayerGadgetTickDispatcher.registerPlayerTick((player, time) -> tickPlayer(player));
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

		long time = player.getServerWorld().getServer().getTicks();
		if (shouldRunScan(player, time, PROJECTILE_SUPPRESS_INTERVAL_TICKS)) {
			discardHostileProjectiles(player);
		}
		if (shouldRunScan(player, time, CLOSE_SUPPRESS_INTERVAL_TICKS)) {
			suppressCloseMobs(player);
		}

		if (!shouldRunScan(player, time, WIDE_IGNORE_INTERVAL_TICKS)) {
			return;
		}

		Box box = player.getBoundingBox().expand(IGNORE_RANGE);
		player.getWorld().getOtherEntities(player, box, entity -> entity instanceof MobEntity)
				.forEach(entity -> suppressMob((MobEntity) entity, player));
	}

	private static boolean shouldRunScan(ServerPlayerEntity player, long time, int interval) {
		return (time + player.getId()) % interval == 0;
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

		if (source.isIn(DamageTypeTags.IS_EXPLOSION) && (sourceEntity instanceof MobEntity || source.getAttacker() instanceof MobEntity)) {
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

	private static void discardHostileProjectiles(ServerPlayerEntity player) {
		Box box = player.getBoundingBox().expand(PROJECTILE_SUPPRESS_RANGE);
		player.getWorld().getOtherEntities(player, box, entity -> entity instanceof ProjectileEntity projectile && projectile.getOwner() instanceof MobEntity)
				.forEach(Entity::discard);
	}

	private static void suppressCloseMobs(ServerPlayerEntity player) {
		Box box = player.getBoundingBox().expand(CLOSE_SUPPRESS_RANGE);
		player.getWorld().getOtherEntities(player, box, entity -> entity instanceof MobEntity)
				.forEach(entity -> suppressMob((MobEntity) entity, player));
	}

	private static void suppressMob(MobEntity mob, ServerPlayerEntity player) {
		GadgetMobRules.suppressMobForStoneHat(mob, player, CLOSE_SUPPRESS_RANGE * CLOSE_SUPPRESS_RANGE);
	}
}
