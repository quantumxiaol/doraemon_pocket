package com.doraemon.pocket.client.render;

import com.doraemon.pocket.client.model.DoraemonEntityModel;
import com.doraemon.pocket.entity.DoraemonEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class DoraemonEntityRenderer<T extends DoraemonEntity> extends MobEntityRenderer<T, DoraemonEntityModel<T>> {
	private final float modelScale;

	public DoraemonEntityRenderer(EntityRendererFactory.Context context, float modelScale, float shadowRadius) {
		super(context, new DoraemonEntityModel<>(context.getPart(DoraemonEntityModel.LAYER)), shadowRadius);
		this.modelScale = modelScale;
	}

	@Override
	public Identifier getTexture(T entity) {
		return DoraemonEntityModel.TEXTURE;
	}

	@Override
	protected void scale(T entity, MatrixStack matrices, float amount) {
		matrices.scale(modelScale, modelScale, modelScale);
	}
}
