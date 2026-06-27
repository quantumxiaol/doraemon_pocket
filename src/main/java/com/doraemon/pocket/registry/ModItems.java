package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.item.AdaptationLightItem;
import com.doraemon.pocket.item.AirCannonItem;
import com.doraemon.pocket.item.ApartmentTreeItem;
import com.doraemon.pocket.item.AnywhereDoorItem;
import com.doraemon.pocket.item.BambooCopterItem;
import com.doraemon.pocket.item.CloudHardeningGasItem;
import com.doraemon.pocket.item.CoconutItem;
import com.doraemon.pocket.item.DepletedRadarSwordItem;
import com.doraemon.pocket.item.DevilsPassportItem;
import com.doraemon.pocket.item.DodgeCloakItem;
import com.doraemon.pocket.item.FourDimensionalPocketItem;
import com.doraemon.pocket.item.MomotaroDumplingItem;
import com.doraemon.pocket.item.PassLoopItem;
import com.doraemon.pocket.item.PlantModificationLiquidItem;
import com.doraemon.pocket.item.RadarSwordItem;
import com.doraemon.pocket.item.ShockGunItem;
import com.doraemon.pocket.item.ShadowCuttingScissorsItem;
import com.doraemon.pocket.item.ShadowGlueItem;
import com.doraemon.pocket.item.StoneHatItem;
import com.doraemon.pocket.item.TimeClothItem;
import com.doraemon.pocket.item.TranslationGummyItem;
import com.doraemon.pocket.item.WeatherCardItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
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
	public static final Item ANYWHERE_DOOR = register(
			"anywhere_door",
			new AnywhereDoorItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE))
	);
	public static final Item PASS_LOOP = register(
			"pass_loop",
			new PassLoopItem(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON))
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
	public static final Item PLANT_MODIFICATION_LIQUID = register(
			"plant_modification_liquid",
			new PlantModificationLiquidItem(new FabricItemSettings().maxCount(16).rarity(Rarity.UNCOMMON))
	);
	public static final Item COCONUT = register(
			"coconut",
			new CoconutItem(new FabricItemSettings().maxCount(64).rarity(Rarity.UNCOMMON))
	);
	public static final Item APARTMENT_TREE = register(
			"apartment_tree",
			new ApartmentTreeItem(ModBlocks.APARTMENT_TREE_SAPLING, new FabricItemSettings().maxCount(16).rarity(Rarity.RARE))
	);
	public static final Item WEATHER_BOX = register(
			"weather_box",
			new BlockItem(ModBlocks.WEATHER_BOX, new FabricItemSettings().maxCount(64).rarity(Rarity.UNCOMMON))
	);
	public static final Item WEATHER_CARD_CLEAR = register(
			"weather_card_clear",
			new WeatherCardItem(WeatherCardItem.WeatherMode.CLEAR, new FabricItemSettings().maxCount(16).rarity(Rarity.UNCOMMON))
	);
	public static final Item WEATHER_CARD_RAIN = register(
			"weather_card_rain",
			new WeatherCardItem(WeatherCardItem.WeatherMode.RAIN, new FabricItemSettings().maxCount(16).rarity(Rarity.UNCOMMON))
	);
	public static final Item WEATHER_CARD_THUNDER = register(
			"weather_card_thunder",
			new WeatherCardItem(WeatherCardItem.WeatherMode.THUNDER, new FabricItemSettings().maxCount(16).rarity(Rarity.UNCOMMON))
	);
	public static final Item WEATHER_CARD_SNOW = register(
			"weather_card_snow",
			new WeatherCardItem(WeatherCardItem.WeatherMode.SNOW, new FabricItemSettings().maxCount(16).rarity(Rarity.UNCOMMON))
	);
	public static final Item TRANSLATION_GUMMY = register(
			"translation_gummy",
			new TranslationGummyItem(new FabricItemSettings().maxCount(16).food(new FoodComponent.Builder().hunger(2).saturationModifier(0.25F).alwaysEdible().build()).rarity(Rarity.UNCOMMON))
	);
	public static final Item FOUR_DIMENSIONAL_POCKET = register(
			"four_dimensional_pocket",
			new FourDimensionalPocketItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE))
	);
	public static final Item GOURMET_TABLE_CLOTH = register(
			"gourmet_table_cloth",
			new BlockItem(ModBlocks.GOURMET_TABLE_CLOTH, new FabricItemSettings().maxCount(16).rarity(Rarity.UNCOMMON))
	);
	public static final Item SHADOW_SCISSORS = register(
			"shadow_scissors",
			new ShadowCuttingScissorsItem(new FabricItemSettings().maxCount(1).maxDamage(64).rarity(Rarity.RARE))
	);
	public static final Item SHADOW_GLUE = register(
			"shadow_glue",
			new ShadowGlueItem(new FabricItemSettings().maxCount(1).maxDamage(64).rarity(Rarity.UNCOMMON))
	);
	public static final Item DEVILS_PASSPORT = register(
			"devils_passport",
			new DevilsPassportItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE))
	);
	public static final Item CLOUD_HARDENING_GAS = register(
			"cloud_hardening_gas",
			new CloudHardeningGasItem(new FabricItemSettings().maxCount(1).maxDamage(64).rarity(Rarity.UNCOMMON))
	);
	public static final Item SOLIDIFIED_CLOUD = register(
			"solidified_cloud",
			new BlockItem(ModBlocks.SOLIDIFIED_CLOUD, new FabricItemSettings().maxCount(64).rarity(Rarity.UNCOMMON))
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
