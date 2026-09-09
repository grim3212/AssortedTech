package com.grim3212.assorted.tech.api.util;

import com.grim3212.assorted.lib.util.LibCommonTags;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/**
 * The mod's armour materials.
 * <p>
 * No longer implements {@code ArmorMaterial}: that is a record now, so an entry builds one rather
 * than answering questions about itself. {@code ArmorItem.Type} became {@link ArmorType}, and the
 * per-slot durability multiplier that used to come from a mixin into vanilla's {@code ArmorMaterials}
 * is public API - {@link ArmorType#getDurability(int)}.
 * <p>
 * The repair ingredient is a {@link TagKey} rather than an {@code Ingredient}, so the ender pearl it
 * used to name directly is the common ender pearl tag.
 * <p>
 * Each material also needs an {@code assets/assortedtech/equipment/<name>.json}: since 1.21.4 the
 * worn-armour texture is resolved through the {@code equipment_asset} registry instead of being
 * derived from the material's name.
 */
public enum TechArmorMaterials {
    GRAVITY("gravity", 15, Util.make(new EnumMap<>(ArmorType.class), (map) -> {
        map.put(ArmorType.BOOTS, 2);
        map.put(ArmorType.LEGGINGS, 6);
        map.put(ArmorType.CHESTPLATE, 5);
        map.put(ArmorType.HELMET, 2);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, LibCommonTags.Items.ENDER_PEARLS);

    private final String name;
    private final int durabilityMultiplier;
    private final Map<ArmorType, Integer> defense;
    private final int enchantmentValue;
    private final Holder<SoundEvent> equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final TagKey<Item> repairIngredient;
    private final ResourceKey<EquipmentAsset> assetId;

    TechArmorMaterials(String name, int durabilityMultiplier, Map<ArmorType, Integer> defense, int enchantmentValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.defense = defense;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
        this.assetId = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    /**
     * This entry as the record vanilla builds armour from.
     * <p>
     * The durability the record carries is the multiplier, not a finished number -
     * {@code Properties#humanoidArmor} scales it per slot with {@link ArmorType#getDurability(int)},
     * which is what the old {@code getDurabilityForType} did by hand.
     */
    public ArmorMaterial material() {
        return new ArmorMaterial(this.durabilityMultiplier, this.defense, this.enchantmentValue, this.equipSound, this.toughness, this.knockbackResistance, this.repairIngredient, this.assetId);
    }

    public String getName() {
        return this.name;
    }

    public ResourceKey<EquipmentAsset> assetId() {
        return this.assetId;
    }
}
