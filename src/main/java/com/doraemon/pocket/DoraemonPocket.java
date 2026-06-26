package com.doraemon.pocket;

import com.doraemon.pocket.event.BambooCopterTickHandler;
import com.doraemon.pocket.command.ModCommands;
import com.doraemon.pocket.event.AnywhereDoorTickHandler;
import com.doraemon.pocket.event.DodgeCloakEvents;
import com.doraemon.pocket.event.EnvironmentalAdaptationEvents;
import com.doraemon.pocket.event.MomotaroObedienceHandler;
import com.doraemon.pocket.event.RadarSwordEvents;
import com.doraemon.pocket.event.ShockGunStunHandler;
import com.doraemon.pocket.event.StoneHatEvents;
import com.doraemon.pocket.event.TimeClothUseHandler;
import com.doraemon.pocket.network.DoraemonPackets;
import com.doraemon.pocket.registry.ModBlockEntities;
import com.doraemon.pocket.registry.ModBlocks;
import com.doraemon.pocket.registry.ModEntities;
import com.doraemon.pocket.registry.ModItemGroups;
import com.doraemon.pocket.registry.ModItems;
import com.doraemon.pocket.registry.ModStatusEffects;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoraemonPocket implements ModInitializer {
	public static final String MOD_ID = "doraemon_pocket";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModStatusEffects.register();
		ModBlocks.register();
		ModBlockEntities.register();
		ModItems.register();
		ModEntities.register();
		ModItemGroups.register();
		ModCommands.register();
		DoraemonPackets.registerServerReceivers();
		AnywhereDoorTickHandler.register();
		BambooCopterTickHandler.register();
		ShockGunStunHandler.register();
		TimeClothUseHandler.register();
		MomotaroObedienceHandler.register();
		StoneHatEvents.register();
		RadarSwordEvents.register();
		EnvironmentalAdaptationEvents.register();
		DodgeCloakEvents.register();

		LOGGER.info("Doraemon Pocket initialized.");
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
