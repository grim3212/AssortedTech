package com.grim3212.assorted.tech;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.crafting.TechConditions;
import com.grim3212.assorted.tech.common.handlers.TechCreativeItems;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.network.TechPackets;
import com.grim3212.assorted.tech.common.particle.TechParticleTypes;
import com.grim3212.assorted.tech.common.sounds.TechSounds;
import com.grim3212.assorted.tech.config.TechCommonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TechCommonMod {

    public static final TechCommonConfig COMMON_CONFIG = new TechCommonConfig();

    public static void init() {
        TechBlocks.init();
        TechBlockEntityTypes.init();
        TechItems.init();
        TechSounds.init();
        TechParticleTypes.init();
        TechPackets.init();
        TechConditions.init();

        Services.PLATFORM.registerCreativeTab(new ResourceLocation(Constants.MOD_ID, "tab"), Component.translatable("itemGroup." + Constants.MOD_ID), () -> new ItemStack(TechBlocks.FLIP_FLOP_TORCH.get()), () -> TechCreativeItems.getCreativeItems());
    }
}
