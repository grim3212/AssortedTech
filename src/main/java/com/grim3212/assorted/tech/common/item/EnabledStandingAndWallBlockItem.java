package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.handler.TechConfig;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

public class EnabledStandingAndWallBlockItem extends StandingAndWallBlockItem {

	private final String enabledCondition;

	public EnabledStandingAndWallBlockItem(Block standingBlock, Block wallBlock, String enabledCondition, Properties props) {
		super(standingBlock, wallBlock, props);
		this.enabledCondition = enabledCondition;
	}

	@Override
	protected boolean allowedIn(CreativeModeTab tab) {
		switch (this.enabledCondition) {
		case EnabledCondition.TORCHES_CONDITION:
			return TechConfig.COMMON.torchesEnabled.get() ? super.allowedIn(tab) : false;
		default:
			return super.allowedIn(tab);
		}
	}
}
