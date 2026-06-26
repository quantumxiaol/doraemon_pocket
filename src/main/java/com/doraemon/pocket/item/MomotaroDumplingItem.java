package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.entity.MomotaroDumplingEntity;
import com.doraemon.pocket.event.MomotaroObedienceHandler;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MomotaroDumplingItem extends Item {
	private static final int COOLDOWN_TICKS = 10;

	public MomotaroDumplingItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.PLAYERS, 0.55F, 0.75F + world.getRandom().nextFloat() * 0.35F);
		user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

		if (!world.isClient()) {
			MomotaroDumplingEntity dumpling = new MomotaroDumplingEntity(world, user);
			dumpling.setItem(new ItemStack(this));
			dumpling.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.45F, 0.8F);
			world.spawnEntity(dumpling);

			if (!user.isCreative()) {
				stack.decrement(1);
			}
		}

		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (entity instanceof PlayerEntity || entity instanceof EnderDragonEntity || entity instanceof WitherEntity) {
			return ActionResult.PASS;
		}

		World world = user.getWorld();
		if (world.isClient()) {
			return ActionResult.SUCCESS;
		}

		if (!(user instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) {
			return ActionResult.PASS;
		}

		MomotaroObedienceHandler.UseResult result = MomotaroObedienceHandler.useOnEntity(serverPlayer, entity, user.isSneaking());
		if (result == MomotaroObedienceHandler.UseResult.FAILED) {
			return ActionResult.FAIL;
		}
		if (result == MomotaroObedienceHandler.UseResult.ALREADY_ACTIVE) {
			return ActionResult.PASS;
		}

		user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		user.swingHand(hand, true);
		if (result != MomotaroObedienceHandler.UseResult.MOUNTED && result != MomotaroObedienceHandler.UseResult.ALREADY_ACTIVE) {
			playFeedback(serverWorld, entity, result == MomotaroObedienceHandler.UseResult.RELEASED);
		}

		if (result == MomotaroObedienceHandler.UseResult.APPLIED && !user.isCreative()) {
			stack.decrement(1);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.momotaro_dumpling.tooltip").formatted(Formatting.GRAY));
	}

	private static void playFeedback(ServerWorld world, LivingEntity entity, boolean released) {
		world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), released ? SoundEvents.ENTITY_FOX_EAT : SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.NEUTRAL, 0.75F, released ? 0.85F : 1.35F);
		world.spawnParticles(released ? ParticleTypes.POOF : ParticleTypes.HEART, entity.getX(), entity.getEyeY(), entity.getZ(), released ? 8 : 5, 0.35D, 0.35D, 0.35D, 0.02D);
	}
}
