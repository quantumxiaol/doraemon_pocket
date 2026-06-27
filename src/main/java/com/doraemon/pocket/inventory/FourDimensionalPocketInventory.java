package com.doraemon.pocket.inventory;

import com.doraemon.pocket.registry.ModItems;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class FourDimensionalPocketInventory extends SimpleInventory {
	private static final String NBT_KEY = "PocketItems";
	private static final String ITEMS_KEY = "Items";
	private static final String SLOT_KEY = "Slot";

	private final ItemStack carrier;
	private boolean loading;

	public FourDimensionalPocketInventory(ItemStack carrier) {
		super(64);
		this.carrier = carrier;
		load();
	}

	public static boolean canStore(ItemStack stack) {
		return !stack.isOf(ModItems.FOUR_DIMENSIONAL_POCKET);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!loading) {
			save();
		}
	}

	private void load() {
		loading = true;
		NbtCompound root = carrier.getSubNbt(NBT_KEY);
		if (root != null) {
			NbtList items = root.getList(ITEMS_KEY, NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < items.size(); i++) {
				NbtCompound itemNbt = items.getCompound(i);
				int slot = itemNbt.getByte(SLOT_KEY) & 255;
				if (slot >= 0 && slot < size()) {
					setStack(slot, ItemStack.fromNbt(itemNbt));
				}
			}
		}
		loading = false;
	}

	private void save() {
		NbtList items = new NbtList();
		for (int slot = 0; slot < size(); slot++) {
			ItemStack stack = getStack(slot);
			if (!stack.isEmpty()) {
				NbtCompound itemNbt = new NbtCompound();
				itemNbt.putByte(SLOT_KEY, (byte) slot);
				stack.writeNbt(itemNbt);
				items.add(itemNbt);
			}
		}

		if (items.isEmpty()) {
			if (carrier.hasNbt()) {
				carrier.getNbt().remove(NBT_KEY);
			}
			return;
		}

		NbtCompound root = new NbtCompound();
		root.put(ITEMS_KEY, items);
		carrier.getOrCreateNbt().put(NBT_KEY, root);
	}
}
