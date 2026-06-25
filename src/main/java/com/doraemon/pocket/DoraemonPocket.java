package com.doraemon.pocket;

import com.doraemon.pocket.event.BambooCopterTickHandler;
import com.doraemon.pocket.network.DoraemonPackets;
import com.doraemon.pocket.registry.ModItemGroups;
import com.doraemon.pocket.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoraemonPocket implements ModInitializer {
	public static final String MOD_ID = "doraemon_pocket";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();
		ModItemGroups.register();
		DoraemonPackets.registerServerReceivers();
		BambooCopterTickHandler.register();

		LOGGER.info("Doraemon Pocket initialized.");
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
