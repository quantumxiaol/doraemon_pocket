package com.doraemon.pocket.client;

import com.doraemon.pocket.client.input.BambooCopterInputHandler;
import com.doraemon.pocket.client.model.BambooCopterModel;
import com.doraemon.pocket.client.model.DodgeCloakModel;
import com.doraemon.pocket.client.render.BambooCopterArmorRenderer;
import com.doraemon.pocket.client.render.DodgeCloakArmorRenderer;
import com.doraemon.pocket.client.render.StoneHatArmorRenderer;
import com.doraemon.pocket.client.sound.BambooCopterSoundManager;
import com.doraemon.pocket.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class DoraemonPocketClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BambooCopterModel.registerLayer();
		DodgeCloakModel.registerLayer();
		BambooCopterArmorRenderer.register();
		DodgeCloakArmorRenderer.register();
		StoneHatArmorRenderer.register();
		BambooCopterInputHandler.register();
		BambooCopterSoundManager.register();
		EntityRendererRegistry.register(ModEntities.MOMOTARO_DUMPLING, FlyingItemEntityRenderer::new);
	}
}
