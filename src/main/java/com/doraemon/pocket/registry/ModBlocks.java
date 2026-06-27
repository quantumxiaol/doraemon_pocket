package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.block.ApartmentTreeSaplingBlock;
import com.doraemon.pocket.block.AnywhereDoorPortalBlock;
import com.doraemon.pocket.block.CoconutFruitBlock;
import com.doraemon.pocket.block.LinkedPortalBlock;
import com.doraemon.pocket.block.WeatherBoxBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

public final class ModBlocks {
	public static final AnywhereDoorPortalBlock ANYWHERE_DOOR_PORTAL = register(
			"anywhere_door_portal",
			new AnywhereDoorPortalBlock(doorSettings())
	);
	public static final LinkedPortalBlock PASS_LOOP_PORTAL = register(
			"pass_loop_portal",
			new LinkedPortalBlock(portalSettings(MapColor.YELLOW))
	);
	public static final CoconutFruitBlock COCONUT_FRUIT = register(
			"coconut_fruit",
			new CoconutFruitBlock(coconutSettings())
	);
	public static final ApartmentTreeSaplingBlock APARTMENT_TREE_SAPLING = register(
			"apartment_tree_sapling",
			new ApartmentTreeSaplingBlock(saplingSettings())
	);
	public static final WeatherBoxBlock WEATHER_BOX = register(
			"weather_box",
			new WeatherBoxBlock(machineSettings())
	);

	private ModBlocks() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket blocks.");
	}

	private static AbstractBlock.Settings portalSettings(MapColor mapColor) {
		return AbstractBlock.Settings.create()
				.mapColor(mapColor)
				.noCollision()
				.nonOpaque()
				.strength(0.4F)
				.luminance(state -> 7)
				.sounds(BlockSoundGroup.GLASS)
				.pistonBehavior(PistonBehavior.DESTROY)
				.dropsNothing();
	}

	private static AbstractBlock.Settings doorSettings() {
		return AbstractBlock.Settings.create()
				.mapColor(MapColor.PINK)
				.nonOpaque()
				.strength(0.6F)
				.sounds(BlockSoundGroup.WOOD)
				.pistonBehavior(PistonBehavior.DESTROY)
				.dropsNothing();
	}

	private static AbstractBlock.Settings coconutSettings() {
		return AbstractBlock.Settings.create()
				.mapColor(MapColor.BROWN)
				.noCollision()
				.nonOpaque()
				.strength(0.2F)
				.sounds(BlockSoundGroup.WOOD)
				.pistonBehavior(PistonBehavior.DESTROY)
				.dropsNothing();
	}

	private static AbstractBlock.Settings saplingSettings() {
		return AbstractBlock.Settings.create()
				.mapColor(MapColor.DARK_GREEN)
				.noCollision()
				.nonOpaque()
				.breakInstantly()
				.sounds(BlockSoundGroup.GRASS)
				.pistonBehavior(PistonBehavior.DESTROY)
				.dropsNothing();
	}

	private static AbstractBlock.Settings machineSettings() {
		return AbstractBlock.Settings.create()
				.mapColor(MapColor.LIGHT_BLUE)
				.strength(2.2F, 6.0F)
				.sounds(BlockSoundGroup.METAL)
				.luminance(state -> 4)
				.requiresTool();
	}

	private static <T extends net.minecraft.block.Block> T register(String path, T block) {
		return Registry.register(Registries.BLOCK, DoraemonPocket.id(path), block);
	}
}
