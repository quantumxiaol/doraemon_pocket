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
	private static final int HEARTBEAT_INTERVAL_TICKS = 10;

	private static boolean sentIdleControl = true;
	private static BambooCopterControl lastSentControl = BambooCopterControl.IDLE;
	private static int ticksSinceLastSend = HEARTBEAT_INTERVAL_TICKS;

	private BambooCopterInputHandler() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(BambooCopterInputHandler::tick);
	}

	private static void tick(MinecraftClient client) {
		if (client.player == null || client.getNetworkHandler() == null) {
			resetSentState();
			return;
		}

		if (BambooCopterItem.isEquipped(client.player)) {
			BambooCopterControl control = BambooCopterControl.fromInput(
					client.player.input.jumping,
					client.player.input.sneaking,
					client.player.input.movementForward,
					client.player.input.movementSideways
			);
			if (!control.equals(lastSentControl) || ticksSinceLastSend >= HEARTBEAT_INTERVAL_TICKS) {
				send(control);
				lastSentControl = control;
				ticksSinceLastSend = 0;
				sentIdleControl = control.isIdle();
			} else {
				ticksSinceLastSend++;
			}
			return;
		}

		if (!sentIdleControl) {
			send(BambooCopterControl.IDLE);
			resetSentState();
		}
	}

	private static void send(BambooCopterControl control) {
		PacketByteBuf buf = PacketByteBufs.create();
		control.write(buf);
		ClientPlayNetworking.send(DoraemonPackets.BAMBOO_COPTER_CONTROL, buf);
	}

	private static void resetSentState() {
		sentIdleControl = true;
		lastSentControl = BambooCopterControl.IDLE;
		ticksSinceLastSend = HEARTBEAT_INTERVAL_TICKS;
	}
}
