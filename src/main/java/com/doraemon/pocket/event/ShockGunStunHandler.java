package com.doraemon.pocket.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;

public final class ShockGunStunHandler {
	private static final Map<UUID, StunState> STUNNED_MOBS = new HashMap<>();

	private ShockGunStunHandler() {
	}

	public static void register() {
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			wakeIfStunned(entity);
			return true;
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			Iterator<Map.Entry<UUID, StunState>> iterator = STUNNED_MOBS.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<UUID, StunState> entry = iterator.next();
				StunState state = entry.getValue();
				int remainingTicks = state.remainingTicks - 1;

				if (remainingTicks > 0) {
					entry.setValue(new StunState(remainingTicks, state.wasAiDisabled));
					continue;
				}

				for (var world : server.getWorlds()) {
					Entity entity = world.getEntity(entry.getKey());
					if (entity instanceof MobEntity mob) {
						mob.setAiDisabled(state.wasAiDisabled);
						break;
					}
				}

				iterator.remove();
			}
		});
	}

	public static void stun(MobEntity mob, int ticks) {
		STUNNED_MOBS.compute(mob.getUuid(), (uuid, state) -> {
			if (state == null) {
				return new StunState(ticks, mob.isAiDisabled());
			}

			return new StunState(Math.max(ticks, state.remainingTicks), state.wasAiDisabled);
		});
	}

	public static boolean wakeIfStunned(LivingEntity entity) {
		if (!(entity instanceof MobEntity mob)) {
			return false;
		}

		StunState state = STUNNED_MOBS.remove(mob.getUuid());
		if (state == null) {
			return false;
		}

		mob.setAiDisabled(state.wasAiDisabled);
		mob.removeStatusEffect(StatusEffects.SLOWNESS);
		mob.removeStatusEffect(StatusEffects.JUMP_BOOST);
		mob.removeStatusEffect(StatusEffects.WEAKNESS);
		return true;
	}

	private record StunState(int remainingTicks, boolean wasAiDisabled) {
	}
}
