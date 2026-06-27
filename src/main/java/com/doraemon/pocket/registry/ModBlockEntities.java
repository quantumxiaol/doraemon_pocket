package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import com.doraemon.pocket.block.entity.WeatherBoxBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
	public static final BlockEntityType<LinkedPortalBlockEntity> LINKED_PORTAL = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			DoraemonPocket.id("linked_portal"),
			BlockEntityType.Builder.create(LinkedPortalBlockEntity::new, ModBlocks.ANYWHERE_DOOR_PORTAL, ModBlocks.PASS_LOOP_PORTAL).build(null)
	);
	public static final BlockEntityType<WeatherBoxBlockEntity> WEATHER_BOX = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			DoraemonPocket.id("weather_box"),
			BlockEntityType.Builder.create(WeatherBoxBlockEntity::new, ModBlocks.WEATHER_BOX).build(null)
	);

	private ModBlockEntities() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket block entities.");
	}
}
