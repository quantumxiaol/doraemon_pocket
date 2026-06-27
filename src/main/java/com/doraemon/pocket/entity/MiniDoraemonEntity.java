package com.doraemon.pocket.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import com.doraemon.pocket.registry.ModItems;

public class MiniDoraemonEntity extends DoraemonEntity {
	private static final int VARIANT_COUNT = 3;
	private static final TrackedData<Integer> VARIANT = DataTracker.registerData(MiniDoraemonEntity.class, TrackedDataHandlerRegistry.INTEGER);

	public MiniDoraemonEntity(EntityType<? extends MiniDoraemonEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32D)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.2D);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		dataTracker.startTracking(VARIANT, random.nextInt(VARIANT_COUNT));
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!stack.isOf(ModItems.DORAYAKI)) {
			return super.interactMob(player, hand);
		}
		if (!getWorld().isClient()) {
			if (!player.isCreative()) {
				stack.decrement(1);
			}
			heal(4.0F);
			if (getWorld() instanceof ServerWorld serverWorld) {
				serverWorld.spawnParticles(ParticleTypes.HEART, getX(), getBodyY(0.85D), getZ(), 6, 0.2D, 0.2D, 0.2D, 0.02D);
			}
			getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.NEUTRAL, 0.45F, 1.9F);
		}
		return ActionResult.success(getWorld().isClient());
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("Variant", getVariant());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("Variant")) {
			dataTracker.set(VARIANT, Math.floorMod(nbt.getInt("Variant"), VARIANT_COUNT));
		}
	}

	public int getVariant() {
		return Math.floorMod(dataTracker.get(VARIANT), VARIANT_COUNT);
	}

	@Override
	protected boolean isMiniDoraemon() {
		return true;
	}
}
