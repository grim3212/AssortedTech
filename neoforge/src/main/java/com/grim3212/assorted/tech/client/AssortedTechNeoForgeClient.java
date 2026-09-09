package com.grim3212.assorted.tech.client;

import com.grim3212.assorted.tech.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * {@code @Mod.EventBusSubscriber} no longer nests under {@code @Mod} and no longer picks a bus, and
 * hanging client setup off {@code FMLConstructModEvent} is not the idiom any more: {@code @Mod}
 * takes a {@code dist} now, so a client-only entry point is simply a second {@code @Mod} class for
 * the same mod id whose constructor runs only on the client.
 * <p>
 * The timing matters for datagen as well as for the game - {@code TechClient.init()} is what queues
 * the bridge model loader onto the mod bus, and the block models this mod generates name it by id.
 * The client data generator constructs mods before it calls {@code ClientBootstrap.bootstrap()},
 * so the codec is in place by the time the generated JSON has to encode a reference to it.
 */
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class AssortedTechNeoForgeClient {

    public AssortedTechNeoForgeClient(IEventBus modBus, ModContainer modContainer) {
        TechClient.init();
    }
}
