package com.grim3212.assorted.tech.common.util;

public enum GravityType {
	ATTRACT(-1),
	REPULSE(1),
	GRAVITATE(0);
	
	private final int powerMod;
	
	GravityType(int powerMod) {
		this.powerMod = powerMod;
	}
	
	public int getPowerMod() {
		return powerMod;
	}
}
