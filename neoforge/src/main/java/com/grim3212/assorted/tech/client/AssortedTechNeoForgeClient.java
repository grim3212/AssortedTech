package com.grim3212.assorted.tech.client;

import com.grim3212.assorted.tech.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * The client-only entry point: a second {@code @Mod} for the same mod id, constructed only on the
 * client. Client datagen constructs it too, so the bridge model loader is registered before the
 * generated models reference it.
 */
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class AssortedTechNeoForgeClient {

    public AssortedTechNeoForgeClient(IEventBus modBus, ModContainer modContainer) {
        TechClient.init();
    }
}
