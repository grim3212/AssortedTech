package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.api.util.TechArmorMaterials;
import net.minecraft.world.item.ArmorItem;

public class GravityArmorItem extends ArmorItem {

    public GravityArmorItem(Properties props) {
        super(TechArmorMaterials.GRAVITY, Type.BOOTS, props);
    }
}
