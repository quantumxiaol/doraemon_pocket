package com.doraemon.pocket.registry;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public enum ModToolMaterials implements ToolMaterial {
	RADAR_SWORD(64, 1, 6.0F, 2.0F, 18),
	DEPLETED_RADAR_SWORD(250, 1, 6.0F, 2.0F, 14);

	private final int durability;
	private final int miningLevel;
	private final float miningSpeedMultiplier;
	private final float attackDamage;
	private final int enchantability;

	ModToolMaterials(int durability, int miningLevel, float miningSpeedMultiplier, float attackDamage, int enchantability) {
		this.durability = durability;
		this.miningLevel = miningLevel;
		this.miningSpeedMultiplier = miningSpeedMultiplier;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
	}

	@Override
	public int getDurability() {
		return durability;
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return miningSpeedMultiplier;
	}

	@Override
	public float getAttackDamage() {
		return attackDamage;
	}

	@Override
	public int getMiningLevel() {
		return miningLevel;
	}

	@Override
	public int getEnchantability() {
		return enchantability;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.EMPTY;
	}
}
