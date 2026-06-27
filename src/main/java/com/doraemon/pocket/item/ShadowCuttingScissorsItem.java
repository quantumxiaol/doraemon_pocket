package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.entity.ShadowEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShadowCuttingScissorsItem extends Item {
	private static final double EXISTING_SHADOW_RANGE = 64.0D;
	private static final int COOLDOWN_TICKS = 40;

	public ShadowCuttingScissorsItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient()) {
			return TypedActionResult.success(stack);
		}

		if (!(user instanceof ServerPlayerEntity player)) {
			return TypedActionResult.pass(stack);
		}

		if (hasExistingShadow(player)) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.shadow_scissors.already_has_shadow"), true);
			return TypedActionResult.success(stack);
		}

		ShadowEntity shadow = new ShadowEntity(world, player);
		shadow.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), 0.0F);
		world.spawnEntity(shadow);
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 0.8F, 0.65F);
		player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.shadow_scissors.tooltip").formatted(Formatting.GRAY));
	}

	private static boolean hasExistingShadow(ServerPlayerEntity player) {
		Box box = player.getBoundingBox().expand(EXISTING_SHADOW_RANGE);
		for (ShadowEntity shadow : player.getWorld().getEntitiesByClass(ShadowEntity.class, box, Entity::isAlive)) {
			if (player.getUuid().equals(shadow.getOwnerUuid())) {
				return true;
			}
		}
		return false;
	}
}
