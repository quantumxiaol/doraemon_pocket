package com.doraemon.pocket.command;

import com.doraemon.pocket.DoraemonPocket;
import com.doraemon.pocket.item.AnywhereDoorItem;
import com.doraemon.pocket.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;

public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
		DoraemonPocket.LOGGER.debug("Registered Doraemon Pocket commands.");
	}

	private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("doraemon_pocket")
				.then(CommandManager.literal("anywhere_door")
						.then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
								.executes(ModCommands::setAnywhereDoorTarget))));
	}

	private static int setAnywhereDoorTarget(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		ServerPlayerEntity player = source.getPlayerOrThrow();
		ItemStack stack = getHeldAnywhereDoor(player);
		if (stack.isEmpty()) {
			source.sendError(Text.translatable("message.doraemon_pocket.anywhere_door.must_hold"));
			return 0;
		}

		BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
		AnywhereDoorItem.setTarget(stack, source.getWorld(), pos);
		source.sendFeedback(() -> Text.translatable("message.doraemon_pocket.anywhere_door.target_set", pos.getX(), pos.getY(), pos.getZ()), false);
		return 1;
	}

	private static ItemStack getHeldAnywhereDoor(ServerPlayerEntity player) {
		ItemStack mainHand = player.getMainHandStack();
		if (mainHand.isOf(ModItems.ANYWHERE_DOOR)) {
			return mainHand;
		}
		ItemStack offHand = player.getOffHandStack();
		return offHand.isOf(ModItems.ANYWHERE_DOOR) ? offHand : ItemStack.EMPTY;
	}
}
