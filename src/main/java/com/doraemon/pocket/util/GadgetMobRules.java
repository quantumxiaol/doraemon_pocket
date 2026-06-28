package com.doraemon.pocket.util;

import com.doraemon.pocket.item.DevilsPassportItem;
import com.doraemon.pocket.registry.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class GadgetMobRules {
	private GadgetMobRules() {
	}

	public static boolean hasDevilsPassport(PlayerEntity player) {
		return DevilsPassportItem.isActive(player);
	}

	public static boolean hasStoneHat(PlayerEntity player) {
		return player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.STONE_HAT);
	}

	public static boolean shouldRejectMobTarget(PlayerEntity player) {
		return hasDevilsPassport(player) || hasStoneHat(player);
	}

	public static boolean isHostileToPlayer(MobEntity mob, PlayerEntity player) {
		return mob.getTarget() == player || mob.getAttacker() == player || isAngryAt(mob, player);
	}

	public static void pardonMob(MobEntity mob, PlayerEntity player) {
		clearDirectHostility(mob, player);
		clearBrainHostility(mob);
	}

	public static void suppressMobForStoneHat(MobEntity mob, PlayerEntity player, double creeperSuppressRangeSquared) {
		boolean primedCreeperNearPlayer = mob instanceof CreeperEntity creeper
				&& mob.squaredDistanceTo(player) <= creeperSuppressRangeSquared
				&& (creeper.getFuseSpeed() > 0 || creeper.isIgnited());

		if (!isHostileToPlayer(mob, player) && !primedCreeperNearPlayer) {
			return;
		}

		clearDirectHostility(mob, player);
		clearBrainHostility(mob);
		mob.setAttacking(false);
		mob.clearActiveItem();
		mob.getNavigation().stop();

		if (mob instanceof CreeperEntity creeper) {
			creeper.setFuseSpeed(-1);
		}
	}

	private static boolean isAngryAt(MobEntity mob, PlayerEntity player) {
		return mob instanceof Angerable angerable && player.getUuid().equals(angerable.getAngryAt());
	}

	private static void clearDirectHostility(MobEntity mob, PlayerEntity player) {
		if (mob.getTarget() == player) {
			mob.setTarget(null);
		}
		if (mob.getAttacker() == player) {
			mob.setAttacker(null);
		}
		if (mob instanceof Angerable angerable && player.getUuid().equals(angerable.getAngryAt())) {
			angerable.stopAnger();
			angerable.setTarget(null);
			angerable.setAttacker(null);
		}
	}

	private static void clearBrainHostility(MobEntity mob) {
		if (mob instanceof AbstractPiglinEntity piglin) {
			piglin.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
			piglin.getBrain().forget(MemoryModuleType.ANGRY_AT);
			piglin.getBrain().forget(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER);
		}
		if (mob instanceof WardenEntity warden) {
			warden.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
			warden.getBrain().forget(MemoryModuleType.ANGRY_AT);
		}
	}
}
