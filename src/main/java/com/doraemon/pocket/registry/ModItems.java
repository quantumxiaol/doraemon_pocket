package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.item.BambooCopterItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

public final class ModItems {
	public static final Item BAMBOO_COPTER = register(
			"bamboo_copter",
			new BambooCopterItem(
					ModArmorMaterials.BAMBOO_COPTER,
					ArmorItem.Type.HELMET,
					new FabricItemSettings().maxDamage(600).rarity(Rarity.UNCOMMON)
			)
	);

	private ModItems() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket items.");
	}

	private static Item register(String path, Item item) {
		return Registry.register(Registries.ITEM, DoraemonPocket.id(path), item);
	}
}
