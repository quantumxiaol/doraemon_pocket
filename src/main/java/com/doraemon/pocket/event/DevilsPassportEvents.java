package com.doraemon.pocket.event;

import com.doraemon.pocket.util.GadgetMobRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

public final class DevilsPassportEvents {
	private static final int CHECK_INTERVAL_TICKS = 20;
	private static final double PARDON_RANGE = 24.0D;

	private DevilsPassportEvents() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(DevilsPassportEvents::allowDamage);
		PlayerGadgetTickDispatcher.registerPlayerTick((player, time) -> {
			if (time % CHECK_INTERVAL_TICKS != 0) {
				return;
			}
			if (GadgetMobRules.hasDevilsPassport(player)) {
				pardonNearby(player);
			}
		});
	}

	private static boolean allowDamage(net.minecraft.entity.LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayerEntity player) || !GadgetMobRules.hasDevilsPassport(player)) {
			return true;
		}

		Entity attacker = source.getAttacker();
		Entity sourceEntity = source.getSource();
		if (attacker instanceof MobEntity mob) {
			GadgetMobRules.pardonMob(mob, player);
			return false;
		}
		if (sourceEntity instanceof ProjectileEntity projectile && projectile.getOwner() instanceof MobEntity mob) {
			GadgetMobRules.pardonMob(mob, player);
			projectile.discard();
			return false;
		}
		return true;
	}

	private static void pardonNearby(ServerPlayerEntity player) {
		Box box = player.getBoundingBox().expand(PARDON_RANGE);
		for (HostileEntity hostile : player.getWorld().getEntitiesByClass(HostileEntity.class, box, entity -> GadgetMobRules.isHostileToPlayer(entity, player))) {
			GadgetMobRules.pardonMob(hostile, player);
		}

		for (IronGolemEntity golem : player.getWorld().getEntitiesByClass(IronGolemEntity.class, box, entity -> GadgetMobRules.isHostileToPlayer(entity, player))) {
			GadgetMobRules.pardonMob(golem, player);
		}
	}
}
