package com.doraemon.pocket.util;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class PhaseVisuals {
	private static final int NIGHT_VISION_REFRESH_TICKS = 260;
	private static final DustParticleEffect FORWARD_GUIDE = new DustParticleEffect(new Vector3f(0.1F, 0.75F, 1.0F), 0.85F);
	private static final DustParticleEffect UP_GUIDE = new DustParticleEffect(new Vector3f(1.0F, 0.9F, 0.18F), 0.65F);

	private PhaseVisuals() {
	}

	public static void refreshNightVision(ServerPlayerEntity player) {
		StatusEffectInstance nightVision = player.getStatusEffect(StatusEffects.NIGHT_VISION);
		if (nightVision == null || nightVision.getDuration() < NIGHT_VISION_REFRESH_TICKS / 2) {
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, NIGHT_VISION_REFRESH_TICKS, 0, false, false, true));
		}
	}

	public static void spawnDirectionGuide(ServerPlayerEntity player) {
		if (player.age % 5 != 0) {
			return;
		}

		Vec3d eye = player.getEyePos();
		Vec3d forward = eye.add(player.getRotationVec(1.0F).normalize().multiply(1.25D));
		player.getServerWorld().spawnParticles(FORWARD_GUIDE, forward.x, forward.y, forward.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		player.getServerWorld().spawnParticles(UP_GUIDE, eye.x, eye.y + 0.75D, eye.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
	}
}
