package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.Constants;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * Registers this mod's test functions on Fabric. The gametest source set is a dev-only second mod,
 * so nothing in {@code main} references it and the tests never ship. Fabric's {@code @GameTest} is
 * not used because it would duplicate the shared {@code test_instance} jsons.
 */
public class TechFabricGameTests implements ModInitializer {

    @Override
    public void onInitialize() {
        TechGameTests.forEach((name, function) ->
                Registry.register(BuiltInRegistries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name), function));
    }
}
