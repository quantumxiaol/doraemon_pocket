package com.doraemon.pocket.item;

import java.util.List;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class CampingCapsuleItem extends Item {
    private static final int MIN_POLE_HEIGHT = 10;
    private static final int EXTRA_POLE_HEIGHT = 6;
    private static final int ROOM_RADIUS = 3;
    private static final int ROOM_HEIGHT = 5;

    public CampingCapsuleItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos base = context.getBlockPos().up();
        if (!world.getBlockState(base).isAir()) {
            return ActionResult.FAIL;
        }

        if (!world.isClient()) {
            int poleHeight = MIN_POLE_HEIGHT + world.getRandom().nextInt(EXTRA_POLE_HEIGHT);
            BlockPos floorCenter = base.up(poleHeight);
            if (!hasSpace(world, base, floorCenter)) {
                if (player != null) {
                    player.sendMessage(Text.translatable("item.doraemon_pocket.camping_capsule.no_space"), true);
                }
                return ActionResult.FAIL;
            }

            Direction facing = player == null ? Direction.SOUTH : player.getHorizontalFacing().getOpposite();
            generateStructure(world, base, floorCenter, facing);
            world.playSound(null, base, SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 1.0F, 1.2F);
            if (player != null && !player.isCreative()) {
                context.getStack().decrement(1);
            }
        }

        return ActionResult.success(world.isClient());
    }

    private boolean hasSpace(World world, BlockPos base, BlockPos floorCenter) {
        for (int y = 0; y <= floorCenter.getY() - base.getY() + ROOM_HEIGHT; y++) {
            BlockPos pos = new BlockPos(base.getX(), base.getY() + y, base.getZ());
            if (!world.getBlockState(pos).isAir()) {
                return false;
            }
        }

        for (int dx = -ROOM_RADIUS; dx <= ROOM_RADIUS; dx++) {
            for (int dy = 0; dy < ROOM_HEIGHT; dy++) {
                for (int dz = -ROOM_RADIUS; dz <= ROOM_RADIUS; dz++) {
                    BlockPos pos = floorCenter.add(dx, dy, dz);
                    if (!world.getBlockState(pos).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void generateStructure(World world, BlockPos base, BlockPos floorCenter, Direction facing) {
        for (int y = 0; y <= floorCenter.getY() - base.getY(); y++) {
            world.setBlockState(base.up(y), Blocks.SCAFFOLDING.getDefaultState(), Block.NOTIFY_ALL);
        }

        for (int dx = -ROOM_RADIUS; dx <= ROOM_RADIUS; dx++) {
            for (int dy = 0; dy < ROOM_HEIGHT; dy++) {
                for (int dz = -ROOM_RADIUS; dz <= ROOM_RADIUS; dz++) {
                    double nx = dx / 3.25D;
                    double ny = (dy - 2.0D) / 2.25D;
                    double nz = dz / 3.25D;
                    double distance = nx * nx + ny * ny + nz * nz;
                    if (distance > 1.18D) {
                        continue;
                    }

                    BlockPos pos = floorCenter.add(dx, dy, dz);
                    boolean shell = distance > 0.64D || dy == 0 || dy == ROOM_HEIGHT - 1;
                    if (shell) {
                        world.setBlockState(pos, shellState(dy), Block.NOTIFY_ALL);
                    } else {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                    }
                }
            }
        }

        world.setBlockState(floorCenter, Blocks.SCAFFOLDING.getDefaultState(), Block.NOTIFY_ALL);
        placeWindow(world, floorCenter, facing);
        placeInterior(world, floorCenter, facing);
    }

    private BlockState shellState(int dy) {
        if (dy == 0) {
            return Blocks.MAGENTA_CONCRETE.getDefaultState();
        }
        return Blocks.SMOOTH_QUARTZ.getDefaultState();
    }

    private void placeWindow(World world, BlockPos floorCenter, Direction facing) {
        Direction right = facing.rotateYClockwise();
        for (int i = -1; i <= 1; i++) {
            for (int y = 2; y <= 3; y++) {
                BlockPos pos = floorCenter.up(y).offset(facing, ROOM_RADIUS).offset(right, i);
                world.setBlockState(pos, Blocks.GLASS.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }

    private void placeInterior(World world, BlockPos floorCenter, Direction facing) {
        Direction bedFacing = facing.rotateYClockwise();
        Direction right = facing.rotateYClockwise();

        BlockPos foot = floorCenter.up(1).offset(facing.getOpposite(), 1).offset(right, -1);
        BlockPos head = foot.offset(bedFacing);
        world.setBlockState(foot, Blocks.WHITE_BED.getDefaultState()
                .with(BedBlock.FACING, bedFacing)
                .with(BedBlock.PART, BedPart.FOOT), Block.NOTIFY_ALL);
        world.setBlockState(head, Blocks.WHITE_BED.getDefaultState()
                .with(BedBlock.FACING, bedFacing)
                .with(BedBlock.PART, BedPart.HEAD), Block.NOTIFY_ALL);

        world.setBlockState(floorCenter.up(1).offset(facing, 1).offset(right, 1), Blocks.CAULDRON.getDefaultState(), Block.NOTIFY_ALL);
        world.setBlockState(floorCenter.up(1).offset(facing, 1).offset(right, -1), Blocks.LANTERN.getDefaultState(), Block.NOTIFY_ALL);
        world.setBlockState(floorCenter.up(4), Blocks.GLOWSTONE.getDefaultState(), Block.NOTIFY_ALL);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.doraemon_pocket.camping_capsule.tooltip").formatted(Formatting.GRAY));
    }
}
