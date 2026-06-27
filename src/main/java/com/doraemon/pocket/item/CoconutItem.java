package com.doraemon.pocket.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CoconutItem extends Item {
	private static final int COOLDOWN_TICKS = 8;
	private static final List<Item> FOOD_POOL = List.of(
			Items.APPLE,
			Items.BREAD,
			Items.COOKIE,
			Items.BAKED_POTATO,
			Items.COOKED_BEEF,
			Items.COOKED_CHICKEN,
			Items.COOKED_PORKCHOP,
			Items.COOKED_SALMON,
			Items.MELON_SLICE,
			Items.PUMPKIN_PIE
	);

	public CoconutItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient()) {
			return TypedActionResult.success(stack);
		}

		Random random = world.getRandom();
		int drops = 1 + random.nextInt(3);
		for (int i = 0; i < drops; i++) {
			Item item = FOOD_POOL.get(random.nextInt(FOOD_POOL.size()));
			ItemStack food = new ItemStack(item);
			if (!user.getInventory().insertStack(food)) {
				user.dropItem(food, false);
			}
		}

		if (!user.isCreative()) {
			stack.decrement(1);
		}
		user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.55F, 0.75F + random.nextFloat() * 0.2F);
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.coconut.tooltip").formatted(Formatting.GRAY));
	}
}
