package com.grim3212.assorted.tech.client;

import com.grim3212.assorted.tech.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Constants.MOD_ID)
public class AssortedTechForgeClient {

    @SubscribeEvent
    public static void initClientSide(final FMLConstructModEvent event) {
        TechClient.init();
    }
}
