package com.grim3212.assorted.tools.client;

import com.grim3212.assorted.tech.client.TechClient;
import net.fabricmc.api.ClientModInitializer;

public class ToolsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TechClient.init();
    }

}
