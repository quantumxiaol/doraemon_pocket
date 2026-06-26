package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.block.entity.LinkedPortalBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
	public static final BlockEntityType<LinkedPortalBlockEntity> LINKED_PORTAL = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			DoraemonPocket.id("linked_portal"),
			BlockEntityType.Builder.create(LinkedPortalBlockEntity::new, ModBlocks.ANYWHERE_DOOR_PORTAL, ModBlocks.PASS_LOOP_PORTAL).build(null)
	);

	private ModBlockEntities() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket block entities.");
	}
}
