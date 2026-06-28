package com.doraemon.pocket.event;

import com.doraemon.pocket.item.PassThroughCapItem;
import com.doraemon.pocket.network.DoraemonPackets;
import com.doraemon.pocket.registry.ModStatusEffects;
import com.doraemon.pocket.util.PhaseBlockRules;
import com.doraemon.pocket.util.SpatialPhaseManager;
import com.doraemon.pocket.util.SpatialPhaseManager.PhaseSource;
import com.doraemon.pocket.util.PhaseVisuals;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

public final class UndergroundSwimTickHandler {
	private UndergroundSwimTickHandler() {
	}

	public static void register() {
		PlayerGadgetTickDispatcher.registerPlayerTick((player, time) -> tickPlayer(player));
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(UndergroundSwimTickHandler::allowDamage);
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopPhasing(handler.player));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SpatialPhaseManager.clear());
	}

	public static boolean isPhasing(LivingEntity entity) {
		return SpatialPhaseManager.isActive(entity, PhaseSource.UNDERGROUND_SWIM);
	}

	private static void tickPlayer(ServerPlayerEntity player) {
		if (!player.hasStatusEffect(ModStatusEffects.UNDERGROUND_SWIMMING) || player.isSpectator() || player.hasVehicle()) {
			stopPhasing(player);
			if (!PassThroughCapItem.isEquipped(player)) {
				DoraemonPackets.forgetUndergroundSwimControl(player.getUuid());
			}
			return;
		}

		PhaseVisuals.refreshNightVision(player);
		Box bodyBox = player.getBoundingBox().contract(0.08D);

		if (PhaseBlockRules.touchesForbiddenBlock(player.getServerWorld(), bodyBox)
				|| PhaseBlockRules.touchesUnswimmableSolidBlock(player.getServerWorld(), bodyBox)) {
			SpatialPhaseManager.restoreLastValidPosition(player);
			return;
		}

		boolean insideSwimmable = PhaseBlockRules.touchesSwimmableSolidBlock(player.getServerWorld(), bodyBox);
		if (insideSwimmable) {
			startPhasing(player);
			player.fallDistance = 0.0F;
			PhaseVisuals.spawnDirectionGuide(player);
			spawnParticles(player);
		} else {
			stopPhasing(player);
			SpatialPhaseManager.rememberValidPosition(player);
		}
	}

	private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
		if (!(entity instanceof ServerPlayerEntity player) || !source.isOf(DamageTypes.IN_WALL)) {
			return true;
		}

		if (!player.hasStatusEffect(ModStatusEffects.UNDERGROUND_SWIMMING)) {
			return true;
		}

		return !isPhasing(player)
				&& !PhaseBlockRules.touchesSwimmableSolidBlock(player.getServerWorld(), player.getBoundingBox().contract(0.08D));
	}

	private static void startPhasing(ServerPlayerEntity player) {
		SpatialPhaseManager.enable(player, PhaseSource.UNDERGROUND_SWIM);
	}

	private static void stopPhasing(ServerPlayerEntity player) {
		if (SpatialPhaseManager.isActive(player, PhaseSource.UNDERGROUND_SWIM)) {
			SpatialPhaseManager.disable(player, PhaseSource.UNDERGROUND_SWIM);
		}
	}

	private static void spawnParticles(ServerPlayerEntity player) {
		if (player.age % 8 != 0) {
			return;
		}
		player.getServerWorld().spawnParticles(
				new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.DIRT.getDefaultState()),
				player.getX(),
				player.getBodyY(0.45D),
				player.getZ(),
				5,
				0.28D,
				0.28D,
				0.28D,
				0.02D
		);
	}

}
