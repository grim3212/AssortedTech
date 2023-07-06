package com.grim3212.assorted.tech.api.util;

import com.grim3212.assorted.lib.util.LibCommonTags;
import com.grim3212.assorted.tech.api.TechTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Predicate;
import java.util.function.Supplier;

public enum SensorType implements StringRepresentable {
    WOOD("wood", MapColor.WOOD, SoundType.WOOD, Entity.class, () -> Ingredient.of(ItemTags.PLANKS)),
    STONE("stone", MapColor.STONE, SoundType.STONE, LivingEntity.class, () -> Ingredient.of(LibCommonTags.Items.STONE)),
    IRON("iron", MapColor.METAL, SoundType.METAL, Player.class, () -> Ingredient.of(LibCommonTags.Items.STORAGE_BLOCKS_IRON)),
    MOSSY_COBBLESTONE("mossy_cobblestone", MapColor.STONE, SoundType.STONE, Monster.class, () -> Ingredient.of(Blocks.MOSSY_COBBLESTONE)),
    PRISMARINE("prismarine", MapColor.STONE, SoundType.STONE, (ent) -> ent instanceof WaterAnimal || ent.getType().is(TechTags.Entities.SENSORS_WATER), () -> Ingredient.of(Blocks.PRISMARINE)),
    GOLD("gold", MapColor.METAL, SoundType.METAL, ItemEntity.class, () -> Ingredient.of(LibCommonTags.Items.STORAGE_BLOCKS_GOLD)),
    EMERALD("emerald", MapColor.METAL, SoundType.METAL, AbstractVillager.class, () -> Ingredient.of(LibCommonTags.Items.STORAGE_BLOCKS_EMERALD)),
    NETHERRACK("netherrack", MapColor.STONE, SoundType.STONE, (ent) -> ent.getType().is(TechTags.Entities.SENSORS_NETHER), () -> Ingredient.of(LibCommonTags.Items.NETHERRACK)),
    COBWEB("cobweb", MapColor.STONE, SoundType.STONE, (ent) -> ent.getType().is(TechTags.Entities.SENSORS_ARTHROPODS), () -> Ingredient.of(LibCommonTags.Items.STRING)),
    END_STONE("end_stone", MapColor.STONE, SoundType.STONE, (ent) -> ent.getType().is(TechTags.Entities.SENSORS_END), () -> Ingredient.of(LibCommonTags.Items.END_STONES)),
    HAY_BALE("hay_bale", MapColor.GRASS, SoundType.GRASS, (ent) -> ent.getType().is(TechTags.Entities.SENSORS_PETS), () -> Ingredient.of(Blocks.HAY_BLOCK)),
    FEATHER("feather", MapColor.WOOL, SoundType.WOOL, (ent) -> ent.getType().is(TechTags.Entities.SENSORS_FLYING), () -> Ingredient.of(LibCommonTags.Items.FEATHERS));

    private final String name;
    private final SoundType soundType;
    private final Predicate<? super Entity> trigger;
    private final MapColor mapColor;
    private final Supplier<Ingredient> craftingMaterial;

    private SensorType(String name, MapColor mapColor, SoundType soundType, Class<? extends Entity> trigger, Supplier<Ingredient> craftingMaterial) {
        this(name, mapColor, soundType, (ent) -> {
            return trigger.isInstance(ent);
        }, craftingMaterial);
    }

    private SensorType(String name, MapColor mapColor, SoundType soundType, Predicate<? super Entity> trigger, Supplier<Ingredient> craftingMaterial) {
        this.name = name;
        this.soundType = soundType;
        this.trigger = trigger;
        this.mapColor = mapColor;
        this.craftingMaterial = craftingMaterial;
    }

    public MapColor getMapColor() {
        return mapColor;
    }

    public SoundType getSoundType() {
        return soundType;
    }

    public Predicate<? super Entity> getTrigger() {
        return trigger;
    }

    public Ingredient getCraftingMaterial() {
        return craftingMaterial.get();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
