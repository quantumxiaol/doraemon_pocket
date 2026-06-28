package com.doraemon.pocket.item;

import java.util.List;

import com.doraemon.pocket.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CloudHardeningGasItem extends Item {
	private static final int MIN_USE_Y = 192;
	private static final int MIN_DIAMETER = 3;
	private static final int MAX_DIAMETER = 64;
	private static final int COOLDOWN_TICKS = 50;
	private static final double CLOUD_CENTER_RANGE = 10.0D;
	private static final int MAX_PLACED_BLOCKS = 14000;
	private static final int MASS_PLACE_FLAGS = Block.NOTIFY_LISTENERS | Block.FORCE_STATE;

	public CloudHardeningGasItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (world.isClient()) {
			return TypedActionResult.success(stack);
		}

		if (!(world instanceof ServerWorld serverWorld) || !(user instanceof ServerPlayerEntity player)) {
			return TypedActionResult.pass(stack);
		}

		if (player.getBlockY() < MIN_USE_Y) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.cloud_hardening_gas.too_low", MIN_USE_Y), true);
			serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.35F, 1.55F);
			return TypedActionResult.fail(stack);
		}

		BlockPos center = chooseCloudCenter(serverWorld, player);
		int placed = generateCloud(serverWorld, center);
		if (placed <= 0) {
			player.sendMessage(Text.translatable("message.doraemon_pocket.cloud_hardening_gas.no_space"), true);
			return TypedActionResult.fail(stack);
		}

		player.getItemCooldownManager().set(this, COOLDOWN_TICKS);
		player.swingHand(hand, true);
		if (!player.isCreative()) {
			stack.damage(1, player, p -> p.sendToolBreakStatus(hand));
		}

		serverWorld.playSound(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, SoundEvents.ENTITY_LLAMA_SPIT, SoundCategory.PLAYERS, 0.85F, 0.55F);
		serverWorld.playSound(null, center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.72F);
		serverWorld.spawnParticles(ParticleTypes.CLOUD, center.getX() + 0.5D, center.getY() + 0.7D, center.getZ() + 0.5D, Math.min(96, placed / 18 + 24), 2.5D, 0.7D, 2.5D, 0.03D);
		player.sendMessage(Text.translatable("message.doraemon_pocket.cloud_hardening_gas.generated", placed), true);
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.doraemon_pocket.cloud_hardening_gas.tooltip").formatted(Formatting.GRAY));
	}

	private static BlockPos chooseCloudCenter(ServerWorld world, ServerPlayerEntity player) {
		Vec3d look = player.getRotationVec(1.0F).normalize();
		Vec3d center = player.getEyePos().add(look.multiply(CLOUD_CENTER_RANGE));
		int y = MathHelper.clamp(MathHelper.floor(center.y - 0.45D), MIN_USE_Y, world.getTopY() - 5);
		return BlockPos.ofFloored(center.x, y, center.z);
	}

	private static int generateCloud(ServerWorld world, BlockPos center) {
		long seed = world.getSeed() ^ center.asLong();
		double sizeSample = normalizedNoise(seed, center.getX() * 0.012D, center.getY() * 0.017D, center.getZ() * 0.012D, 4);
		int diameter = MIN_DIAMETER + MathHelper.floor(Math.pow(sizeSample, 1.35D) * (MAX_DIAMETER - MIN_DIAMETER));
		double radiusX = Math.max(1.5D, diameter / 2.0D);
		double stretch = MathHelper.lerp(normalizedNoise(seed + 41L, center.getX() * 0.019D, 0.0D, center.getZ() * 0.019D, 3), 0.68D, 1.28D);
		double radiusZ = Math.max(1.5D, radiusX * stretch);
		int maxX = MathHelper.ceil(radiusX + 2.0D);
		int maxZ = MathHelper.ceil(radiusZ + 2.0D);
		int placed = 0;
		BlockState cloud = ModBlocks.SOLIDIFIED_CLOUD.getDefaultState();

		for (int dx = -maxX; dx <= maxX; dx++) {
			for (int dz = -maxZ; dz <= maxZ; dz++) {
				double nx = dx / radiusX;
				double nz = dz / radiusZ;
				double distance = nx * nx + nz * nz;
				double edge = signedNoise(seed + 97L, (center.getX() + dx) * 0.085D, center.getY() * 0.045D, (center.getZ() + dz) * 0.085D, 4);
				double puff = 1.0D + edge * 0.38D;
				if (distance > puff) {
					continue;
				}

				double heightNoise = signedNoise(seed + 151L, (center.getX() + dx) * 0.055D, center.getY() * 0.04D, (center.getZ() + dz) * 0.055D, 3);
				int surfaceY = center.getY() + MathHelper.clamp(MathHelper.floor(heightNoise * 2.1D), -2, 2);
				int thickness = 1 + MathHelper.floor(Math.max(0.0D, 1.0D - distance) * 2.7D);
				thickness += normalizedNoise(seed + 211L, (center.getX() + dx) * 0.12D, surfaceY * 0.06D, (center.getZ() + dz) * 0.12D, 2) > 0.72D ? 1 : 0;
				thickness = MathHelper.clamp(thickness, 1, 4);

				for (int dy = 0; dy < thickness; dy++) {
					BlockPos pos = center.add(dx, surfaceY - center.getY() - dy, dz);
					if (pos.getY() < world.getBottomY() || pos.getY() >= world.getTopY()) {
						continue;
					}
					if (!world.getBlockState(pos).isAir()) {
						continue;
					}
					world.setBlockState(pos, cloud, MASS_PLACE_FLAGS);
					placed++;
					if (placed >= MAX_PLACED_BLOCKS) {
						return placed;
					}
				}
			}
		}

		return placed;
	}

	private static double signedNoise(long seed, double x, double y, double z, int octaves) {
		return normalizedNoise(seed, x, y, z, octaves) * 2.0D - 1.0D;
	}

	private static double normalizedNoise(long seed, double x, double y, double z, int octaves) {
		double total = 0.0D;
		double amplitude = 1.0D;
		double frequency = 1.0D;
		double max = 0.0D;
		for (int i = 0; i < octaves; i++) {
			total += smoothNoise(seed + i * 1013L, x * frequency, y * frequency, z * frequency) * amplitude;
			max += amplitude;
			amplitude *= 0.5D;
			frequency *= 2.0D;
		}
		return MathHelper.clamp(total / max, 0.0D, 1.0D);
	}

	private static double smoothNoise(long seed, double x, double y, double z) {
		int x0 = MathHelper.floor(x);
		int y0 = MathHelper.floor(y);
		int z0 = MathHelper.floor(z);
		double fx = fade(x - x0);
		double fy = fade(y - y0);
		double fz = fade(z - z0);

		double x00 = MathHelper.lerp(fx, lattice(seed, x0, y0, z0), lattice(seed, x0 + 1, y0, z0));
		double x10 = MathHelper.lerp(fx, lattice(seed, x0, y0 + 1, z0), lattice(seed, x0 + 1, y0 + 1, z0));
		double x01 = MathHelper.lerp(fx, lattice(seed, x0, y0, z0 + 1), lattice(seed, x0 + 1, y0, z0 + 1));
		double x11 = MathHelper.lerp(fx, lattice(seed, x0, y0 + 1, z0 + 1), lattice(seed, x0 + 1, y0 + 1, z0 + 1));
		double y0v = MathHelper.lerp(fy, x00, x10);
		double y1v = MathHelper.lerp(fy, x01, x11);
		return MathHelper.lerp(fz, y0v, y1v);
	}

	private static double fade(double value) {
		return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
	}

	private static double lattice(long seed, int x, int y, int z) {
		long value = seed;
		value ^= x * 0x9E3779B97F4A7C15L;
		value ^= y * 0xC2B2AE3D27D4EB4FL;
		value ^= z * 0x165667B19E3779F9L;
		value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
		value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
		value ^= value >>> 31;
		return (value & 0x1FFFFFL) / (double) 0x1FFFFF;
	}
}
