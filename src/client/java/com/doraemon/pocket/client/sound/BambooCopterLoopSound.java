package com.doraemon.pocket.client.sound;

import com.doraemon.pocket.item.BambooCopterItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;

public final class BambooCopterLoopSound extends MovingSoundInstance {
	private static final float TARGET_VOLUME = 0.025F;
	private static final float BASE_PITCH = 0.8F;

	private final AbstractClientPlayerEntity wearer;
	private boolean stopping;

	public BambooCopterLoopSound(AbstractClientPlayerEntity wearer) {
		super(SoundEvents.ENTITY_BEE_LOOP, SoundCategory.PLAYERS, Random.create());
		this.wearer = wearer;
		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.0F;
		this.pitch = BASE_PITCH;
		this.attenuationType = SoundInstance.AttenuationType.LINEAR;
		updatePosition();
	}

	@Override
	public void tick() {
		if (!BambooCopterSoundManager.isFlightSoundActive(wearer)) {
			beginStopping();
		}

		updatePosition();

		if (stopping) {
			volume *= 0.72F;
			if (volume < 0.006F) {
				setDone();
			}
			return;
		}

		volume += (TARGET_VOLUME - volume) * 0.18F;
		float verticalVelocity = (float) wearer.getVelocity().y;
		float targetPitch = BASE_PITCH + Math.max(-0.06F, Math.min(0.10F, verticalVelocity * 0.14F));
		pitch += (targetPitch - pitch) * 0.12F;
	}

	@Override
	public boolean canPlay() {
		return true;
	}

	public void beginStopping() {
		stopping = true;
	}

	private void updatePosition() {
		x = wearer.getX();
		y = wearer.getEyeY() + 0.35D;
		z = wearer.getZ();
	}
}
