package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TranslationGummyItem extends Item {
	private static final int DURATION_TICKS = 20 * 60 * 20;

	public TranslationGummyItem(Settings settings) {
		super(settings);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		ItemStack result = super.finishUsing(stack, world, user);
		if (!world.isClient()) {
			user.addStatusEffect(new StatusEffectInstance(ModStatusEffects.UNIVERSAL_UNDERSTANDING, DURATION_TICKS, 0, false, false, true));
		}
		return result;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.translation_gummy.tooltip").formatted(Formatting.GRAY));
	}
}
