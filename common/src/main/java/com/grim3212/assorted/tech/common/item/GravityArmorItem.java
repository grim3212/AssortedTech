package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.api.util.TechArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * The gravity boots.
 * <p>
 * {@code ArmorItem} is gone. Armour is a plain {@link Item} carrying an {@code equippable}
 * component, which {@code Properties#humanoidArmor} writes along with the durability, defence,
 * toughness, knockback resistance, enchantability and repair material from the material record.
 * <p>
 * The class survives only so the rest of the mod can identify a pair of gravity boots by type.
 */
public class GravityArmorItem extends Item {

    public GravityArmorItem(Properties props) {
        super(props.humanoidArmor(TechArmorMaterials.GRAVITY.material(), ArmorType.BOOTS));
    }
}
