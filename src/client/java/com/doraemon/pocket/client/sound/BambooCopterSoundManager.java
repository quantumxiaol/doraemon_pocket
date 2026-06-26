package com.doraemon.pocket.client.sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.doraemon.pocket.item.BambooCopterItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class BambooCopterSoundManager {
	private static final int FLAP_INTERVAL_TICKS = 3;
	private static final float FLAP_VOLUME = 0.22F;
	private static final float FLAP_BASE_PITCH = 1.88F;
	private static final Map<UUID, BambooCopterLoopSound> LOOP_SOUNDS = new HashMap<>();
	private static final Map<UUID, Integer> NEXT_FLAP_TICKS = new HashMap<>();

	private BambooCopterSoundManager() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(BambooCopterSoundManager::tick);
	}

	public static boolean isFlightSoundActive(AbstractClientPlayerEntity player) {
		if (player == null
				|| player.isRemoved()
				|| !player.isAlive()
				|| !BambooCopterItem.isEquipped(player)
				|| player.hasVehicle()
				|| player.isFallFlying()
				|| player.isClimbing()
				|| player.isTouchingWater()) {
			return false;
		}

		if (!player.isOnGround()) {
			return true;
		}

		return player instanceof ClientPlayerEntity clientPlayer && clientPlayer.input != null && clientPlayer.input.jumping;
	}

	private static void tick(MinecraftClient client) {
		if (client.world == null || client.player == null) {
			stopAll();
			return;
		}

		for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
			tickPlayer(client, player);
		}

		cleanupInactivePlayers(client);
	}

	private static void tickPlayer(MinecraftClient client, AbstractClientPlayerEntity player) {
		UUID playerId = player.getUuid();
		if (!isFlightSoundActive(player)) {
			BambooCopterLoopSound loopSound = LOOP_SOUNDS.get(playerId);
			if (loopSound != null) {
				loopSound.beginStopping();
			}
			NEXT_FLAP_TICKS.remove(playerId);
			return;
		}

		BambooCopterLoopSound loopSound = LOOP_SOUNDS.get(playerId);
		if (loopSound == null || loopSound.isDone()) {
			loopSound = new BambooCopterLoopSound(player);
			LOOP_SOUNDS.put(playerId, loopSound);
			client.getSoundManager().play(loopSound);
		}

		int nextFlapTick = NEXT_FLAP_TICKS.getOrDefault(playerId, 0);
		if (player.age >= nextFlapTick) {
			playFlap(client, player);
			NEXT_FLAP_TICKS.put(playerId, player.age + FLAP_INTERVAL_TICKS);
		}
	}

	private static void playFlap(MinecraftClient client, AbstractClientPlayerEntity player) {
		float pitch = FLAP_BASE_PITCH + (player.getRandom().nextFloat() - 0.5F) * 0.12F;
		client.getSoundManager().play(new PositionedSoundInstance(
				SoundEvents.ENTITY_PHANTOM_FLAP,
				SoundCategory.PLAYERS,
				FLAP_VOLUME,
				pitch,
				player.getRandom(),
				player.getX(),
				player.getEyeY() + 0.35D,
				player.getZ()
		));
	}

	private static void cleanupInactivePlayers(MinecraftClient client) {
		Iterator<Map.Entry<UUID, BambooCopterLoopSound>> iterator = LOOP_SOUNDS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, BambooCopterLoopSound> entry = iterator.next();
			AbstractClientPlayerEntity player = findPlayer(client, entry.getKey());
			BambooCopterLoopSound sound = entry.getValue();
			if (player == null || sound.isDone()) {
				sound.beginStopping();
				iterator.remove();
				NEXT_FLAP_TICKS.remove(entry.getKey());
			}
		}
	}

	private static AbstractClientPlayerEntity findPlayer(MinecraftClient client, UUID playerId) {
		if (client.world == null) {
			return null;
		}
		for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
			if (player.getUuid().equals(playerId)) {
				return player;
			}
		}
		return null;
	}

	private static void stopAll() {
		LOOP_SOUNDS.values().forEach(BambooCopterLoopSound::beginStopping);
		LOOP_SOUNDS.clear();
		NEXT_FLAP_TICKS.clear();
	}
}
