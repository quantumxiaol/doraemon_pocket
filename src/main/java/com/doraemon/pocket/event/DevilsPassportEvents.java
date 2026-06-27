package com.doraemon.pocket.event;

import com.doraemon.pocket.item.DevilsPassportItem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

public final class DevilsPassportEvents {
	private static final int CHECK_INTERVAL_TICKS = 1;
	private static final double PARDON_RANGE = 48.0D;

	private DevilsPassportEvents() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DevilsPassportEvents::allowDamage);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTicks() % CHECK_INTERVAL_TICKS != 0) {
				return;
			}

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				if (DevilsPassportItem.isActive(player)) {
					pardonNearby(player);
				}
			}
		});
	}

	private static boolean allowDamage(net.minecraft.entity.LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayerEntity player) || !DevilsPassportItem.isActive(player)) {
			return true;
		}

		Entity attacker = source.getAttacker();
		Entity sourceEntity = source.getSource();
		if (attacker instanceof MobEntity mob) {
			clearTarget(mob, player);
			return false;
		}
		if (attacker instanceof IronGolemEntity golem) {
			clearTarget(golem, player);
			return false;
		}
		if (sourceEntity instanceof ProjectileEntity projectile && projectile.getOwner() instanceof MobEntity mob) {
			clearTarget(mob, player);
			projectile.discard();
			return false;
		}
		return true;
	}

	private static void pardonNearby(ServerPlayerEntity player) {
		Box box = player.getBoundingBox().expand(PARDON_RANGE);
		for (HostileEntity hostile : player.getWorld().getEntitiesByClass(HostileEntity.class, box, entity -> entity.getTarget() == player || entity.getAttacker() == player)) {
			clearTarget(hostile, player);
			if (hostile instanceof Angerable angerable && player.getUuid().equals(angerable.getAngryAt())) {
				angerable.stopAnger();
			}
			if (hostile instanceof AbstractPiglinEntity piglin) {
				clearPiglinMemory(piglin);
			}
			if (hostile instanceof WardenEntity warden) {
				warden.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
				warden.getBrain().forget(MemoryModuleType.ANGRY_AT);
			}
			if (hostile instanceof EndermanEntity enderman && enderman.getTarget() == player) {
				enderman.setTarget(null);
			}
		}

		for (IronGolemEntity golem : player.getWorld().getEntitiesByClass(IronGolemEntity.class, box, entity -> entity.getTarget() == player || entity.getAttacker() == player)) {
			clearTarget(golem, player);
		}
	}

	private static void clearTarget(MobEntity entity, ServerPlayerEntity player) {
		if (entity.getTarget() == player) {
			entity.setTarget(null);
		}
		if (entity.getAttacker() == player) {
			entity.setAttacker(null);
		}
	}

	private static void clearPiglinMemory(AbstractPiglinEntity piglin) {
		piglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
		piglin.getBrain().forget(MemoryModuleType.ANGRY_AT);
		piglin.getBrain().forget(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
	}
}
