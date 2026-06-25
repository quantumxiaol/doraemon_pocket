package com.doraemon.pocket.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BambooCopterItem extends ArmorItem {
	public BambooCopterItem(ArmorMaterial material, Type type, Settings settings) {
		super(material, type, settings);
	}

	public static boolean isEquipped(LivingEntity entity) {
		return entity.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof BambooCopterItem;
	}

	public static ItemStack getEquippedStack(LivingEntity entity) {
		ItemStack stack = entity.getEquippedStack(EquipmentSlot.HEAD);
		return stack.getItem() instanceof BambooCopterItem ? stack : ItemStack.EMPTY;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.bamboo_copter.tooltip").formatted(Formatting.GRAY));
	}
}
