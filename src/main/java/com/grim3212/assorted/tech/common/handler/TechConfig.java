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
		public final ForgeConfigSpec.BooleanValue alarmEnabled;
		public final ForgeConfigSpec.BooleanValue bridgesEnabled;
		public final ForgeConfigSpec.BooleanValue gravityEnabled;

		public final ForgeConfigSpec.BooleanValue hideUncraftableItems;

		public final ForgeConfigSpec.DoubleValue fanSpeed;
		public final ForgeConfigSpec.DoubleValue fanModSpeed;
		public final ForgeConfigSpec.IntValue fanMaxRange;

		public final ForgeConfigSpec.IntValue bridgeMaxLength;

		public final ForgeConfigSpec.DoubleValue attractRepulseSpeed;
		public final ForgeConfigSpec.DoubleValue attractRepulseModSpeed;
		public final ForgeConfigSpec.DoubleValue gravitorSpeed;

		public final ForgeConfigSpec.IntValue gravityMaxRange;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.push("Parts");
			torchesEnabled = builder.comment("Set this to true if you would like torches to be craftable and found in the creative tab.").define("torchesEnabled", true);
			spikesEnabled = builder.comment("Set this to true if you would like spikes to be craftable and found in the creative tab.").define("spikesEnabled", true);
			sensorsEnabled = builder.comment("Set this to true if you would like sensors to be craftable and found in the creative tab.").define("sensorsEnabled", true);
			fanEnabled = builder.comment("Set this to true if you would like the fan to be craftable and found in the creative tab.").define("fanEnabled", true);
			alarmEnabled = builder.comment("Set this to true if you would like the alarm to be craftable and found in the creative tab.").define("alarmEnabled", true);
			bridgesEnabled = builder.comment("Set this to true if you would like the bridges to be craftable and found in the creative tab.").define("bridgesEnabled", true);
			gravityEnabled = builder.comment("Set this to true if you would like the gravity blocks and items to be craftable and found in the creative tab.").define("gravityEnabled", true);
			builder.pop();

			builder.push("General");
			hideUncraftableItems = builder.comment("For any item that is unobtainable (like missing materials from other mods) hide it from the creative menu / JEI.").define("hideUncraftableItems", true);
			builder.pop();

			builder.push("Fans");
			fanSpeed = builder.comment("The base speed at which the fan blows or sucks entities.").defineInRange("fanSpeed", 0.13D, 0.001D, 1000D);
			fanModSpeed = builder.comment("The modifier speed at which the fan blows or sucks entities will be added onto the base speed divided by the maxRange-fanRange.").defineInRange("fanModSpeed", 0.065D, 0.001D, 1000D);
			fanMaxRange = builder.comment("The maximum distance at which the range for fans can be set.").defineInRange("fanMaxRange", 32, 1, 1000);
			builder.pop();

			builder.push("Bridges");
			bridgeMaxLength = builder.comment("The maximum length that bridges will extend out to in blocks.").defineInRange("bridgeMaxLength", 128, 1, 1000);
			builder.pop();

			builder.push("Gravity");
			gravityMaxRange = builder.comment("The maximum distance at which the range for gravity blocks can be set.").defineInRange("gravityMaxRange", 15, 1, 1000);
			attractRepulseSpeed = builder.comment("The base speed at which the attractor and repulsor moves entities.").defineInRange("attractRepulseSpeed", 0.13D, 0.001D, 1000D);
			attractRepulseModSpeed = builder.comment("The modifier speed at which the attractor and repulsor moves entities will be added onto the base speed divided by the maxRange-fanRange.").defineInRange("attractRepulseModSpeed", 0.065D, 0.001D, 1000D);
			gravitorSpeed = builder.comment("The speed at which the gravitor blocks will move entities up.").defineInRange("gravitorSpeed", 0.1D, 0.001D, 10D);
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
