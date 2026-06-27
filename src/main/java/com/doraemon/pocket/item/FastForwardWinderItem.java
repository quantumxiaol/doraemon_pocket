package com.doraemon.pocket.item;

import com.doraemon.pocket.mixin.AbstractFurnaceBlockEntityAccessor;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class FastForwardWinderItem extends Item {
    private static final int EFFECT_DURATION_TICKS = 20 * 60;
    private static final int FURNACE_BOOST_TICKS = 200;

    public FastForwardWinderItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient()) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, EFFECT_DURATION_TICKS, 2));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, EFFECT_DURATION_TICKS, 2));
            user.getWorld().playSound(null, entity.getBlockPos(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.8F, 1.7F);
            damageOrConsume(stack, user);
            user.getItemCooldownManager().set(this, 20);
        }
        return ActionResult.success(user.getWorld().isClient());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        PlayerEntity player = context.getPlayer();
        if (!(blockEntity instanceof AbstractFurnaceBlockEntity furnace)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            AbstractFurnaceBlockEntityAccessor accessor = (AbstractFurnaceBlockEntityAccessor) furnace;
            int total = Math.max(1, accessor.doraemonPocket$getCookTimeTotal());
            int nextCookTime = Math.min(total - 1, accessor.doraemonPocket$getCookTime() + FURNACE_BOOST_TICKS);
            accessor.doraemonPocket$setCookTime(nextCookTime);
            furnace.markDirty();
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.6F, 1.6F);
            if (player != null) {
                damageOrConsume(context.getStack(), player);
                player.getItemCooldownManager().set(this, 20);
            }
        }

        return ActionResult.success(world.isClient());
    }

    private void damageOrConsume(ItemStack stack, PlayerEntity player) {
        if (player.isCreative()) {
            return;
        }
        if (stack.isDamageable()) {
            stack.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
        } else {
            stack.decrement(1);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.doraemon_pocket.fast_forward_winder.tooltip").formatted(Formatting.GRAY));
    }
}
