package com.doraemon.pocket.registry;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.entity.MomotaroDumplingEntity;
import com.doraemon.pocket.entity.ShadowEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
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
	public static final EntityType<ShadowEntity> SHADOW = Registry.register(
			Registries.ENTITY_TYPE,
			DoraemonPocket.id("shadow"),
			EntityType.Builder.<ShadowEntity>create(ShadowEntity::new, SpawnGroup.CREATURE)
					.setDimensions(0.6F, 1.95F)
					.maxTrackingRange(8)
					.trackingTickInterval(3)
					.build(DoraemonPocket.id("shadow").toString())
	);

	private ModEntities() {
	}

	public static void register() {
		FabricDefaultAttributeRegistry.register(SHADOW, ShadowEntity.createAttributes());
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket entities.");
	}
}
