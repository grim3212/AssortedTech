package com.grim3212.assorted.tech.common.crafting;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.TechCommonMod;

public class TechConditions {

    public static class Parts {
        public static final String TORCHES = "torches";
        public static final String SPIKES = "spikes";
        public static final String SENSORS = "sensors";
        public static final String FAN = "fan";
        public static final String ALARM = "alarm";
        public static final String BRIDGES = "bridges";
        public static final String GRAVITY = "gravity";
    }


    public static void init() {
        Services.CONDITIONS.registerPartCondition(Parts.TORCHES, () -> TechCommonMod.COMMON_CONFIG.torchesEnabled.get());
        Services.CONDITIONS.registerPartCondition(Parts.SPIKES, () -> TechCommonMod.COMMON_CONFIG.spikesEnabled.get());
        Services.CONDITIONS.registerPartCondition(Parts.SENSORS, () -> TechCommonMod.COMMON_CONFIG.sensorsEnabled.get());
        Services.CONDITIONS.registerPartCondition(Parts.FAN, () -> TechCommonMod.COMMON_CONFIG.fanEnabled.get());
        Services.CONDITIONS.registerPartCondition(Parts.ALARM, () -> TechCommonMod.COMMON_CONFIG.alarmEnabled.get());
        Services.CONDITIONS.registerPartCondition(Parts.BRIDGES, () -> TechCommonMod.COMMON_CONFIG.bridgesEnabled.get());
        Services.CONDITIONS.registerPartCondition(Parts.GRAVITY, () -> TechCommonMod.COMMON_CONFIG.gravityEnabled.get());
    }

}
