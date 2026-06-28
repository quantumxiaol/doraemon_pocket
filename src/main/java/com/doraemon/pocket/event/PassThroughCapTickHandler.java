package com.doraemon.pocket.event;

import com.doraemon.pocket.item.PassThroughCapItem;
import com.doraemon.pocket.util.PhaseBlockRules;
import com.doraemon.pocket.util.SpatialPhaseManager;
import com.doraemon.pocket.util.SpatialPhaseManager.PhaseSource;
import com.doraemon.pocket.util.PhaseVisuals;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;

public final class PassThroughCapTickHandler {
	private static final int DURABILITY_DAMAGE_INTERVAL = 12;

	private PassThroughCapTickHandler() {
	}

	public static void register() {
		PlayerGadgetTickDispatcher.registerPlayerTick((player, time) -> tickPlayer(player));
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(PassThroughCapTickHandler::allowDamage);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopPhasing(handler.player));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SpatialPhaseManager.clear());
	}

	private static void tickPlayer(ServerPlayerEntity player) {
		ItemStack cap = PassThroughCapItem.getEquippedStack(player);
		if (cap.isEmpty() || player.isSpectator() || player.hasVehicle()) {
			stopPhasing(player);
			return;
		}

		PhaseVisuals.refreshNightVision(player);
		Box bodyBox = player.getBoundingBox().contract(0.08D);

		if (PhaseBlockRules.touchesForbiddenBlock(player.getServerWorld(), bodyBox)
				|| PhaseBlockRules.touchesCapUnphaseableSolidBlock(player.getServerWorld(), bodyBox)) {
			SpatialPhaseManager.restoreLastValidPosition(player);
			return;
		}

		boolean insidePhaseable = PhaseBlockRules.touchesCapPhaseableSolidBlock(player.getServerWorld(), bodyBox);
		if (insidePhaseable) {
			startPhasing(player);
			player.fallDistance = 0.0F;
			PhaseVisuals.spawnDirectionGuide(player);
			damageCap(player, cap);
			spawnParticles(player);
			return;
		}

		stopPhasing(player);
		SpatialPhaseManager.rememberValidPosition(player);
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayerEntity player) || !source.isOf(DamageTypes.IN_WALL)) {
			return true;
		}

		if (!PassThroughCapItem.isEquipped(player)) {
			return true;
		}

		return !SpatialPhaseManager.isActive(player, PhaseSource.PASS_THROUGH_CAP)
				&& !PhaseBlockRules.touchesCapPhaseableSolidBlock(player.getServerWorld(), player.getBoundingBox().contract(0.08D));
	}

	private static void startPhasing(ServerPlayerEntity player) {
		SpatialPhaseManager.enable(player, PhaseSource.PASS_THROUGH_CAP);
	}

	private static void stopPhasing(ServerPlayerEntity player) {
		SpatialPhaseManager.disable(player, PhaseSource.PASS_THROUGH_CAP);
	}

	private static void damageCap(ServerPlayerEntity player, ItemStack cap) {
		if (player.isCreative() || player.age % DURABILITY_DAMAGE_INTERVAL != 0) {
			return;
		}
		cap.damage(1, player, brokenPlayer -> brokenPlayer.sendEquipmentBreakStatus(EquipmentSlot.HEAD));
	}

	private static void spawnParticles(ServerPlayerEntity player) {
		if (player.age % 10 != 0) {
			return;
		}
		BlockState state = player.getBlockStateAtPos();
		if (state.isAir() || state.getCollisionShape(player.getWorld(), player.getBlockPos()).isEmpty()) {
			return;
		}
		player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.12F, 1.65F);
		player.getServerWorld().spawnParticles(
				new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
				player.getX(),
				player.getBodyY(0.55D),
				player.getZ(),
				6,
				0.28D,
				0.35D,
				0.28D,
				0.025D
			);
	}

}
