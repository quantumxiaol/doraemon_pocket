package com.doraemon.pocket.client;

import com.doraemon.pocket.client.input.BambooCopterInputHandler;
import com.doraemon.pocket.client.input.FourDimensionalPocketInputHandler;
import com.doraemon.pocket.client.model.BambooCopterModel;
import com.doraemon.pocket.client.model.DodgeCloakModel;
import com.doraemon.pocket.client.render.BambooCopterArmorRenderer;
import com.doraemon.pocket.client.render.DodgeCloakArmorRenderer;
import com.doraemon.pocket.client.render.LinkedPortalBlockEntityRenderer;
import com.doraemon.pocket.client.render.ShadowEntityRenderer;
import com.doraemon.pocket.client.render.StoneHatArmorRenderer;
import com.doraemon.pocket.client.screen.FourDimensionalPocketScreen;
import com.doraemon.pocket.client.sound.BambooCopterSoundManager;
import com.doraemon.pocket.registry.ModBlockEntities;
import com.doraemon.pocket.registry.ModBlocks;
import com.doraemon.pocket.registry.ModEntities;
import com.doraemon.pocket.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public class DoraemonPocketClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BambooCopterModel.registerLayer();
		DodgeCloakModel.registerLayer();
		BambooCopterArmorRenderer.register();
		DodgeCloakArmorRenderer.register();
		StoneHatArmorRenderer.register();
		BlockEntityRendererFactories.register(ModBlockEntities.LINKED_PORTAL, LinkedPortalBlockEntityRenderer::new);
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ANYWHERE_DOOR_PORTAL, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PASS_LOOP_PORTAL, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.COCONUT_FRUIT, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.APARTMENT_TREE_SAPLING, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GOURMET_TABLE_CLOTH, RenderLayer.getCutout());
		HandledScreens.register(ModScreenHandlers.FOUR_DIMENSIONAL_POCKET, FourDimensionalPocketScreen::new);
		BambooCopterInputHandler.register();
		FourDimensionalPocketInputHandler.register();
		BambooCopterSoundManager.register();
		EntityRendererRegistry.register(ModEntities.MOMOTARO_DUMPLING, FlyingItemEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.SHADOW, ShadowEntityRenderer::new);
	}
}
