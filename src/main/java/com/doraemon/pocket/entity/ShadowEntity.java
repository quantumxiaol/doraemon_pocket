package com.doraemon.pocket.entity;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import com.doraemon.pocket.registry.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ShadowEntity extends PathAwareEntity {
	private static final TrackedData<Boolean> REBEL = DataTracker.registerData(ShadowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private static final int FRIENDLY_DURATION_TICKS = 20 * 60 * 5;
	private static final int PICKUP_INTERVAL_TICKS = 10;
	private static final double ASSIST_RANGE = 24.0D;
	private static final double FOLLOW_START_RANGE = 7.0D;
	private static final double FOLLOW_STOP_RANGE = 3.0D;
	private static final double TELEPORT_START_RANGE = 24.0D;
	private static final double PICKUP_RANGE = 8.0D;
	private static final double PICKUP_DISTANCE_SQUARED = 2.2D;

	private UUID ownerUuid;
	private int shadowAge;

	public ShadowEntity(EntityType<? extends ShadowEntity> entityType, World world) {
		super(entityType, world);
	}

	public ShadowEntity(World world, ServerPlayerEntity owner) {
		this(ModEntities.SHADOW, world);
		this.ownerUuid = owner.getUuid();
		setCustomName(friendlyName(owner));
		setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30D)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.25D);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(2, new MeleeAttackGoal(this, 1.25D, true));
		goalSelector.add(7, new WanderAroundFarGoal(this, 0.85D));
		goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		goalSelector.add(9, new LookAroundGoal(this));
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		dataTracker.startTracking(REBEL, false);
	}

	@Override
	public void tick() {
		super.tick();
		if (getWorld().isClient()) {
			return;
		}

		shadowAge++;
		ServerPlayerEntity owner = getOwnerPlayer();
		if (!isRebel() && shadowAge >= FRIENDLY_DURATION_TICKS) {
			becomeRebel(owner);
		}

		if (isRebel()) {
			if (owner != null && owner.isAlive()) {
				setTarget(owner);
			}
			return;
		}

		if (owner == null || !owner.isAlive()) {
			return;
		}

		LivingEntity assistTarget = findAssistTarget(owner);
		if (assistTarget != null) {
			setTarget(assistTarget);
		} else if (getTarget() != null && !getTarget().isAlive()) {
			setTarget(null);
		}

		if (age % PICKUP_INTERVAL_TICKS == 0) {
			pickUpNearbyItems(owner);
			followOwner(owner);
		}
	}

	public UUID getOwnerUuid() {
		return ownerUuid;
	}

	public boolean isRebel() {
		return dataTracker.get(REBEL);
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		if (ownerUuid != null) {
			nbt.putUuid("Owner", ownerUuid);
		}
		nbt.putInt("ShadowAge", shadowAge);
		nbt.putBoolean("Rebel", isRebel());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		ownerUuid = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
		shadowAge = nbt.getInt("ShadowAge");
		dataTracker.set(REBEL, nbt.getBoolean("Rebel"));
		if (isRebel()) {
			setCustomName(Text.translatable("entity.doraemon_pocket.shadow.rebel"));
			setCustomNameVisible(true);
		}
	}

	private ServerPlayerEntity getOwnerPlayer() {
		if (ownerUuid == null || !(getWorld() instanceof ServerWorld serverWorld)) {
			return null;
		}
		return serverWorld.getServer().getPlayerManager().getPlayer(ownerUuid);
	}

	private LivingEntity findAssistTarget(ServerPlayerEntity owner) {
		LivingEntity target = owner.getAttacking();
		if (isValidAssistTarget(owner, target)) {
			return target;
		}

		target = owner.getAttacker();
		if (isValidAssistTarget(owner, target)) {
			return target;
		}
		return null;
	}

	private boolean isValidAssistTarget(ServerPlayerEntity owner, LivingEntity target) {
		return target != null
				&& target.isAlive()
				&& target != this
				&& target != owner
				&& !(target instanceof PlayerEntity)
				&& target.squaredDistanceTo(owner) <= ASSIST_RANGE * ASSIST_RANGE;
	}

	private void followOwner(ServerPlayerEntity owner) {
		double distance = squaredDistanceTo(owner);
		if (distance > TELEPORT_START_RANGE * TELEPORT_START_RANGE && tryTeleportNearOwner(owner)) {
			getNavigation().stop();
			return;
		}
		if (distance > FOLLOW_START_RANGE * FOLLOW_START_RANGE) {
			getNavigation().startMovingTo(owner, 1.15D);
		} else if (distance < FOLLOW_STOP_RANGE * FOLLOW_STOP_RANGE && getTarget() == null) {
			getNavigation().stop();
		}
	}

	private boolean tryTeleportNearOwner(ServerPlayerEntity owner) {
		if (!(getWorld() instanceof ServerWorld serverWorld) || owner.getWorld() != getWorld()) {
			return false;
		}

		BlockPos ownerPos = owner.getBlockPos();
		for (int i = 0; i < 16; i++) {
			int x = ownerPos.getX() + getRandom().nextBetween(-3, 3);
			int z = ownerPos.getZ() + getRandom().nextBetween(-3, 3);
			int y = ownerPos.getY() + getRandom().nextBetween(-1, 1);
			BlockPos candidate = new BlockPos(x, y, z);
			if (isSafeTeleportTarget(serverWorld, candidate)) {
				refreshPositionAndAngles(x + 0.5D, y, z + 0.5D, getYaw(), getPitch());
				serverWorld.spawnParticles(ParticleTypes.POOF, getX(), getBodyY(0.5D), getZ(), 12, 0.25D, 0.45D, 0.25D, 0.04D);
				serverWorld.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL, 0.35F, 1.4F);
				return true;
			}
		}
		return false;
	}

	private boolean isSafeTeleportTarget(ServerWorld world, BlockPos pos) {
		Box box = getDimensions(getPose()).getBoxAt(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
		return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)
				&& world.isSpaceEmpty(this, box)
				&& world.getBlockState(pos).getFluidState().isEmpty()
				&& world.getBlockState(pos.up()).getFluidState().isEmpty();
	}

	private void pickUpNearbyItems(ServerPlayerEntity owner) {
		Optional<ItemEntity> nearest = getWorld()
				.getEntitiesByClass(ItemEntity.class, getBoundingBox().expand(PICKUP_RANGE), item -> item.isAlive() && !item.getStack().isEmpty())
				.stream()
				.min(Comparator.comparingDouble(this::squaredDistanceTo));
		if (nearest.isEmpty()) {
			return;
		}

		ItemEntity item = nearest.get();
		if (squaredDistanceTo(item) > PICKUP_DISTANCE_SQUARED) {
			getNavigation().startMovingTo(item, 1.2D);
			return;
		}

		ItemStack remaining = item.getStack().copy();
		owner.getInventory().insertStack(remaining);
		if (remaining.isEmpty()) {
			item.discard();
			getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.35F, 1.3F);
		} else {
			item.setStack(remaining);
		}
	}

	private void becomeRebel(ServerPlayerEntity owner) {
		dataTracker.set(REBEL, true);
		setTarget(owner);
		setCustomName(Text.translatable("entity.doraemon_pocket.shadow.rebel"));
		setCustomNameVisible(true);
		getNavigation().stop();
		if (getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getBodyY(0.8D), getZ(), 12, 0.35D, 0.55D, 0.35D, 0.02D);
			serverWorld.playSound(null, getX(), getY(), getZ(), SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.HOSTILE, 0.35F, 1.45F);
		}
	}

	private static final class TextBridge {
		private TextBridge() {
		}

		private static net.minecraft.text.Text shadowSuffix() {
			return net.minecraft.text.Text.translatable("entity.doraemon_pocket.shadow.suffix");
		}
	}

	private static Text friendlyName(ServerPlayerEntity owner) {
		return owner.getDisplayName().copy().append(TextBridge.shadowSuffix());
	}
}
