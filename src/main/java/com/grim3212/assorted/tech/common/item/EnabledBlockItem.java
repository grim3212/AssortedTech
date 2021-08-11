package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.handler.TechConfig;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;

public class EnabledBlockItem extends BlockItem {

	private final String enabled;

	public EnabledBlockItem(Block block, Properties props, String enabled) {
		super(block, props);
		this.enabled = enabled;
	}

	@Override
	protected boolean allowdedIn(ItemGroup tab) {
		switch (this.enabled) {
			case EnabledCondition.TORCHES_CONDITION:
				return TechConfig.COMMON.torchesEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.SPIKES_CONDITION:
				return TechConfig.COMMON.spikesEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.SENSORS_CONDITION:
				return TechConfig.COMMON.sensorsEnabled.get() ? super.allowdedIn(tab) : false;
			default:
				return super.allowdedIn(tab);
		}
	}
}
