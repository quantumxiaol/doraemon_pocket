package com.doraemon.pocket.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.doraemon.pocket.DoraemonPocket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public final class DoraemonPackets {
	public static final Identifier BAMBOO_COPTER_CONTROL = DoraemonPocket.id("bamboo_copter_control");

	private static final Map<UUID, BambooCopterControl> BAMBOO_COPTER_CONTROLS = new ConcurrentHashMap<>();

	private DoraemonPackets() {
	}

	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(BAMBOO_COPTER_CONTROL, (server, player, handler, buf, responseSender) -> {
			BambooCopterControl control = BambooCopterControl.read(buf);
			server.execute(() -> BAMBOO_COPTER_CONTROLS.put(player.getUuid(), control));
		});
	}

	public static BambooCopterControl getBambooCopterControl(UUID playerId) {
		return BAMBOO_COPTER_CONTROLS.getOrDefault(playerId, BambooCopterControl.IDLE);
	}

	public static void forgetBambooCopterControl(UUID playerId) {
		BAMBOO_COPTER_CONTROLS.remove(playerId);
	}
}
