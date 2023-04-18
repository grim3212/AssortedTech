package com.grim3212.assorted.tech;

import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.crafting.TechConditions;
import com.grim3212.assorted.tech.api.util.TechDamageTypes;
import com.grim3212.assorted.tech.common.handlers.TechCreativeItems;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.network.TechPackets;
import com.grim3212.assorted.tech.common.particle.TechParticleTypes;
import com.grim3212.assorted.tech.common.sounds.TechSounds;
import com.grim3212.assorted.tech.config.TechCommonConfig;

public class TechCommonMod {

    public static final TechCommonConfig COMMON_CONFIG = new TechCommonConfig();

    public static void init() {
        Constants.LOG.info(Constants.MOD_NAME + " starting up...");

        TechBlocks.init();
        TechBlockEntityTypes.init();
        TechItems.init();
        TechSounds.init();
        TechParticleTypes.init();
        TechPackets.init();
        TechConditions.init();
        TechCreativeItems.init();
    }
}
