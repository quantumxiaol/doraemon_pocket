package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.registry.ModItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DevilsPassportItem extends Item {
	public DevilsPassportItem(Settings settings) {
		super(settings);
	}

	public static boolean isActive(PlayerEntity player) {
		return player.getMainHandStack().isOf(ModItems.DEVILS_PASSPORT) || player.getOffHandStack().isOf(ModItems.DEVILS_PASSPORT);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.devils_passport.tooltip").formatted(Formatting.GRAY));
	}
}
