package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.util.TechArmorMaterials;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class GravityArmorItem extends ArmorItem {

	public GravityArmorItem(EquipmentSlot slot, Properties props) {
		super(TechArmorMaterials.GRAVITY, slot, props);
	}

	@Override
	protected boolean allowdedIn(CreativeModeTab group) {
		return TechConfig.COMMON.bridgesEnabled.get() ? super.allowdedIn(group) : false;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return AssortedTech.MODID + ":textures/models/armor/gravity_layer_1.png";
	}
}
