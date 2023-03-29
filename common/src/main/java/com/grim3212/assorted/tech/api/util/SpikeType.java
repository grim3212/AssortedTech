package com.grim3212.assorted.tech.api.util;

import com.grim3212.assorted.lib.util.LibCommonTags;
import com.grim3212.assorted.tech.api.TechTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public enum SpikeType {
    // Vanilla materials
    WOOD(() -> ItemTags.PLANKS, 2f),
    STONE(() -> ItemTags.STONE_TOOL_MATERIALS, 5f),
    IRON(() -> LibCommonTags.Items.INGOTS_IRON, 8f),
    COPPER(() -> LibCommonTags.Items.INGOTS_COPPER, 6.5f),
    DIAMOND(() -> LibCommonTags.Items.GEMS_DIAMOND, 10f),
    GOLD(() -> LibCommonTags.Items.INGOTS_GOLD, 6f),
    NETHERITE(() -> LibCommonTags.Items.INGOTS_NETHERITE, 14f),
    AMETHYST(() -> LibCommonTags.Items.GEMS_AMETHYST, 9f),
    EMERALD(() -> LibCommonTags.Items.GEMS_EMERALD, 11f),

    // Assorted Core added materials
    TIN(() -> TechTags.Items.INGOTS_TIN, 4f),
    SILVER(() -> TechTags.Items.INGOTS_SILVER, 9f),
    ALUMINUM(() -> TechTags.Items.INGOTS_ALUMINUM, 3f),
    NICKEL(() -> TechTags.Items.INGOTS_NICKEL, 7f),
    PLATINUM(() -> TechTags.Items.INGOTS_PLATINUM, 11f),
    LEAD(() -> TechTags.Items.INGOTS_LEAD, 7f),
    BRONZE(() -> TechTags.Items.INGOTS_BRONZE, 8f),
    ELECTRUM(() -> TechTags.Items.INGOTS_ELECTRUM, 9f),
    INVAR(() -> TechTags.Items.INGOTS_INVAR, 8.5f),
    STEEL(() -> TechTags.Items.INGOTS_STEEL, 10f),
    RUBY(() -> TechTags.Items.GEMS_RUBY, 8f),
    SAPPHIRE(() -> TechTags.Items.GEMS_SAPPHIRE, 7.5f),
    TOPAZ(() -> TechTags.Items.GEMS_TOPAZ, 6.5f),
    PERIDOT(() -> TechTags.Items.GEMS_PERIDOT, 7f);

    private final Supplier<TagKey<Item>> material;
    private final float damage;

    private SpikeType(Supplier<TagKey<Item>> material, float damage) {
        this.material = material;
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    public TagKey<Item> getMaterial() {
        return material.get();
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
