package com.grim3212.assorted.tech.config;

import com.grim3212.assorted.lib.config.ConfigurationType;
import com.grim3212.assorted.lib.config.IConfigurationBuilder;
import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.Constants;

import java.util.function.Supplier;

public class TechClientConfig {

    public final Supplier<Boolean> showFanParticles;

    public TechClientConfig() {
        final IConfigurationBuilder builder = Services.CONFIG.createBuilder(ConfigurationType.CLIENT_ONLY, Constants.MOD_ID + "-client");

        showFanParticles = builder.defineBoolean("fans.showFanParticles", true, "Set this to true if you would like to see particles when a fan is on.");

        builder.setup();
    }
}
