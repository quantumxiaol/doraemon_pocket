package com.doraemon.pocket.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.Formatting;

import org.jetbrains.annotations.Nullable;

public class RestoringFlashlightItem extends Item {
    private static final double RANGE = 12.0D;

    public RestoringFlashlightItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            return TypedActionResult.success(stack);
        }

        ItemEntity target = findTargetItem(world, user);
        if (target == null) {
            user.sendMessage(Text.translatable("item.doraemon_pocket.restoring_flashlight.no_target"), true);
            return TypedActionResult.fail(stack);
        }

        List<ItemStack> restored = restore((ServerWorld) world, target.getStack());
        if (restored.isEmpty()) {
            user.sendMessage(Text.translatable("item.doraemon_pocket.restoring_flashlight.no_recipe"), true);
            return TypedActionResult.fail(stack);
        }

        Vec3d pos = target.getPos();
        consumeOne(target);
        for (ItemStack restoredStack : restored) {
            ItemEntity drop = new ItemEntity(world, pos.x, pos.y + 0.2D, pos.z, restoredStack);
            drop.setVelocity((world.random.nextDouble() - 0.5D) * 0.2D, 0.25D, (world.random.nextDouble() - 0.5D) * 0.2D);
            world.spawnEntity(drop);
        }

        ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD, pos.x, pos.y + 0.25D, pos.z, 18, 0.25D, 0.2D, 0.25D, 0.02D);
        world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.8F, 1.5F);
        if (!user.isCreative()) {
            stack.damage(1, user, p -> p.sendToolBreakStatus(hand));
        }
        user.getItemCooldownManager().set(this, 15);

        return TypedActionResult.success(stack);
    }

    private ItemEntity findTargetItem(World world, PlayerEntity user) {
        Vec3d start = user.getCameraPosVec(1.0F);
        Vec3d rotation = user.getRotationVec(1.0F);
        Vec3d end = start.add(rotation.multiply(RANGE));
        HitResult blockHit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
        double maxDistanceSquared = blockHit.getType() == HitResult.Type.MISS ? RANGE * RANGE : blockHit.getPos().squaredDistanceTo(start);
        Box box = user.getBoundingBox().stretch(rotation.multiply(RANGE)).expand(1.0D);
        EntityHitResult entityHit = ProjectileUtil.raycast(user, start, end, box,
                entity -> entity instanceof ItemEntity && entity.isAlive(), maxDistanceSquared);
        if (entityHit == null) {
            return null;
        }
        Entity entity = entityHit.getEntity();
        return entity instanceof ItemEntity itemEntity ? itemEntity : null;
    }

    private List<ItemStack> restore(ServerWorld world, ItemStack target) {
        List<ItemStack> manual = manualRestore(target);
        if (!manual.isEmpty()) {
            return manual;
        }

        for (CraftingRecipe recipe : world.getRecipeManager().listAllOfType(RecipeType.CRAFTING)) {
            ItemStack output = recipe.getOutput(world.getRegistryManager());
            if (!ItemStack.areItemsEqual(output, target)) {
                continue;
            }

            List<ItemStack> ingredients = new ArrayList<>();
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.isEmpty()) {
                    continue;
                }
                ItemStack[] matchingStacks = ingredient.getMatchingStacks();
                if (matchingStacks.length == 0) {
                    continue;
                }
                ItemStack ingredientStack = matchingStacks[0].copy();
                ingredientStack.setCount(1);
                ingredients.add(ingredientStack);
            }
            if (!ingredients.isEmpty()) {
                return ingredients;
            }
        }

        return List.of();
    }

    private List<ItemStack> manualRestore(ItemStack target) {
        if (target.isOf(Items.COOKED_BEEF)) {
            return List.of(new ItemStack(Items.BEEF));
        }
        if (target.isOf(Items.COOKED_PORKCHOP)) {
            return List.of(new ItemStack(Items.PORKCHOP));
        }
        if (target.isOf(Items.COOKED_CHICKEN)) {
            return List.of(new ItemStack(Items.CHICKEN));
        }
        if (target.isOf(Items.COOKED_MUTTON)) {
            return List.of(new ItemStack(Items.MUTTON));
        }
        if (target.isOf(Items.COOKED_RABBIT)) {
            return List.of(new ItemStack(Items.RABBIT));
        }
        if (target.isOf(Items.BAKED_POTATO)) {
            return List.of(new ItemStack(Items.POTATO));
        }
        if (target.isOf(Items.CHARCOAL)) {
            return List.of(new ItemStack(Items.OAK_LOG));
        }
        return List.of();
    }

    private void consumeOne(ItemEntity target) {
        ItemStack stack = target.getStack();
        stack.decrement(1);
        if (stack.isEmpty()) {
            target.discard();
        } else {
            target.setStack(stack);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.doraemon_pocket.restoring_flashlight.tooltip").formatted(Formatting.GRAY));
    }
}
