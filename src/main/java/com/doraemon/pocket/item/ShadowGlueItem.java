package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.entity.ShadowEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShadowGlueItem extends Item {
	public ShadowGlueItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (!(entity instanceof ShadowEntity shadow)) {
			return ActionResult.PASS;
		}

		World world = user.getWorld();
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		if (!user.getUuid().equals(shadow.getOwnerUuid())) {
			user.sendMessage(Text.translatable("message.doraemon_pocket.shadow_glue.not_your_shadow"), true);
			return ActionResult.SUCCESS;
		}

		if (world instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.POOF, shadow.getX(), shadow.getBodyY(0.5D), shadow.getZ(), 16, 0.35D, 0.55D, 0.35D, 0.04D);
		}
		world.playSound(null, shadow.getX(), shadow.getY(), shadow.getZ(), SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.PLAYERS, 0.85F, 1.2F);
		shadow.discard();
		stack.damage(1, user, p -> p.sendToolBreakStatus(hand));
		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.shadow_glue.tooltip").formatted(Formatting.GRAY));
	}
}
