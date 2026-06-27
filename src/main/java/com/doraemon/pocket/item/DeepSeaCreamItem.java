package com.doraemon.pocket.item;

import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class DeepSeaCreamItem extends Item {
    private static final int DURATION_TICKS = 20 * 60 * 20;

    public DeepSeaCreamItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient()) {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, DURATION_TICKS, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, DURATION_TICKS, 0));
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, DURATION_TICKS, 0));
            world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.8F, 1.2F);
        }

        if (user instanceof PlayerEntity player && player.isCreative()) {
            return stack;
        }

        stack.decrement(1);
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.doraemon_pocket.deep_sea_cream.tooltip").formatted(Formatting.GRAY));
    }
}
