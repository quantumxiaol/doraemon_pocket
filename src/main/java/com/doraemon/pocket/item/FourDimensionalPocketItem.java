package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.inventory.FourDimensionalPocketInventory;
import com.doraemon.pocket.registry.ModItems;
import com.doraemon.pocket.screen.FourDimensionalPocketScreenHandler;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FourDimensionalPocketItem extends Item {
	public static final int SIZE = 64;

	public FourDimensionalPocketItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient()) {
			return TypedActionResult.success(stack);
		}

		if (user instanceof ServerPlayerEntity player) {
			open(player, stack);
		}
		return TypedActionResult.success(stack);
	}

	public static boolean openFirst(ServerPlayerEntity player) {
		ItemStack mainHand = player.getMainHandStack();
		if (mainHand.isOf(ModItems.FOUR_DIMENSIONAL_POCKET)) {
			open(player, mainHand);
			return true;
		}

		ItemStack offHand = player.getOffHandStack();
		if (offHand.isOf(ModItems.FOUR_DIMENSIONAL_POCKET)) {
			open(player, offHand);
			return true;
		}

		for (int slot = 0; slot < player.getInventory().size(); slot++) {
			ItemStack stack = player.getInventory().getStack(slot);
			if (stack.isOf(ModItems.FOUR_DIMENSIONAL_POCKET)) {
				open(player, stack);
				return true;
			}
		}

		player.sendMessage(Text.translatable("message.doraemon_pocket.four_dimensional_pocket.missing"), true);
		return false;
	}

	public static void open(ServerPlayerEntity player, ItemStack stack) {
		player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
				(syncId, inventory, ignored) -> new FourDimensionalPocketScreenHandler(syncId, inventory, new FourDimensionalPocketInventory(stack)),
				Text.translatable("container.doraemon_pocket.four_dimensional_pocket")
		));
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 0.45F, 1.25F);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.four_dimensional_pocket.tooltip", SIZE).formatted(Formatting.GRAY));
	}
}
