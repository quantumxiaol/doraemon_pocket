package com.doraemon.pocket.client;

import com.doraemon.pocket.client.input.BambooCopterInputHandler;
import com.doraemon.pocket.client.model.BambooCopterModel;
import com.doraemon.pocket.client.render.BambooCopterArmorRenderer;
import net.fabricmc.api.ClientModInitializer;

public class DoraemonPocketClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BambooCopterModel.registerLayer();
		BambooCopterArmorRenderer.register();
		BambooCopterInputHandler.register();
	}
}
