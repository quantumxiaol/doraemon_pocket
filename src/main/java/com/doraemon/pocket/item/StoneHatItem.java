package com.doraemon.pocket.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StoneHatItem extends ArmorItem {
	public StoneHatItem(ArmorMaterial material, Type type, Settings settings) {
		super(material, type, settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.stone_hat.tooltip").formatted(Formatting.GRAY));
	}
}
