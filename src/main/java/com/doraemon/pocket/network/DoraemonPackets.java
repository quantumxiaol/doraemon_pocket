package com.doraemon.pocket.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.item.BambooCopterItem;
import com.doraemon.pocket.item.FourDimensionalPocketItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class DoraemonPackets {
	public static final Identifier BAMBOO_COPTER_CONTROL = DoraemonPocket.id("bamboo_copter_control");
	public static final Identifier OPEN_FOUR_DIMENSIONAL_POCKET = DoraemonPocket.id("open_four_dimensional_pocket");

	private static final int BAMBOO_COPTER_CONTROL_TIMEOUT_TICKS = 30;
	private static final Map<UUID, TimedBambooCopterControl> BAMBOO_COPTER_CONTROLS = new ConcurrentHashMap<>();

	private DoraemonPackets() {
	}

	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(BAMBOO_COPTER_CONTROL, (server, player, handler, buf, responseSender) -> {
			BambooCopterControl control = BambooCopterControl.read(buf);
			server.execute(() -> {
				if (!BambooCopterItem.isEquipped(player) || player.isSpectator()) {
					forgetBambooCopterControl(player.getUuid());
					return;
				}
				if (!player.isRemoved()) {
					BAMBOO_COPTER_CONTROLS.put(player.getUuid(), new TimedBambooCopterControl(control, server.getTicks()));
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(OPEN_FOUR_DIMENSIONAL_POCKET, (server, player, handler, buf, responseSender) ->
				server.execute(() -> FourDimensionalPocketItem.openFirst(player))
		);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> forgetBambooCopterControl(handler.player.getUuid()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> BAMBOO_COPTER_CONTROLS.clear());
	}

	public static BambooCopterControl getBambooCopterControl(ServerPlayerEntity player) {
		TimedBambooCopterControl timedControl = BAMBOO_COPTER_CONTROLS.get(player.getUuid());
		if (timedControl == null) {
			return BambooCopterControl.IDLE;
		}

		if (player.getServerWorld().getServer().getTicks() - timedControl.receivedAtTick() > BAMBOO_COPTER_CONTROL_TIMEOUT_TICKS) {
			BAMBOO_COPTER_CONTROLS.remove(player.getUuid());
			return BambooCopterControl.IDLE;
		}

		return timedControl.control();
	}

	public static void forgetBambooCopterControl(UUID playerId) {
		BAMBOO_COPTER_CONTROLS.remove(playerId);
	}

	private record TimedBambooCopterControl(BambooCopterControl control, long receivedAtTick) {
	}
}
