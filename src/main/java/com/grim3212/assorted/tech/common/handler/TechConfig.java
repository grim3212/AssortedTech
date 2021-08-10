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
		public final ForgeConfigSpec.BooleanValue fanEnabled;
		
		public final ForgeConfigSpec.DoubleValue fanSpeed;
		public final ForgeConfigSpec.DoubleValue fanModSpeed;
		public final ForgeConfigSpec.IntValue fanMaxRange;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.push("Parts");
			torchesEnabled = builder.comment("Set this to true if you would like torches to be craftable and found in the creative tab.").define("torchesEnabled", true);
			spikesEnabled = builder.comment("Set this to true if you would like spikes to be craftable and found in the creative tab.").define("spikesEnabled", true);
			sensorsEnabled = builder.comment("Set this to true if you would like sensors to be craftable and found in the creative tab.").define("sensorsEnabled", true);
			fanEnabled = builder.comment("Set this to true if you would like the fan to be craftable and found in the creative tab.").define("fanEnabled", true);
			builder.pop();
			
			builder.push("Fans");
			fanSpeed = builder.comment("The base speed at which the fan blows or sucks entities.").defineInRange("fanSpeed", 0.13D, 0.001D, 1000D);
			fanModSpeed = builder.comment("The modifier speed at which the fan blows or sucks entities will be added onto the base speed divided by the maxRange-fanRange.").defineInRange("fanModSpeed", 0.065D, 0.001D, 1000D);
			fanMaxRange = builder.comment("The maximum distance at which the range for fans can be set.").defineInRange("fanMaxRange", 32, 1, 1000);
			builder.pop();
		}
	}

	public static final Client CLIENT;
	public static final ForgeConfigSpec CLIENT_SPEC;
	static {
		final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		CLIENT_SPEC = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	public static class Client {

		public final ForgeConfigSpec.BooleanValue showFanParticles;

		public Client(ForgeConfigSpec.Builder builder) {
			builder.push("Fans");
			showFanParticles = builder.comment("Set this to true if you would like to see particles when a fan is on.").define("showFanParticles", true);
			builder.pop();
		}

	}
}
