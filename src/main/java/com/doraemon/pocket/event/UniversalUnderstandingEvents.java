package com.doraemon.pocket.event;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

public final class UniversalUnderstandingEvents {
	private static final int CHECK_INTERVAL_TICKS = 10;
	private static final double CALM_RANGE = 24.0D;

	private UniversalUnderstandingEvents() {
	}

	public static void register() {
		PlayerGadgetTickDispatcher.registerPlayerTick((player, time) -> {
			if (time % CHECK_INTERVAL_TICKS != 0) {
				return;
			}
			if (player.hasStatusEffect(ModStatusEffects.UNIVERSAL_UNDERSTANDING)) {
				calmNearbyMobs(player);
			}
		});
	}

	private static void calmNearbyMobs(ServerPlayerEntity player) {
		Box box = player.getBoundingBox().expand(CALM_RANGE);
		for (EndermanEntity enderman : player.getWorld().getEntitiesByClass(EndermanEntity.class, box, entity -> true)) {
			calmAngerable(enderman, player);
		}
		for (AbstractPiglinEntity piglin : player.getWorld().getEntitiesByClass(AbstractPiglinEntity.class, box, entity -> true)) {
			if (piglin.getTarget() == player) {
				piglin.setTarget(null);
			}
			piglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
			piglin.getBrain().forget(MemoryModuleType.ANGRY_AT);
			piglin.getBrain().forget(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
		}
	}

	private static void calmAngerable(EndermanEntity enderman, ServerPlayerEntity player) {
		if (enderman.getTarget() == player) {
			enderman.setTarget(null);
		}
		Angerable angerable = enderman;
		if (player.getUuid().equals(angerable.getAngryAt())) {
			angerable.stopAnger();
		}
		if (enderman.getAttacker() == player) {
			enderman.setAttacker((LivingEntity) null);
		}
	}
}
