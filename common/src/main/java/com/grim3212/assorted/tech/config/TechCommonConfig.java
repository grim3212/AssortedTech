package com.grim3212.assorted.tech.config;

import com.grim3212.assorted.lib.config.ConfigurationType;
import com.grim3212.assorted.lib.config.IConfigurationBuilder;
import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.Constants;

import java.util.function.Supplier;

public class TechCommonConfig {

    public final Supplier<Boolean> torchesEnabled;
    public final Supplier<Boolean> spikesEnabled;
    public final Supplier<Boolean> sensorsEnabled;
    public final Supplier<Boolean> fanEnabled;
    public final Supplier<Boolean> alarmEnabled;
    public final Supplier<Boolean> bridgesEnabled;
    public final Supplier<Boolean> gravityEnabled;

    public final Supplier<Boolean> hideUncraftableItems;

    public final Supplier<Double> fanSpeed;
    public final Supplier<Double> fanModSpeed;
    public final Supplier<Integer> fanMaxRange;

    public final Supplier<Integer> bridgeMaxLength;

    public final Supplier<Double> attractRepulseSpeed;
    public final Supplier<Double> attractRepulseModSpeed;
    public final Supplier<Double> gravitorSpeed;

    public final Supplier<Integer> gravityMaxRange;

    public TechCommonConfig() {
        final IConfigurationBuilder builder = Services.CONFIG.createBuilder(ConfigurationType.NOT_SYNCED, Constants.MOD_ID + "-common");

        torchesEnabled = builder.defineBoolean("parts.torchesEnabled", true, "Set this to true if you would like torches to be craftable and found in the creative tab.");
        spikesEnabled = builder.defineBoolean("parts.spikesEnabled", true, "Set this to true if you would like spikes to be craftable and found in the creative tab.");
        sensorsEnabled = builder.defineBoolean("parts.sensorsEnabled", true, "Set this to true if you would like sensors to be craftable and found in the creative tab.");
        fanEnabled = builder.defineBoolean("parts.fanEnabled", true, "Set this to true if you would like the fan to be craftable and found in the creative tab.");
        alarmEnabled = builder.defineBoolean("parts.alarmEnabled", true, "Set this to true if you would like the alarm to be craftable and found in the creative tab.");
        bridgesEnabled = builder.defineBoolean("parts.bridgesEnabled", true, "Set this to true if you would like the bridges to be craftable and found in the creative tab.");
        gravityEnabled = builder.defineBoolean("parts.gravityEnabled", true, "Set this to true if you would like the gravity blocks and items to be craftable and found in the creative tab.");

        hideUncraftableItems = builder.defineBoolean("general.hideUncraftableItems", false, "For any item that is unobtainable (like missing materials from other mods) hide it from the creative menu / JEI.");

        fanSpeed = builder.defineDouble("fans.fanSpeed", 0.13D, 0.001D, 1000D, "The base speed at which the fan blows or sucks entities.");
        fanModSpeed = builder.defineDouble("fans.fanModSpeed", 0.065D, 0.001D, 1000D, "The modifier speed at which the fan blows or sucks entities will be added onto the base speed divided by the maxRange-fanRange.");
        fanMaxRange = builder.defineInteger("fans.fanMaxRange", 32, 1, 1000, "The maximum distance at which the range for fans can be set.");

        bridgeMaxLength = builder.defineInteger("bridges.bridgeMaxLength", 128, 1, 1000, "The maximum length that bridges will extend out to in blocks.");

        gravityMaxRange = builder.defineInteger("gravity.gravityMaxRange", 15, 1, 1000, "The maximum distance at which the range for gravity blocks can be set.");
        attractRepulseSpeed = builder.defineDouble("gravity.attractRepulseSpeed", 0.13D, 0.001D, 1000D, "The base speed at which the attractor and repulsor moves entities.");
        attractRepulseModSpeed = builder.defineDouble("gravity.attractRepulseModSpeed", 0.065D, 0.001D, 1000D, "The modifier speed at which the attractor and repulsor moves entities will be added onto the base speed divided by the maxRange-fanRange.");
        gravitorSpeed = builder.defineDouble("gravity.gravitorSpeed", 0.1D, 0.001D, 10D, "The speed at which the gravitor blocks will move entities up.");

        builder.setup();
    }
}
