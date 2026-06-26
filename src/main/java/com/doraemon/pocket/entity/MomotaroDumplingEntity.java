package com.doraemon.pocket.entity;

import com.doraemon.pocket.event.MomotaroObedienceHandler;
import com.doraemon.pocket.registry.ModEntities;
import com.doraemon.pocket.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class MomotaroDumplingEntity extends ThrownItemEntity {
	public MomotaroDumplingEntity(EntityType<? extends MomotaroDumplingEntity> entityType, World world) {
		super(entityType, world);
	}

	public MomotaroDumplingEntity(World world, LivingEntity owner) {
		super(ModEntities.MOMOTARO_DUMPLING, owner, world);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.MOMOTARO_DUMPLING;
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);

		Entity hitEntity = entityHitResult.getEntity();
		Entity owner = getOwner();
		if (!(owner instanceof ServerPlayerEntity player) || !canAffect(hitEntity)) {
			return;
		}

		MomotaroObedienceHandler.UseResult result = MomotaroObedienceHandler.applyThrownDumpling(player, (LivingEntity) hitEntity);
		if (result == MomotaroObedienceHandler.UseResult.FAILED) {
			spawnImpactParticles(false);
			return;
		}

		spawnImpactParticles(true);
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);

		if (!getWorld().isClient()) {
			if (hitResult.getType() != HitResult.Type.ENTITY) {
				spawnImpactParticles(false);
			}
			discard();
		}
	}

	private boolean canAffect(Entity entity) {
		return entity instanceof LivingEntity
				&& !(entity instanceof PlayerEntity)
				&& !(entity instanceof EnderDragonEntity)
				&& !(entity instanceof WitherEntity);
	}

	private void spawnImpactParticles(boolean success) {
		if (!(getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}

		if (success) {
			serverWorld.spawnParticles(ParticleTypes.HEART, getX(), getY(), getZ(), 5, 0.25D, 0.25D, 0.25D, 0.02D);
			serverWorld.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.NEUTRAL, 0.65F, 1.35F);
		} else {
			serverWorld.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, getStack()), getX(), getY(), getZ(), 8, 0.18D, 0.18D, 0.18D, 0.04D);
			serverWorld.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.45F, 0.8F);
		}
	}
}
