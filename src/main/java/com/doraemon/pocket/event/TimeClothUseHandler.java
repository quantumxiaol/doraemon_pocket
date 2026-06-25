package com.doraemon.pocket.event;

import com.doraemon.pocket.item.TimeClothItem;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

public final class TimeClothUseHandler {
	private TimeClothUseHandler() {
	}

	public static void register() {
		UseItemCallback.EVENT.register((player, world, hand) -> TimeClothItem.tryRepairFromHands(world, player, hand, true));
	}
}
