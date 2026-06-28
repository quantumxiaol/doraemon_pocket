package com.doraemon.pocket.mixin;

import com.doraemon.pocket.item.PassThroughCapItem;
import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {
	@Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
	private static void doraemon_pocket$hidePhaseBlockOverlay(Sprite sprite, MatrixStack matrices, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}

		if (client.player.hasStatusEffect(ModStatusEffects.UNDERGROUND_SWIMMING) || PassThroughCapItem.isEquipped(client.player)) {
			ci.cancel();
		}
	}
}
