package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
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
	private static final int REFRESH_INTERVAL_TICKS = 20;
	private static final int ADAPTATION_DURATION_TICKS = 80;
	private static final int ADAPTATION_REFRESH_THRESHOLD_TICKS = 40;
	private static final int VISION_DURATION_TICKS = 260;
	private static final int VISION_REFRESH_THRESHOLD_TICKS = 220;

	public AdaptationLightItem(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (world.isClient() || !(entity instanceof PlayerEntity player)) {
			return;
		}

		if (player.age % REFRESH_INTERVAL_TICKS != 0) {
			return;
		}

		applyAdaptation(player);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.adaptation_light.tooltip").formatted(Formatting.GRAY));
	}

	private static void applyAdaptation(LivingEntity entity) {
		refreshEffect(entity, ModStatusEffects.ENVIRONMENTAL_ADAPTATION, ADAPTATION_DURATION_TICKS, ADAPTATION_REFRESH_THRESHOLD_TICKS);
		refreshEffect(entity, StatusEffects.NIGHT_VISION, VISION_DURATION_TICKS, VISION_REFRESH_THRESHOLD_TICKS);
		refreshEffect(entity, StatusEffects.CONDUIT_POWER, ADAPTATION_DURATION_TICKS, ADAPTATION_REFRESH_THRESHOLD_TICKS);
	}

	private static void refreshEffect(LivingEntity entity, StatusEffect effect, int duration, int threshold) {
		StatusEffectInstance current = entity.getStatusEffect(effect);
		if (current != null && current.getDuration() > threshold) {
			return;
		}

		entity.addStatusEffect(new StatusEffectInstance(effect, duration, 0, false, false, true));
	}
}
