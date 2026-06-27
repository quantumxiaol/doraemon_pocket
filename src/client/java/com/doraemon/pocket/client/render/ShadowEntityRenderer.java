package com.doraemon.pocket.client.render;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.entity.ShadowEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class ShadowEntityRenderer extends MobEntityRenderer<ShadowEntity, PlayerEntityModel<ShadowEntity>> {
	private static final Identifier FRIENDLY_TEXTURE = DoraemonPocket.id("textures/entity/shadow.png");
	private static final Identifier REBEL_TEXTURE = DoraemonPocket.id("textures/entity/shadow_rebel.png");

	public ShadowEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new PlayerEntityModel<>(context.getPart(EntityModelLayers.PLAYER), false), 0.45F);
	}

	@Override
	public Identifier getTexture(ShadowEntity entity) {
		return entity.isRebel() ? REBEL_TEXTURE : FRIENDLY_TEXTURE;
	}
}
