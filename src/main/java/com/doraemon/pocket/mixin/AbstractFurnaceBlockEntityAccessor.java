package com.doraemon.pocket.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {
    @Accessor("cookTime")
    int doraemonPocket$getCookTime();

    @Accessor("cookTime")
    void doraemonPocket$setCookTime(int cookTime);

    @Accessor("cookTimeTotal")
    int doraemonPocket$getCookTimeTotal();
}
