package com.doraemon.pocket.entity;

import java.util.ArrayList;
import java.util.List;

import com.doraemon.pocket.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DoraemonEntity extends PathAwareEntity {
	private int hoverSeed;

	public DoraemonEntity(EntityType<? extends DoraemonEntity> entityType, World world) {
		super(entityType, world);
		hoverSeed = random.nextInt(628);
		setNoGravity(true);
		setPersistent();
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.24D)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0D)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.45D);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(6, new WanderAroundFarGoal(this, 0.55D));
		goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		goalSelector.add(8, new LookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		setNoGravity(true);
		if (!getWorld().isClient() && !isAiDisabled()) {
			tickHoverFlight();
		}
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!stack.isOf(ModItems.DORAYAKI)) {
			if (!getWorld().isClient()) {
				player.sendMessage(Text.translatable("message.doraemon_pocket.doraemon.want_dorayaki"), true);
			}
			return ActionResult.success(getWorld().isClient());
		}

		if (getWorld().isClient()) {
			return ActionResult.SUCCESS;
		}

		List<DoraemonTrade> offers = affordableOffers(stack.getCount());
		if (offers.isEmpty()) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.doraemon.need_more_dorayaki"), true);
			return ActionResult.CONSUME;
		}

		DoraemonTrade offer = offers.get(random.nextInt(offers.size()));
		if (!player.isCreative()) {
			stack.decrement(offer.cost());
		}

		ItemStack reward = new ItemStack(offer.item(), offer.count());
		boolean inserted = player.getInventory().insertStack(reward);
		if (!inserted || !reward.isEmpty()) {
			player.dropItem(reward, false);
		}

		player.sendMessage(Text.translatable("message.doraemon_pocket.doraemon.trade", offer.cost(), new ItemStack(offer.item(), offer.count()).getName()), true);
		if (getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getBodyY(0.75D), getZ(), 12, 0.35D, 0.35D, 0.35D, 0.04D);
		}
		getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.NEUTRAL, 0.55F, 1.45F);
		return ActionResult.CONSUME;
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("HoverSeed", hoverSeed);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		hoverSeed = nbt.getInt("HoverSeed");
	}

	protected void tickHoverFlight() {
		double hover = Math.sin((age + hoverSeed) * 0.12D) * 0.018D;
		Vec3d velocity = getVelocity();
		double x = velocity.x * 0.88D;
		double z = velocity.z * 0.88D;
		if (age % 80 == 0) {
			float yaw = random.nextFloat() * 360.0F;
			setYaw(yaw);
			double speed = isMiniDoraemon() ? 0.075D : 0.045D;
			x += -Math.sin(Math.toRadians(yaw)) * speed;
			z += Math.cos(Math.toRadians(yaw)) * speed;
		}
		setVelocity(x, hover, z);
		velocityModified = true;
	}

	protected boolean isMiniDoraemon() {
		return false;
	}

	private List<DoraemonTrade> affordableOffers(int dorayakiCount) {
		List<DoraemonTrade> offers = new ArrayList<>();
		addIfAffordable(offers, dorayakiCount, 1, ModItems.MOMOTARO_DUMPLING, 4);
		addIfAffordable(offers, dorayakiCount, 1, ModItems.TRANSLATION_GUMMY, 2);
		addIfAffordable(offers, dorayakiCount, 1, ModItems.DEEP_SEA_CREAM, 2);
		addIfAffordable(offers, dorayakiCount, 2, ModItems.BAMBOO_COPTER, 1);
		addIfAffordable(offers, dorayakiCount, 2, ModItems.PLANT_MODIFICATION_LIQUID, 2);
		addIfAffordable(offers, dorayakiCount, 2, ModItems.CLOUD_HARDENING_GAS, 1);
		addIfAffordable(offers, dorayakiCount, 3, ModItems.AIR_CANNON, 1);
		addIfAffordable(offers, dorayakiCount, 3, ModItems.SHOCK_GUN, 1);
		addIfAffordable(offers, dorayakiCount, 3, ModItems.FAST_FORWARD_WINDER, 1);
		addIfAffordable(offers, dorayakiCount, 4, ModItems.TIME_CLOTH, 1);
		addIfAffordable(offers, dorayakiCount, 4, ModItems.DODGE_CLOAK, 1);
		addIfAffordable(offers, dorayakiCount, 4, ModItems.WEATHER_BOX, 1);
		addIfAffordable(offers, dorayakiCount, 6, ModItems.ANYWHERE_DOOR, 1);
		addIfAffordable(offers, dorayakiCount, 6, ModItems.PASS_LOOP, 1);
		addIfAffordable(offers, dorayakiCount, 6, ModItems.RADAR_SWORD, 1);
		addIfAffordable(offers, dorayakiCount, 8, ModItems.FOUR_DIMENSIONAL_POCKET, 1);
		return offers;
	}

	private void addIfAffordable(List<DoraemonTrade> offers, int dorayakiCount, int cost, Item item, int count) {
		if (dorayakiCount >= cost) {
			offers.add(new DoraemonTrade(cost, item, count));
		}
	}

	private record DoraemonTrade(int cost, Item item, int count) {
	}
}
