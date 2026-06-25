package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItemGroups {
	public static final ItemGroup DORAEMON_TOOLS = Registry.register(
			Registries.ITEM_GROUP,
			DoraemonPocket.id("tools"),
			FabricItemGroup.builder()
					.displayName(Text.translatable("itemGroup.doraemon_pocket.tools"))
					.icon(() -> new ItemStack(ModItems.BAMBOO_COPTER))
					.entries((context, entries) -> entries.add(ModItems.BAMBOO_COPTER))
					.build()
	);

	private ModItemGroups() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket item groups.");
	}
}
