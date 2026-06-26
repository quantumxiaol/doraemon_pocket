package com.doraemon.pocket.registry;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum ModArmorMaterials implements ArmorMaterial {
	BAMBOO_COPTER("bamboo_copter", 9, 15, 0.0F, 0.0F),
	DODGE_CLOAK("dodge_cloak", 12, 18, 0.0F, 0.0F),
	STONE_HAT("stone_hat", 8, 8, 0.0F, 0.0F);

	private final String name;
	private final int durabilityMultiplier;
	private final int enchantability;
	private final float toughness;
	private final float knockbackResistance;

	ModArmorMaterials(String name, int durabilityMultiplier, int enchantability, float toughness, float knockbackResistance) {
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.enchantability = enchantability;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
	}

	@Override
	public int getDurability(ArmorItem.Type type) {
		return switch (type) {
			case HELMET -> 11;
			case CHESTPLATE -> 16;
			case LEGGINGS -> 15;
			case BOOTS -> 13;
		} * durabilityMultiplier;
	}

	@Override
	public int getProtection(ArmorItem.Type type) {
		return 0;
	}

	@Override
	public int getEnchantability() {
		return enchantability;
	}

	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.EMPTY;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public float getToughness() {
		return toughness;
	}

	@Override
	public float getKnockbackResistance() {
		return knockbackResistance;
	}
}
