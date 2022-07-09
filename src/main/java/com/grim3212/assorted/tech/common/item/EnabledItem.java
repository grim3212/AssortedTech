package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.handler.TechConfig;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class EnabledItem extends Item {

	private final String enabledCondition;

	public EnabledItem(String enabledCondition, Properties props) {
		super(props);
		this.enabledCondition = enabledCondition;
	}

	@Override
	protected boolean allowedIn(CreativeModeTab tab) {
		switch (this.enabledCondition) {
		case EnabledCondition.TORCHES_CONDITION:
			return TechConfig.COMMON.torchesEnabled.get() ? super.allowedIn(tab) : false;
		case EnabledCondition.SPIKES_CONDITION:
			return TechConfig.COMMON.spikesEnabled.get() ? super.allowedIn(tab) : false;
		case EnabledCondition.SENSORS_CONDITION:
			return TechConfig.COMMON.sensorsEnabled.get() ? super.allowedIn(tab) : false;
		case EnabledCondition.FAN_CONDITION:
			return TechConfig.COMMON.fanEnabled.get() ? super.allowedIn(tab) : false;
		case EnabledCondition.ALARM_CONDITION:
			return TechConfig.COMMON.alarmEnabled.get() ? super.allowedIn(tab) : false;
		case EnabledCondition.BRIDGES_CONDITION:
			return TechConfig.COMMON.bridgesEnabled.get() ? super.allowedIn(tab) : false;
		case EnabledCondition.GRAVITY_CONDITION:
			return TechConfig.COMMON.gravityEnabled.get() ? super.allowedIn(tab) : false;
		default:
			return super.allowedIn(tab);
		}
	}
}