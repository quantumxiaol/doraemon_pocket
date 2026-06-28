package com.doraemon.pocket.event;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerGadgetTickDispatcher {
	private static final List<PlayerTick> PLAYER_TICKS = new ArrayList<>();
	private static final List<ServerTick> SERVER_TICKS = new ArrayList<>();
	private static boolean registered = false;

	private PlayerGadgetTickDispatcher() {
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long time = server.getTicks();
			for (ServerTick tick : SERVER_TICKS) {
				tick.tick(server, time);
			}

			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				for (PlayerTick tick : PLAYER_TICKS) {
					tick.tick(player, time);
				}
			}
		});
	}

	public static void registerPlayerTick(PlayerTick tick) {
		PLAYER_TICKS.add(tick);
	}

	public static void registerServerTick(ServerTick tick) {
		SERVER_TICKS.add(tick);
	}

	@FunctionalInterface
	public interface PlayerTick {
		void tick(ServerPlayerEntity player, long time);
	}

	@FunctionalInterface
	public interface ServerTick {
		void tick(MinecraftServer server, long time);
	}
}
