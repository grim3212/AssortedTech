package com.grim3212.assorted.tech.client;

import net.fabricmc.api.ClientModInitializer;

public class AssortedTechFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TechClient.init();
    }

}
