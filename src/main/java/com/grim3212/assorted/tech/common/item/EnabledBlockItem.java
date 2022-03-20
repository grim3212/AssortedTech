package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.handler.TechConfig;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;

public class EnabledBlockItem extends BlockItem {

	private final String enabled;

	public EnabledBlockItem(Block block, Properties props, String enabled) {
		super(block, props);
		this.enabled = enabled;
	}

	@Override
	protected boolean allowdedIn(CreativeModeTab tab) {
		switch (this.enabled) {
			case EnabledCondition.TORCHES_CONDITION:
				return TechConfig.COMMON.torchesEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.SPIKES_CONDITION:
				return TechConfig.COMMON.spikesEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.SENSORS_CONDITION:
				return TechConfig.COMMON.sensorsEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.FAN_CONDITION:
				return TechConfig.COMMON.fanEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.ALARM_CONDITION:
				return TechConfig.COMMON.alarmEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.BRIDGES_CONDITION:
				return TechConfig.COMMON.bridgesEnabled.get() ? super.allowdedIn(tab) : false;
			case EnabledCondition.GRAVITY_CONDITION:
				return TechConfig.COMMON.gravityEnabled.get() ? super.allowdedIn(tab) : false;
			default:
				return super.allowdedIn(tab);
		}
	}
}
