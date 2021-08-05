package com.grim3212.assorted.tech.common.handler;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TechConfig {

	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	public static class Common {

		public final ForgeConfigSpec.BooleanValue torchesEnabled;
		public final ForgeConfigSpec.BooleanValue spikesEnabled;
		public final ForgeConfigSpec.BooleanValue sensorsEnabled;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.push("Parts");
			torchesEnabled = builder.comment("Set this to true if you would like torches to be craftable and found in the creative tab.").define("torchesEnabled", true);
			spikesEnabled = builder.comment("Set this to true if you would like spikes to be craftable and found in the creative tab.").define("spikesEnabled", true);
			sensorsEnabled = builder.comment("Set this to true if you would like sensors to be craftable and found in the creative tab.").define("sensorsEnabled", true);
			builder.pop();
		}
	}
}
