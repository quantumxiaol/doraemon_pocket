package com.doraemon.pocket.client.screen;

import com.doraemon.pocket.screen.FourDimensionalPocketScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class FourDimensionalPocketScreen extends HandledScreen<FourDimensionalPocketScreenHandler> {
	private static final int PANEL_COLOR = 0xFFE9F2F6;
	private static final int PANEL_BORDER = 0xFF427C9C;
	private static final int SLOT_OUTLINE = 0xFF7EA7B8;
	private static final int SLOT_FILL = 0xFFF8FCFF;
	private static final int SHADOW = 0x55315A6F;

	public FourDimensionalPocketScreen(FourDimensionalPocketScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		backgroundWidth = FourDimensionalPocketScreenHandler.BACKGROUND_WIDTH;
		backgroundHeight = FourDimensionalPocketScreenHandler.BACKGROUND_HEIGHT;
		playerInventoryTitleX = FourDimensionalPocketScreenHandler.PLAYER_INVENTORY_X;
		playerInventoryTitleY = FourDimensionalPocketScreenHandler.PLAYER_INVENTORY_Y - 11;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		int x = (width - backgroundWidth) / 2;
		int y = (height - backgroundHeight) / 2;
		context.fill(x + 4, y + 5, x + backgroundWidth + 4, y + backgroundHeight + 5, SHADOW);
		context.fill(x, y, x + backgroundWidth, y + backgroundHeight, PANEL_COLOR);
		drawBorder(context, x, y, backgroundWidth, backgroundHeight, PANEL_BORDER);
		for (Slot slot : handler.slots) {
			drawSlotBackground(context, x + slot.x - 1, y + slot.y - 1);
		}
	}

	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		context.drawText(textRenderer, title, titleX, titleY, 0x24495F, false);
		context.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, 0x24495F, false);
	}

	private static void drawSlotBackground(DrawContext context, int x, int y) {
		context.fill(x, y, x + 18, y + 18, SLOT_OUTLINE);
		context.fill(x + 1, y + 1, x + 17, y + 17, SLOT_FILL);
	}

	private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 2, color);
		context.fill(x, y + height - 2, x + width, y + height, color);
		context.fill(x, y, x + 2, y + height, color);
		context.fill(x + width - 2, y, x + width, y + height, color);
	}
}
