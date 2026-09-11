package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.api.util.TechArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

/** The gravity boots, a boots armour item of {@link TechArmorMaterials#GRAVITY}. */
public class GravityArmorItem extends Item {

    public GravityArmorItem(Properties props) {
        super(props.humanoidArmor(TechArmorMaterials.GRAVITY.material(), ArmorType.BOOTS));
    }
}
