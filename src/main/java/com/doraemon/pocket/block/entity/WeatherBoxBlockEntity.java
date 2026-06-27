package com.doraemon.pocket.block.entity;

import com.doraemon.pocket.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class WeatherBoxBlockEntity extends BlockEntity {
	private ItemStack card = ItemStack.EMPTY;

	public WeatherBoxBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.WEATHER_BOX, pos, state);
	}

	public boolean hasCard() {
		return !card.isEmpty();
	}

	public ItemStack removeCard() {
		ItemStack removed = card;
		card = ItemStack.EMPTY;
		sync();
		return removed;
	}

	public void setCard(ItemStack stack) {
		card = stack.copy();
		card.setCount(1);
		sync();
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		card = nbt.contains("Card") ? ItemStack.fromNbt(nbt.getCompound("Card")) : ItemStack.EMPTY;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if (!card.isEmpty()) {
			nbt.put("Card", card.writeNbt(new NbtCompound()));
		}
	}

	private void sync() {
		markDirty();
		if (world != null) {
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
		}
	}
}
