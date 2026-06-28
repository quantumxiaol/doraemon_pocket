package com.doraemon.pocket.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.doraemon.pocket.entity.DoraemonEntity;
import com.doraemon.pocket.registry.ModEntities;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.joml.Vector3f;

public final class DoraemonFlybyEvents {
	private static final int DURATION_TICKS = 150;
	private static final double DRAGON_TRIGGER_RANGE_SQUARED = 192.0D * 192.0D;
	private static final List<Flyby> ACTIVE_FLYBYS = new ArrayList<>();

	private static final Color BLUE = new Color(0.02F, 0.55F, 1.0F);
	private static final Color WHITE = new Color(0.96F, 0.98F, 1.0F);
	private static final Color RED = new Color(1.0F, 0.02F, 0.05F);
	private static final Color BLACK = new Color(0.015F, 0.015F, 0.02F);
	private static final Color YELLOW = new Color(1.0F, 0.86F, 0.16F);
	private static final Color BROWN = new Color(0.48F, 0.28F, 0.08F);
	private static final Color ORANGE = new Color(1.0F, 0.48F, 0.12F);

	private DoraemonFlybyEvents() {
	}

	public static void register() {
		PlayerGadgetTickDispatcher.registerServerTick((server, time) -> tick());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE_FLYBYS.clear());
		ServerLivingEntityEvents.AFTER_DEATH.register(DoraemonFlybyEvents::afterDeath);
	}

	public static void playForPlayer(ServerPlayerEntity player) {
		Vec3d look = horizontalOrFallback(player.getRotationVec(1.0F), new Vec3d(0.0D, 0.0D, 1.0D));
		Vec3d right = new Vec3d(-look.z, 0.0D, look.x).normalize();
		Vec3d viewer = player.getEyePos();
		Vec3d focus = viewer.add(look.multiply(7.0D)).add(0.0D, 1.1D, 0.0D);
		Vec3d start = focus.subtract(right.multiply(8.8D)).add(0.0D, 1.2D, 0.0D);
		Vec3d control = focus.add(look.multiply(1.8D)).add(0.0D, 3.1D, 0.0D);
		Vec3d end = focus.add(right.multiply(8.8D)).add(0.0D, 0.2D, 0.0D);
		start(player.getServerWorld(), start, control, end, viewer);
	}

	private static void afterDeath(LivingEntity entity, DamageSource source) {
		if (!(entity instanceof EnderDragonEntity) || !(entity.getWorld() instanceof ServerWorld world)) {
			return;
		}

		spawnDoraemonCompanion(world, entity.getPos());

		ServerPlayerEntity viewer = findViewer(world, entity, source);
		if (viewer != null) {
			playForPlayer(viewer);
			return;
		}

		Vec3d center = entity.getPos().add(0.0D, 5.0D, 0.0D);
		Vec3d start = center.add(-10.5D, 3.0D, 0.0D);
		Vec3d control = center.add(0.0D, 6.2D, 2.5D);
		Vec3d end = center.add(10.5D, 0.8D, 0.0D);
		start(world, start, control, end, center.add(0.0D, 0.0D, -8.0D));
	}

	private static void spawnDoraemonCompanion(ServerWorld world, Vec3d center) {
		BlockPos spawnPos = findDoraemonSpawnPos(world, center);
		DoraemonEntity doraemon = new DoraemonEntity(ModEntities.DORAEMON, world);
		doraemon.refreshPositionAndAngles(
				spawnPos.getX() + 0.5D,
				spawnPos.getY() + 0.1D,
				spawnPos.getZ() + 0.5D,
				world.random.nextFloat() * 360.0F,
				0.0F
		);
		doraemon.setCustomName(Text.translatable("entity.doraemon_pocket.doraemon"));
		doraemon.setCustomNameVisible(true);
		world.spawnEntity(doraemon);
		world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, doraemon.getX(), doraemon.getBodyY(0.75D), doraemon.getZ(), 24, 0.45D, 0.45D, 0.45D, 0.04D);
		world.playSound(null, doraemon.getX(), doraemon.getY(), doraemon.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.NEUTRAL, 0.7F, 1.55F);
	}

	private static BlockPos findDoraemonSpawnPos(ServerWorld world, Vec3d center) {
		int centerX = MathHelper.floor(center.x);
		int centerZ = MathHelper.floor(center.z);
		for (int radius = 0; radius <= 12; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
						continue;
					}
					BlockPos column = new BlockPos(centerX + dx, world.getBottomY(), centerZ + dz);
					BlockPos top = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, column);
					if (isClearSpawnPos(world, top)) {
						return top;
					}
				}
			}
		}
		return BlockPos.ofFloored(center.x, Math.max(world.getBottomY() + 8.0D, center.y), center.z);
	}

	private static boolean isClearSpawnPos(ServerWorld world, BlockPos pos) {
		if (pos.getY() <= world.getBottomY() || pos.getY() >= world.getTopY() - 2) {
			return false;
		}
		return world.getBlockState(pos.down()).isSolidBlock(world, pos.down())
				&& world.getBlockState(pos).isAir()
				&& world.getBlockState(pos.up()).isAir()
				&& world.getFluidState(pos).isEmpty()
				&& world.getFluidState(pos.up()).isEmpty();
	}

	private static ServerPlayerEntity findViewer(ServerWorld world, LivingEntity killed, DamageSource source) {
		Entity attacker = source.getAttacker();
		if (attacker instanceof ServerPlayerEntity player && player.getServerWorld() == world) {
			return player;
		}

		ServerPlayerEntity nearest = null;
		double nearestDistance = DRAGON_TRIGGER_RANGE_SQUARED;
		for (ServerPlayerEntity player : world.getPlayers(player -> !player.isSpectator())) {
			double distance = player.squaredDistanceTo(killed);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = player;
			}
		}
		return nearest;
	}

	private static void start(ServerWorld world, Vec3d start, Vec3d control, Vec3d end, Vec3d viewer) {
		ACTIVE_FLYBYS.add(new Flyby(world, start, control, end, viewer));
		world.playSound(null, start.x, start.y, start.z, SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.PLAYERS, 0.7F, 1.8F);
	}

	private static void tick() {
		Iterator<Flyby> iterator = ACTIVE_FLYBYS.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().tick()) {
				iterator.remove();
			}
		}
	}

	private static Vec3d horizontalOrFallback(Vec3d vector, Vec3d fallback) {
		Vec3d horizontal = new Vec3d(vector.x, 0.0D, vector.z);
		return horizontal.lengthSquared() > 1.0E-4D ? horizontal.normalize() : fallback.normalize();
	}

	private static final class Flyby {
		private final ServerWorld world;
		private final Vec3d start;
		private final Vec3d control;
		private final Vec3d end;
		private final Vec3d viewer;
		private int age;

		private Flyby(ServerWorld world, Vec3d start, Vec3d control, Vec3d end, Vec3d viewer) {
			this.world = world;
			this.start = start;
			this.control = control;
			this.end = end;
			this.viewer = viewer;
		}

		private boolean tick() {
			if (age >= DURATION_TICKS) {
				burst(end);
				return true;
			}

			double t = (double) age / (double) DURATION_TICKS;
			double eased = MathHelper.sin((float) (t * Math.PI * 0.5D));
			Vec3d position = quadratic(start, control, end, eased);
			Vec3d tangent = quadraticTangent(start, control, end, eased).normalize();
			render(position, tangent, t);
			if (age % 7 == 0) {
				world.playSound(null, position.x, position.y, position.z, SoundEvents.ENTITY_PHANTOM_FLAP, SoundCategory.PLAYERS, 0.22F, 1.9F);
			}
			age++;
			return false;
		}

		private void render(Vec3d position, Vec3d tangent, double t) {
			Vec3d face = viewer.subtract(position);
			if (face.lengthSquared() < 1.0E-4D) {
				face = tangent.negate();
			}
			face = face.normalize();
			Vec3d up = new Vec3d(0.0D, 1.0D, 0.0D);
			Vec3d right = up.crossProduct(face);
			if (right.lengthSquared() < 1.0E-4D) {
				right = new Vec3d(1.0D, 0.0D, 0.0D);
			} else {
				right = right.normalize();
			}
			up = face.crossProduct(right).normalize();

			double bob = Math.sin(t * Math.PI * 8.0D) * 0.08D;
			Vec3d center = position.add(up.multiply(bob));
			renderBody(center, right, up, face);
			renderFace(center, right, up, face);
			renderBambooCopter(center, right, up, face, t);
			renderTrail(position, tangent, right, up, face, t);
		}

		private void renderBody(Vec3d center, Vec3d right, Vec3d up, Vec3d face) {
			for (int i = 0; i < 16; i++) {
				double a = Math.PI * 2.0D * i / 16.0D;
				double x = Math.cos(a) * 0.78D;
				double y = 0.03D + Math.sin(a) * 0.85D;
				plot(center, right, up, face, x, y, 0.14D, BLUE, 1.0F);
			}
			drawLine(center, right, up, face, -0.52D, -0.44D, 0.92D, 0.52D, -0.44D, 0.92D, RED, 6, 0.72F);
			for (int i = 0; i < 14; i++) {
				double a = Math.PI * 2.0D * i / 14.0D;
				plot(center, right, up, face, Math.cos(a) * 0.54D, -0.75D + Math.sin(a) * 0.30D, 0.78D, WHITE, 1.05F);
			}
			plot(center, right, up, face, 0.0D, -0.75D, 0.86D, WHITE, 1.15F);
			drawLine(center, right, up, face, -0.30D, -0.78D, 1.00D, 0.30D, -0.78D, 1.00D, BLACK, 4, 0.35F);
			for (int i = 0; i < 7; i++) {
				double a = Math.PI + Math.PI * i / 6.0D;
				plot(center, right, up, face, Math.cos(a) * 0.30D, -0.81D + Math.sin(a) * 0.16D, 1.00D, BLACK, 0.35F);
			}
			plot(center, right, up, face, 0.0D, -0.53D, 1.00D, YELLOW, 1.25F);
			plot(center, right, up, face, 0.0D, -0.53D, 1.14D, BLACK, 0.55F);
			plot(center, right, up, face, -0.72D, -0.18D, 0.28D, WHITE, 0.85F);
			plot(center, right, up, face, 0.72D, -0.18D, 0.28D, WHITE, 0.85F);
		}

		private void renderFace(Vec3d center, Vec3d right, Vec3d up, Vec3d face) {
			for (int i = 0; i < 12; i++) {
				double a = Math.PI * 2.0D * i / 12.0D;
				plot(center, right, up, face, Math.cos(a) * 0.55D, 0.28D + Math.sin(a) * 0.42D, 0.56D, WHITE, 1.05F);
			}
			plot(center, right, up, face, -0.24D, 0.61D, 0.78D, WHITE, 1.35F);
			plot(center, right, up, face, 0.24D, 0.61D, 0.78D, WHITE, 1.35F);
			plot(center, right, up, face, -0.13D, 0.57D, 0.96D, BLACK, 0.58F);
			plot(center, right, up, face, 0.13D, 0.57D, 0.96D, BLACK, 0.58F);
			plot(center, right, up, face, 0.0D, 0.25D, 1.02D, RED, 1.65F);
			plot(center, right, up, face, 0.09D, 0.34D, 1.14D, WHITE, 0.55F);
			drawLine(center, right, up, face, -0.08D, 0.19D, 1.02D, -0.62D, 0.35D, 0.95D, BLACK, 3, 0.45F);
			drawLine(center, right, up, face, -0.10D, 0.11D, 1.02D, -0.68D, 0.12D, 0.96D, BLACK, 3, 0.45F);
			drawLine(center, right, up, face, -0.08D, 0.03D, 1.02D, -0.62D, -0.10D, 0.95D, BLACK, 3, 0.45F);
			drawLine(center, right, up, face, 0.08D, 0.19D, 1.02D, 0.62D, 0.35D, 0.95D, BLACK, 3, 0.45F);
			drawLine(center, right, up, face, 0.10D, 0.11D, 1.02D, 0.68D, 0.12D, 0.96D, BLACK, 3, 0.45F);
			drawLine(center, right, up, face, 0.08D, 0.03D, 1.02D, 0.62D, -0.10D, 0.95D, BLACK, 3, 0.45F);
			drawLine(center, right, up, face, 0.0D, 0.13D, 1.03D, 0.0D, -0.38D, 1.0D, BLACK, 4, 0.42F);
			drawLine(center, right, up, face, -0.38D, -0.34D, 0.95D, 0.38D, -0.34D, 0.95D, RED, 5, 0.7F);
			plot(center, right, up, face, 0.0D, -0.47D, 1.02D, ORANGE, 0.85F);
		}

		private void renderBambooCopter(Vec3d center, Vec3d right, Vec3d up, Vec3d face, double t) {
			drawLine(center, right, up, face, 0.0D, 0.94D, 0.14D, 0.0D, 1.32D, 0.14D, BROWN, 4, 0.42F);
			double rotation = t * Math.PI * 28.0D;
			for (int blade = 0; blade < 2; blade++) {
				double a = rotation + blade * Math.PI;
				for (int i = 0; i < 4; i++) {
					double r = 0.28D + i * 0.18D;
					double x = Math.cos(a) * r;
					double z = 0.14D + Math.sin(a) * r * 0.38D;
					plot(center, right, up, face, x, 1.40D, z, YELLOW, 0.75F);
				}
			}
			plot(center, right, up, face, 0.0D, 1.37D, 0.14D, BROWN, 0.55F);
		}

		private void renderTrail(Vec3d position, Vec3d tangent, Vec3d right, Vec3d up, Vec3d face, double t) {
			for (int i = 1; i <= 8; i++) {
				Vec3d trail = position.subtract(tangent.multiply(i * 0.35D)).add(up.multiply(Math.sin(t * 18.0D + i) * 0.08D));
				Color color = i % 2 == 0 ? BLUE : WHITE;
				spawn(trail, color, 0.45F);
			}
		}

		private void burst(Vec3d position) {
			for (int i = 0; i < 36; i++) {
				double a = Math.PI * 2.0D * i / 36.0D;
				double radius = 0.9D + (i % 4) * 0.18D;
				Vec3d p = position.add(Math.cos(a) * radius, Math.sin(i * 1.7D) * 0.35D, Math.sin(a) * radius);
				spawn(p, i % 3 == 0 ? YELLOW : (i % 2 == 0 ? BLUE : WHITE), 0.9F);
			}
			world.spawnParticles(ParticleTypes.FIREWORK, position.x, position.y, position.z, 18, 0.55D, 0.45D, 0.55D, 0.03D);
			world.playSound(null, position.x, position.y, position.z, SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.PLAYERS, 0.8F, 1.25F);
		}

		private void drawLine(Vec3d center, Vec3d right, Vec3d up, Vec3d face, double x1, double y1, double z1, double x2, double y2, double z2, Color color, int points, float scale) {
			for (int i = 0; i < points; i++) {
				double amount = points == 1 ? 0.0D : (double) i / (double) (points - 1);
				plot(
						center,
						right,
						up,
						face,
						MathHelper.lerp(amount, x1, x2),
						MathHelper.lerp(amount, y1, y2),
						MathHelper.lerp(amount, z1, z2),
						color,
						scale
				);
			}
		}

		private void plot(Vec3d center, Vec3d right, Vec3d up, Vec3d face, double x, double y, double z, Color color, float scale) {
			spawn(center.add(right.multiply(x)).add(up.multiply(y)).add(face.multiply(z)), color, scale);
		}

		private void spawn(Vec3d position, Color color, float scale) {
			world.spawnParticles(new DustParticleEffect(new Vector3f(color.r(), color.g(), color.b()), scale), position.x, position.y, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}

		private static Vec3d quadratic(Vec3d start, Vec3d control, Vec3d end, double t) {
			double inv = 1.0D - t;
			return start.multiply(inv * inv).add(control.multiply(2.0D * inv * t)).add(end.multiply(t * t));
		}

		private static Vec3d quadraticTangent(Vec3d start, Vec3d control, Vec3d end, double t) {
			return control.subtract(start).multiply(2.0D * (1.0D - t)).add(end.subtract(control).multiply(2.0D * t));
		}
	}

	private record Color(float r, float g, float b) {
	}
}
