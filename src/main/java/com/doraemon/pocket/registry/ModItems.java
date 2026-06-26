package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.item.AdaptationLightItem;
import com.doraemon.pocket.item.AirCannonItem;
import com.doraemon.pocket.item.BambooCopterItem;
import com.doraemon.pocket.item.DepletedRadarSwordItem;
import com.doraemon.pocket.item.DodgeCloakItem;
import com.doraemon.pocket.item.MomotaroDumplingItem;
import com.doraemon.pocket.item.RadarSwordItem;
import com.doraemon.pocket.item.ShockGunItem;
import com.doraemon.pocket.item.StoneHatItem;
import com.doraemon.pocket.item.TimeClothItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FoodComponent;
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
					new FabricItemSettings().maxDamage(6000).rarity(Rarity.UNCOMMON)
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
	public static final Item MOMOTARO_DUMPLING = register(
			"momotaro_dumpling",
			new MomotaroDumplingItem(new FabricItemSettings().maxCount(16).rarity(Rarity.UNCOMMON))
	);
	public static final Item STONE_HAT = register(
			"stone_hat",
			new StoneHatItem(
					ModArmorMaterials.STONE_HAT,
					ArmorItem.Type.HELMET,
					new FabricItemSettings().maxDamage(88).rarity(Rarity.UNCOMMON)
			)
	);
	public static final Item RADAR_SWORD = register(
			"radar_sword",
			new RadarSwordItem(ModToolMaterials.RADAR_SWORD, 5, -2.4F, new FabricItemSettings().rarity(Rarity.RARE))
	);
	public static final Item DEPLETED_RADAR_SWORD = register(
			"depleted_radar_sword",
			new DepletedRadarSwordItem(ModToolMaterials.DEPLETED_RADAR_SWORD, 5, -2.4F, new FabricItemSettings().rarity(Rarity.UNCOMMON))
	);
	public static final Item DORAYAKI = register(
			"dorayaki",
			new Item(new FabricItemSettings().maxCount(64).food(new FoodComponent.Builder().hunger(5).saturationModifier(0.6F).build()))
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
