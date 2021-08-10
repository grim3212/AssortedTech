package com.grim3212.assorted.tech.common.proxy;

import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;

public interface IProxy {
	default void starting() {
	}

	default void openFanScreen(FanBlockEntity fan) {
	}
}
