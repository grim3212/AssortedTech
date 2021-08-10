package com.grim3212.assorted.tech.common.util;

import net.minecraft.util.StringRepresentable;

public enum FanMode implements StringRepresentable {
	BLOW,
	SUCK,
	OFF;

	public static FanMode[] VALUES = FanMode.values();

	public FanMode getNext() {
		// Check if the next value is the last value
		// If so go back to the first value
		if ((this.ordinal() + 2) == VALUES.length)
			return VALUES[0];

		return VALUES[(this.ordinal() + 1) % VALUES.length];
	}

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase();
	}
}
