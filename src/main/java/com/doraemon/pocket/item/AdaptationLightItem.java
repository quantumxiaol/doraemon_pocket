package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.registry.ModStatusEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

public class AdaptationLightItem extends Item {
	private static final double RANGE = 18.0D;
	private static final double RAYCAST_MARGIN = 0.85D;
	private static final int ADAPTATION_DURATION_TICKS = 90 * 60 * 20;
	private static final int COOLDOWN_TICKS = 20;
	private static final DustParticleEffect GREEN_LIGHT = new DustParticleEffect(new Vector3f(0.22F, 0.95F, 0.34F), 0.95F);

	public AdaptationLightItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.35F, 1.65F);
		user.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		user.swingHand(hand, true);

		if (!world.isClient()) {
			if (user.isSneaking()) {
				applyAdaptation(user, user);
				spawnImpactParticles(world, user);
			} else {
				fire(world, user);
			}
		}

		return TypedActionResult.success(stack, world.isClient());
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.adaptation_light.tooltip").formatted(Formatting.GRAY));
	}

	private static void fire(World world, PlayerEntity user) {
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

		EntityHitResult entityHit = ProjectileUtil.raycast(
				user,
				start,
				end,
				user.getBoundingBox().stretch(direction.multiply(start.distanceTo(end))).expand(RAYCAST_MARGIN),
				AdaptationLightItem::canHit,
				start.squaredDistanceTo(end)
		);

		if (entityHit != null) {
			end = entityHit.getPos();
		}

		spawnBeamParticles(world, start, end);

		if (entityHit == null || !(entityHit.getEntity() instanceof LivingEntity target)) {
			return;
		}

		applyAdaptation(target, user);
		spawnImpactParticles(world, target);
		world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.55F, 1.35F);
	}

	private static boolean canHit(Entity entity) {
		return entity instanceof LivingEntity && entity.isAlive() && entity.canHit() && !entity.isSpectator();
	}

	private static void applyAdaptation(LivingEntity entity, Entity source) {
		entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.ENVIRONMENTAL_ADAPTATION, ADAPTATION_DURATION_TICKS, 0, false, true, true), source);
		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, ADAPTATION_DURATION_TICKS, 0, false, true, true), source);
		entity.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, ADAPTATION_DURATION_TICKS, 0, false, true, true), source);
	}

	private static void spawnBeamParticles(World world, Vec3d start, Vec3d end) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		Vec3d path = end.subtract(start);
		int steps = Math.max(4, (int) Math.ceil(path.length() * 1.4D));

		for (int i = 1; i <= steps; i++) {
			Vec3d pos = start.add(path.multiply(i / (double) steps));
			serverWorld.spawnParticles(GREEN_LIGHT, pos.x, pos.y, pos.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
		}
	}

	private static void spawnImpactParticles(World world, LivingEntity target) {
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(GREEN_LIGHT, target.getX(), target.getBodyY(0.65D), target.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.0D);
			serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getBodyY(0.8D), target.getZ(), 8, 0.30D, 0.42D, 0.30D, 0.02D);
		}
	}
}
