package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.event.ShockGunStunHandler;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShockGunItem extends Item {
	private static final double RANGE = 12.0D;
	private static final double RAYCAST_MARGIN = 0.75D;
	private static final int EFFECT_TICKS = 100;
	private static final int AI_STUN_TICKS = 100;
	private static final int COOLDOWN_TICKS = 18;

	public ShockGunItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.35F, 1.75F);
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
		tooltip.add(Text.translatable("item.doraemon_pocket.shock_gun.tooltip").formatted(Formatting.GRAY));
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

		EntityHitResult entityHit = ProjectileUtil.raycast(
				user,
				start,
				end,
				user.getBoundingBox().stretch(direction.multiply(start.distanceTo(end))).expand(RAYCAST_MARGIN),
				ShockGunItem::canHit,
				start.squaredDistanceTo(end)
		);

		spawnBeamParticles(world, start, end);

		if (entityHit == null || !(entityHit.getEntity() instanceof LivingEntity target)) {
			return;
		}

		target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_TICKS, 9, false, true, true), user);
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, EFFECT_TICKS, -10, false, true, true), user);
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, EFFECT_TICKS, 4, false, true, true), user);

		if (target instanceof MobEntity mob) {
			mob.setTarget(null);
			mob.getNavigation().stop();
			ShockGunStunHandler.stun(mob, AI_STUN_TICKS);
			mob.setAiDisabled(true);
		}

		if (world instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getEyeY(), target.getZ(), 18, 0.45D, 0.55D, 0.45D, 0.08D);
		}
	}

	private static boolean canHit(Entity entity) {
		return entity instanceof LivingEntity && entity.isAlive() && entity.canHit() && !entity.isSpectator();
	}

	private static void spawnBeamParticles(World world, Vec3d start, Vec3d end) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		Vec3d path = end.subtract(start);
		int steps = Math.max(3, (int) Math.ceil(path.length()));

		for (int i = 1; i <= steps; i++) {
			Vec3d pos = start.add(path.multiply(i / (double) steps));
			serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 1, 0.03D, 0.03D, 0.03D, 0.01D);
		}
	}
}
