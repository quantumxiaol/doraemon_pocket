package com.doraemon.pocket.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class SpatialPhaseManager {
	private static final Map<UUID, EnumSet<PhaseSource>> ACTIVE_SOURCES = new HashMap<>();
	private static final Map<UUID, FlightState> SAVED_FLIGHT_STATES = new HashMap<>();
	private static final Map<UUID, Vec3d> LAST_VALID_POSITIONS = new HashMap<>();

	private SpatialPhaseManager() {
	}

	public static void enable(ServerPlayerEntity player, PhaseSource source) {
		EnumSet<PhaseSource> sources = ACTIVE_SOURCES.computeIfAbsent(player.getUuid(), uuid -> EnumSet.noneOf(PhaseSource.class));
		boolean wasInactive = sources.isEmpty();
		sources.add(source);
		player.noClip = true;

		if (wasInactive) {
			SAVED_FLIGHT_STATES.put(player.getUuid(), new FlightState(player.getAbilities().allowFlying, player.getAbilities().flying));
			LAST_VALID_POSITIONS.put(player.getUuid(), player.getPos());
			player.getAbilities().allowFlying = true;
			player.getAbilities().flying = true;
			player.sendAbilitiesUpdate();
		}
	}

	public static void disable(ServerPlayerEntity player, PhaseSource source) {
		EnumSet<PhaseSource> sources = ACTIVE_SOURCES.get(player.getUuid());
		if (sources != null) {
			sources.remove(source);
			if (sources.isEmpty()) {
				ACTIVE_SOURCES.remove(player.getUuid());
			}
		}

		if (!isActive(player)) {
			if (!player.isSpectator()) {
				player.noClip = false;
			}
			restoreFlight(player);
			LAST_VALID_POSITIONS.remove(player.getUuid());
		}
	}

	public static boolean isActive(LivingEntity entity) {
		EnumSet<PhaseSource> sources = ACTIVE_SOURCES.get(entity.getUuid());
		return sources != null && !sources.isEmpty();
	}

	public static boolean isActive(LivingEntity entity, PhaseSource source) {
		EnumSet<PhaseSource> sources = ACTIVE_SOURCES.get(entity.getUuid());
		return sources != null && sources.contains(source);
	}

	public static void clear() {
		ACTIVE_SOURCES.clear();
		SAVED_FLIGHT_STATES.clear();
		LAST_VALID_POSITIONS.clear();
	}

	public static void rememberValidPosition(ServerPlayerEntity player) {
		LAST_VALID_POSITIONS.put(player.getUuid(), player.getPos());
	}

	public static void restoreLastValidPosition(ServerPlayerEntity player) {
		Vec3d position = LAST_VALID_POSITIONS.get(player.getUuid());
		if (position == null) {
			return;
		}

		player.teleport(player.getServerWorld(), position.x, position.y, position.z, player.getYaw(), player.getPitch());
		player.setVelocity(Vec3d.ZERO);
		player.velocityModified = true;
	}

	private static void restoreFlight(ServerPlayerEntity player) {
		FlightState state = SAVED_FLIGHT_STATES.remove(player.getUuid());
		if (state == null || player.isSpectator()) {
			return;
		}

		player.getAbilities().allowFlying = state.allowFlying();
		player.getAbilities().flying = state.flying();
		player.sendAbilitiesUpdate();
	}

	public enum PhaseSource {
		UNDERGROUND_SWIM,
		PASS_THROUGH_CAP
	}

	private record FlightState(boolean allowFlying, boolean flying) {
	}
}
