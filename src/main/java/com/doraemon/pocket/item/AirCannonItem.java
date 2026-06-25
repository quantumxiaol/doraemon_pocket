package com.doraemon.pocket.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AirCannonItem extends Item {
	private static final double RANGE = 14.0D;
	private static final double START_RADIUS = 0.9D;
	private static final double END_RADIUS = 3.1D;
	private static final double MAX_KNOCKBACK_STRENGTH = 2.2D;
	private static final double MIN_KNOCKBACK_STRENGTH = 0.55D;
	private static final double KNOCKBACK_LIFT = 0.28D;
	private static final float MAX_DAMAGE = 3.0F;
	private static final float MIN_DAMAGE = 1.0F;
	private static final int COOLDOWN_TICKS = 12;

	public AirCannonItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.8F, 1.35F);
		user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		user.swingHand(hand, true);

		if (!world.isClient()) {
			fire(world, user);

			if (!user.isCreative() && user instanceof ServerPlayerEntity serverPlayer) {
				stack.damage(1, serverPlayer, player -> player.sendToolBreakStatus(hand));
			}
		}

		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.air_cannon.tooltip").formatted(Formatting.GRAY));
	}

	private static void fire(World world, net.minecraft.entity.player.PlayerEntity user) {
		Vec3d start = user.getEyePos();
		Vec3d direction = user.getRotationVec(1.0F).normalize();
		Vec3d end = start.add(direction.multiply(RANGE));
		HitResult blockHit = world.raycast(new RaycastContext(
				start,
				end,
				RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE,
				user
		));

		if (blockHit.getType() != HitResult.Type.MISS) {
			end = blockHit.getPos();
		}

		double effectiveRange = start.distanceTo(end);
		spawnAirWaveParticles(world, start, direction, effectiveRange);
		world.getOtherEntities(user, user.getBoundingBox().stretch(direction.multiply(effectiveRange)).expand(END_RADIUS), AirCannonItem::canHit)
				.forEach(target -> hitTarget(world, user, target, start, direction, effectiveRange));
	}

	private static void hitTarget(World world, net.minecraft.entity.player.PlayerEntity user, Entity target, Vec3d start, Vec3d direction, double effectiveRange) {
		Vec3d targetPos = target instanceof LivingEntity livingTarget ? livingTarget.getEyePos() : target.getBoundingBox().getCenter();
		Vec3d targetOffset = targetPos.subtract(start);
		double distance = targetOffset.dotProduct(direction);

		if (distance <= 0.0D || distance > effectiveRange) {
			return;
		}

		Vec3d centerLinePos = start.add(direction.multiply(distance));
		double radius = START_RADIUS + (END_RADIUS - START_RADIUS) * (distance / RANGE) + target.getWidth() * 0.5D;
		double sideDistance = targetPos.distanceTo(centerLinePos);

		if (sideDistance > radius) {
			return;
		}

		double falloff = MathHelper.clamp(1.0D - distance / RANGE, 0.0D, 1.0D);
		float damage = (float) (MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * falloff);
		double knockback = MIN_KNOCKBACK_STRENGTH + (MAX_KNOCKBACK_STRENGTH - MIN_KNOCKBACK_STRENGTH) * falloff;

		target.damage(world.getDamageSources().playerAttack(user), damage);

		Vec3d impulse = direction.multiply(knockback).add(0.0D, KNOCKBACK_LIFT * (0.5D + falloff), 0.0D);
		target.addVelocity(impulse);
		target.velocityModified = true;

		if (target instanceof LivingEntity livingTarget) {
			livingTarget.takeKnockback(0.12D + 0.1D * falloff, -direction.x, -direction.z);
		}
	}

	private static boolean canHit(Entity entity) {
		return entity.isAlive() && entity.canHit() && !entity.isSpectator();
	}

	private static void spawnAirWaveParticles(World world, Vec3d start, Vec3d direction, double effectiveRange) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		int steps = Math.max(4, (int) Math.ceil(effectiveRange));
		for (int i = 1; i <= steps; i++) {
			double distance = effectiveRange * i / steps;
			Vec3d pos = start.add(direction.multiply(distance));
			double spread = START_RADIUS + (END_RADIUS - START_RADIUS) * (distance / RANGE);
			serverWorld.spawnParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 2, spread * 0.15D, spread * 0.08D, spread * 0.15D, 0.01D);
		}
	}
}
