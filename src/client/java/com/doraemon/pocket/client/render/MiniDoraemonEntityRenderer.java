package com.doraemon.pocket.client.render;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.entity.MiniDoraemonEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class MiniDoraemonEntityRenderer extends DoraemonEntityRenderer<MiniDoraemonEntity> {
	private static final Identifier[] TEXTURES = {
			DoraemonPocket.id("textures/entity/mini_doraemon_red.png"),
			DoraemonPocket.id("textures/entity/mini_doraemon_yellow.png"),
			DoraemonPocket.id("textures/entity/mini_doraemon_green.png")
	};

	public MiniDoraemonEntityRenderer(EntityRendererFactory.Context context) {
		super(context, 0.45F, 0.2F);
	}

	@Override
	public Identifier getTexture(MiniDoraemonEntity entity) {
		return TEXTURES[entity.getVariant()];
	}
}
