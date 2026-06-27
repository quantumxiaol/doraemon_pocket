package com.doraemon.pocket.screen;

import com.doraemon.pocket.inventory.FourDimensionalPocketInventory;
import com.doraemon.pocket.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class FourDimensionalPocketScreenHandler extends ScreenHandler {
	public static final int POCKET_COLUMNS = 8;
	public static final int POCKET_ROWS = 8;
	public static final int POCKET_SIZE = POCKET_COLUMNS * POCKET_ROWS;
	public static final int BACKGROUND_WIDTH = 194;
	public static final int BACKGROUND_HEIGHT = 250;
	public static final int POCKET_X = 25;
	public static final int POCKET_Y = 18;
	public static final int PLAYER_INVENTORY_X = 16;
	public static final int PLAYER_INVENTORY_Y = 166;

	private final Inventory inventory;

	public FourDimensionalPocketScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(POCKET_SIZE));
	}

	public FourDimensionalPocketScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
		super(ModScreenHandlers.FOUR_DIMENSIONAL_POCKET, syncId);
		checkSize(inventory, POCKET_SIZE);
		this.inventory = inventory;
		inventory.onOpen(playerInventory.player);

		for (int row = 0; row < POCKET_ROWS; row++) {
			for (int column = 0; column < POCKET_COLUMNS; column++) {
				addSlot(new PocketSlot(inventory, column + row * POCKET_COLUMNS, POCKET_X + column * 18, POCKET_Y + row * 18));
			}
		}

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				addSlot(new Slot(playerInventory, column + row * 9 + 9, PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
			}
		}

		for (int column = 0; column < 9; column++) {
			addSlot(new Slot(playerInventory, column, PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + 58));
		}
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return inventory.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int index) {
		ItemStack movedStack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot == null || !slot.hasStack()) {
			return movedStack;
		}

		ItemStack slotStack = slot.getStack();
		movedStack = slotStack.copy();
		if (index < POCKET_SIZE) {
			if (!insertItem(slotStack, POCKET_SIZE, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else {
			if (!FourDimensionalPocketInventory.canStore(slotStack) || !insertItem(slotStack, 0, POCKET_SIZE, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (slotStack.isEmpty()) {
			slot.setStack(ItemStack.EMPTY);
		} else {
			slot.markDirty();
		}
		slot.onTakeItem(player, slotStack);
		return movedStack;
	}

	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		inventory.onClose(player);
	}

	private static class PocketSlot extends Slot {
		private PocketSlot(Inventory inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean canInsert(ItemStack stack) {
			return FourDimensionalPocketInventory.canStore(stack);
		}
	}
}
