package com.doraemon.pocket.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WoodcuttersSpringBlock extends Block {
    public WoodcuttersSpringBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient() || !(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack reward = getReward(itemEntity.getStack());
        if (reward.isEmpty()) {
            return;
        }

        ItemStack source = itemEntity.getStack();
        source.decrement(1);
        if (source.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setStack(source);
        }

        Vec3d center = pos.toCenterPos();
        ItemEntity rewardEntity = new ItemEntity(world, center.x, pos.getY() + 1.1D, center.z, reward);
        rewardEntity.setVelocity(0.0D, 0.35D, 0.0D);
        world.spawnEntity(rewardEntity);

        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.spawnParticles(ParticleTypes.HAPPY_VILLAGER, center.x, pos.getY() + 1.0D, center.z, 24, 0.35D, 0.35D, 0.35D, 0.03D);
        world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.8F, 1.4F);
        for (PlayerEntity player : world.getPlayers()) {
            if (player.squaredDistanceTo(center) <= 16.0D * 16.0D) {
                player.sendMessage(Text.translatable("block.doraemon_pocket.woodcutters_spring.reward"), true);
            }
        }
    }

    private ItemStack getReward(ItemStack input) {
        if (!input.isDamageable() || input.getDamage() <= 0) {
            return ItemStack.EMPTY;
        }

        if (input.isOf(Items.IRON_PICKAXE)) {
            return new ItemStack(Items.DIAMOND_PICKAXE);
        }
        if (input.isOf(Items.IRON_AXE)) {
            return new ItemStack(Items.DIAMOND_AXE);
        }
        if (input.isOf(Items.IRON_SHOVEL)) {
            return new ItemStack(Items.DIAMOND_SHOVEL);
        }
        if (input.isOf(Items.IRON_SWORD)) {
            return new ItemStack(Items.DIAMOND_SWORD);
        }
        if (input.isOf(Items.IRON_HOE)) {
            return new ItemStack(Items.DIAMOND_HOE);
        }
        if (input.isOf(Items.GOLDEN_PICKAXE) || input.isOf(Items.STONE_PICKAXE) || input.isOf(Items.WOODEN_PICKAXE)) {
            return new ItemStack(Items.IRON_PICKAXE);
        }
        if (input.isOf(Items.GOLDEN_AXE) || input.isOf(Items.STONE_AXE) || input.isOf(Items.WOODEN_AXE)) {
            return new ItemStack(Items.IRON_AXE);
        }
        if (input.isOf(Items.GOLDEN_SHOVEL) || input.isOf(Items.STONE_SHOVEL) || input.isOf(Items.WOODEN_SHOVEL)) {
            return new ItemStack(Items.IRON_SHOVEL);
        }
        if (input.isOf(Items.GOLDEN_SWORD) || input.isOf(Items.STONE_SWORD) || input.isOf(Items.WOODEN_SWORD)) {
            return new ItemStack(Items.IRON_SWORD);
        }
        if (input.isOf(Items.GOLDEN_HOE) || input.isOf(Items.STONE_HOE) || input.isOf(Items.WOODEN_HOE)) {
            return new ItemStack(Items.IRON_HOE);
        }

        return ItemStack.EMPTY;
    }
}
