package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.handler.TechConfig;

import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.WallOrFloorItem;

public class EnabledStandingAndWallBlockItem extends WallOrFloorItem {

	private final String enabledCondition;

	public EnabledStandingAndWallBlockItem(Block standingBlock, Block wallBlock, String enabledCondition, Properties props) {
		super(standingBlock, wallBlock, props);
		this.enabledCondition = enabledCondition;
	}

	@Override
	protected boolean allowdedIn(ItemGroup tab) {
		switch (this.enabledCondition) {
			case EnabledCondition.TORCHES_CONDITION:
				return TechConfig.COMMON.torchesEnabled.get() ? super.allowdedIn(tab) : false;
			default:
				return super.allowdedIn(tab);
		}
	}
}
