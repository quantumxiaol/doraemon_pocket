package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.entity.MomotaroDumplingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEntities {
	public static final EntityType<MomotaroDumplingEntity> MOMOTARO_DUMPLING = Registry.register(
			Registries.ENTITY_TYPE,
			DoraemonPocket.id("momotaro_dumpling"),
			EntityType.Builder.<MomotaroDumplingEntity>create(MomotaroDumplingEntity::new, SpawnGroup.MISC)
					.setDimensions(0.25F, 0.25F)
					.maxTrackingRange(4)
					.trackingTickInterval(10)
					.build(DoraemonPocket.id("momotaro_dumpling").toString())
	);

	private ModEntities() {
	}

	public static void register() {
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket entities.");
	}
}
