package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AdaptationLightItem extends Item {
	private static final int EFFECT_DURATION_TICKS = 240;

	public AdaptationLightItem(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (world.isClient() || !(entity instanceof PlayerEntity player)) {
			return;
		}

		applyAdaptation(player);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.adaptation_light.tooltip").formatted(Formatting.GRAY));
	}

	private static void applyAdaptation(LivingEntity entity) {
		entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.ENVIRONMENTAL_ADAPTATION, EFFECT_DURATION_TICKS, 0, false, false, true));
		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, EFFECT_DURATION_TICKS, 0, false, false, true));
		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, EFFECT_DURATION_TICKS, 0, false, false, true));
	}
}
