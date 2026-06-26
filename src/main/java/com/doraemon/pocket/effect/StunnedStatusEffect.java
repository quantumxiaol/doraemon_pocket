package com.doraemon.pocket.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

public class StunnedStatusEffect extends StatusEffect {
	public StunnedStatusEffect() {
		super(StatusEffectCategory.HARMFUL, 0x6EC7FF);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		return true;
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		entity.setJumping(false);
		Vec3d velocity = entity.getVelocity();
		entity.setVelocity(velocity.x * 0.05D, velocity.y, velocity.z * 0.05D);
		entity.velocityModified = true;

		if (entity instanceof MobEntity mob) {
			mob.getNavigation().stop();
			mob.setTarget(null);
			mob.setAttacking(false);
		}
		entity.setAttacker(null);

		if (entity instanceof Angerable angerable) {
			angerable.stopAnger();
			angerable.setTarget(null);
			angerable.setAttacker(null);
		}
	}
}
