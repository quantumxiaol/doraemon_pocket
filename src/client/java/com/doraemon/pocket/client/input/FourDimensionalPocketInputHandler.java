package com.doraemon.pocket.client.input;

import com.doraemon.pocket.network.DoraemonPackets;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class FourDimensionalPocketInputHandler {
	private static KeyBinding openPocketKey;

	private FourDimensionalPocketInputHandler() {
	}

	public static void register() {
		openPocketKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.doraemon_pocket.open_four_dimensional_pocket",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				"category.doraemon_pocket.keys"
		));
		ClientTickEvents.END_CLIENT_TICK.register(FourDimensionalPocketInputHandler::tick);
	}

	private static void tick(MinecraftClient client) {
		while (openPocketKey.wasPressed()) {
			if (client.player != null && client.getNetworkHandler() != null && client.currentScreen == null) {
				ClientPlayNetworking.send(DoraemonPackets.OPEN_FOUR_DIMENSIONAL_POCKET, PacketByteBufs.create());
			}
		}
	}
}
