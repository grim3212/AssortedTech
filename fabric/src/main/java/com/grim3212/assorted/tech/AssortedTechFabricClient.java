package com.grim3212.assorted.tech;

import com.grim3212.assorted.tech.client.TechClient;
import net.fabricmc.api.ClientModInitializer;

public class AssortedTechFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TechClient.init();
    }

}
