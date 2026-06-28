package com.doraemon.pocket.client.input;

import com.doraemon.pocket.item.PassThroughCapItem;
import com.doraemon.pocket.registry.ModStatusEffects;
import com.doraemon.pocket.util.PhaseBlockRules;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;

public final class UndergroundSwimInputHandler {
	private static boolean enabledClientPhase = false;
	private static boolean savedClientAllowFlying = false;
	private static boolean savedClientFlying = false;

	private UndergroundSwimInputHandler() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(UndergroundSwimInputHandler::tick);
	}

	private static void tick(MinecraftClient client) {
		if (client.player == null || client.getNetworkHandler() == null) {
			enabledClientPhase = false;
			return;
		}

		if (isInsidePhaseableBlock(client)) {
			enableClientPhase(client);
			return;
		}

		disableClientPhase(client);
	}

	private static void enableClientPhase(MinecraftClient client) {
		if (client.player == null || client.player.isSpectator()) {
			return;
		}
		if (!enabledClientPhase) {
			savedClientAllowFlying = client.player.getAbilities().allowFlying;
			savedClientFlying = client.player.getAbilities().flying;
		}
		client.player.noClip = true;
		client.player.getAbilities().allowFlying = true;
		client.player.getAbilities().flying = true;
		enabledClientPhase = true;
	}

	private static void disableClientPhase(MinecraftClient client) {
		if (!enabledClientPhase) {
			return;
		}
		if (client.player != null && !client.player.isSpectator()) {
			client.player.noClip = false;
			client.player.getAbilities().allowFlying = savedClientAllowFlying;
			client.player.getAbilities().flying = savedClientFlying;
		}
		enabledClientPhase = false;
	}

	private static boolean isInsidePhaseableBlock(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return false;
		}

		Box bodyBox = client.player.getBoundingBox().contract(0.08D);
		return PassThroughCapItem.isEquipped(client.player) && PhaseBlockRules.touchesCapPhaseableSolidBlock(client.world, bodyBox)
				|| client.player.hasStatusEffect(ModStatusEffects.UNDERGROUND_SWIMMING) && PhaseBlockRules.touchesSwimmableSolidBlock(client.world, bodyBox);
	}
}
