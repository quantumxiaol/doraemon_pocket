package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.registry.ModItems;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TimeClothItem extends Item {
	private static final int EXPERIENCE_POINT_COST = 30;
	private static final int COOLDOWN_TICKS = 20;

	public TimeClothItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return tryRepairFromHands(world, user, hand, false);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.time_cloth.tooltip").formatted(Formatting.GRAY));
	}

	private static boolean canRepair(ItemStack stack) {
		return !stack.isEmpty() && stack.isDamageable() && stack.isDamaged();
	}

	public static TypedActionResult<ItemStack> tryRepairFromHands(World world, PlayerEntity user, Hand usedHand, boolean passWhenNoRepairPair) {
		RepairPair repairPair = findRepairPair(user, usedHand);
		ItemStack usedStack = user.getStackInHand(usedHand);

		if (repairPair == null) {
			return passWhenNoRepairPair ? TypedActionResult.pass(usedStack) : TypedActionResult.fail(usedStack);
		}

		if (!user.isCreative() && getCurrentExperiencePoints(user) < EXPERIENCE_POINT_COST) {
			return TypedActionResult.fail(usedStack);
		}

		repair(world, user, repairPair);
		return TypedActionResult.success(usedStack, world.isClient());
	}

	private static RepairPair findRepairPair(PlayerEntity user, Hand usedHand) {
		Hand otherHand = getOtherHand(usedHand);
		ItemStack usedStack = user.getStackInHand(usedHand);
		ItemStack otherStack = user.getStackInHand(otherHand);

		if (usedStack.isOf(ModItems.TIME_CLOTH) && canRepair(otherStack)) {
			return new RepairPair(usedHand, otherHand);
		}

		if (otherStack.isOf(ModItems.TIME_CLOTH) && canRepair(usedStack)) {
			return new RepairPair(otherHand, usedHand);
		}

		return null;
	}

	private static Hand getOtherHand(Hand hand) {
		return hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
	}

	private static void repair(World world, PlayerEntity user, RepairPair repairPair) {
		ItemStack cloth = user.getStackInHand(repairPair.clothHand);
		ItemStack target = user.getStackInHand(repairPair.targetHand);

		user.getItemCooldownManager().set(cloth.getItem(), COOLDOWN_TICKS);
		user.swingHand(repairPair.clothHand, true);

		if (world.isClient()) {
			return;
		}

		int repairedDamage = target.getDamage();
		target.setDamage(0);

		if (!user.isCreative()) {
			user.totalExperience = getCurrentExperiencePoints(user);
			user.addExperience(-EXPERIENCE_POINT_COST);
		}

		if (user instanceof ServerPlayerEntity serverPlayer) {
			cloth.damage(1, serverPlayer, player -> player.sendToolBreakStatus(repairPair.clothHand));
		}

		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 0.65F, 1.8F);

		if (world instanceof ServerWorld serverWorld) {
			double particleCount = Math.min(18.0D, Math.max(5.0D, repairedDamage / 8.0D));
			serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, user.getX(), user.getEyeY() - 0.25D, user.getZ(), (int) particleCount, 0.35D, 0.25D, 0.35D, 0.02D);
		}
	}

	private static int getCurrentExperiencePoints(PlayerEntity player) {
		return getExperienceForLevel(player.experienceLevel) + Math.round(player.experienceProgress * player.getNextLevelExperience());
	}

	private static int getExperienceForLevel(int level) {
		if (level <= 0) {
			return 0;
		}
		if (level <= 16) {
			return level * level + 6 * level;
		}
		if (level <= 31) {
			return (int) (2.5D * level * level - 40.5D * level + 360.0D);
		}
		return (int) (4.5D * level * level - 162.5D * level + 2220.0D);
	}

	private record RepairPair(Hand clothHand, Hand targetHand) {
	}
}
