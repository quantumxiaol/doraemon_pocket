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
					.entries((context, entries) -> {
						entries.add(ModItems.BAMBOO_COPTER);
						entries.add(ModItems.AIR_CANNON);
						entries.add(ModItems.SHOCK_GUN);
						entries.add(ModItems.DODGE_CLOAK);
						entries.add(ModItems.TIME_CLOTH);
						entries.add(ModItems.ADAPTATION_LIGHT);
						entries.add(ModItems.MOMOTARO_DUMPLING);
						entries.add(ModItems.ANYWHERE_DOOR);
						entries.add(ModItems.PASS_LOOP);
						entries.add(ModItems.STONE_HAT);
						entries.add(ModItems.RADAR_SWORD);
						entries.add(ModItems.DEPLETED_RADAR_SWORD);
						entries.add(ModItems.DORAYAKI);
						entries.add(ModItems.PLANT_MODIFICATION_LIQUID);
						entries.add(ModItems.COCONUT);
						entries.add(ModItems.APARTMENT_TREE);
						entries.add(ModItems.WEATHER_BOX);
						entries.add(ModItems.WEATHER_CARD_CLEAR);
						entries.add(ModItems.WEATHER_CARD_RAIN);
						entries.add(ModItems.WEATHER_CARD_THUNDER);
						entries.add(ModItems.WEATHER_CARD_SNOW);
						entries.add(ModItems.TRANSLATION_GUMMY);
						entries.add(ModItems.FOUR_DIMENSIONAL_POCKET);
						entries.add(ModItems.GOURMET_TABLE_CLOTH);
						entries.add(ModItems.SHADOW_SCISSORS);
						entries.add(ModItems.SHADOW_GLUE);
						entries.add(ModItems.DEVILS_PASSPORT);
						entries.add(ModItems.CLOUD_HARDENING_GAS);
						entries.add(ModItems.SOLIDIFIED_CLOUD);
						entries.add(ModItems.DEEP_SEA_CREAM);
						entries.add(ModItems.FAST_FORWARD_WINDER);
						entries.add(ModItems.WOODCUTTERS_SPRING);
						entries.add(ModItems.CAMPING_CAPSULE);
						entries.add(ModItems.RESTORING_FLASHLIGHT);
					})
					.build()
	);

	private ModItemGroups() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket item groups.");
	}
}
