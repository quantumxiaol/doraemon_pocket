package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.item.AdaptationLightItem;
import com.doraemon.pocket.item.AirCannonItem;
import com.doraemon.pocket.item.BambooCopterItem;
import com.doraemon.pocket.item.DodgeCloakItem;
import com.doraemon.pocket.item.ShockGunItem;
import com.doraemon.pocket.item.TimeClothItem;
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
	public static final Item AIR_CANNON = register(
			"air_cannon",
			new AirCannonItem(new FabricItemSettings().maxCount(1).maxDamage(250).rarity(Rarity.UNCOMMON))
	);
	public static final Item SHOCK_GUN = register(
			"shock_gun",
			new ShockGunItem(new FabricItemSettings().maxCount(1).maxDamage(180).rarity(Rarity.UNCOMMON))
	);
	public static final Item DODGE_CLOAK = register(
			"dodge_cloak",
			new DodgeCloakItem(
					ModArmorMaterials.DODGE_CLOAK,
					ArmorItem.Type.CHESTPLATE,
					new FabricItemSettings().maxDamage(720).rarity(Rarity.UNCOMMON)
			)
	);
	public static final Item TIME_CLOTH = register(
			"time_cloth",
			new TimeClothItem(new FabricItemSettings().maxCount(1).maxDamage(128).rarity(Rarity.UNCOMMON))
	);
	public static final Item ADAPTATION_LIGHT = register(
			"adaptation_light",
			new AdaptationLightItem(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON))
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
