package com.doraemon.pocket.client.input;

import com.doraemon.pocket.item.BambooCopterItem;
import com.doraemon.pocket.network.BambooCopterControl;
import com.doraemon.pocket.network.DoraemonPackets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

public final class BambooCopterInputHandler {
	private static boolean sentIdleControl = true;

	private BambooCopterInputHandler() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(BambooCopterInputHandler::tick);
	}

	private static void tick(MinecraftClient client) {
		if (client.player == null || client.getNetworkHandler() == null) {
			sentIdleControl = true;
			return;
		}

		if (BambooCopterItem.isEquipped(client.player)) {
			send(BambooCopterControl.fromInput(
					client.player.input.jumping,
					client.player.input.sneaking,
					client.player.input.movementForward,
					client.player.input.movementSideways,
					client.player.getYaw()
			));
			sentIdleControl = false;
			return;
		}

		if (!sentIdleControl) {
			send(BambooCopterControl.IDLE);
			sentIdleControl = true;
		}
	}

	private static void send(BambooCopterControl control) {
		PacketByteBuf buf = PacketByteBufs.create();
		control.write(buf);
		ClientPlayNetworking.send(DoraemonPackets.BAMBOO_COPTER_CONTROL, buf);
	}
}
