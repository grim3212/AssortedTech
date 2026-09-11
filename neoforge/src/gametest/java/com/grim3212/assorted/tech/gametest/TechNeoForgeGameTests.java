package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Registers this mod's test functions on NeoForge. It lives in the gametest source set, so nothing
 * in {@code main} references it and release builds do not contain it.
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
public final class TechNeoForgeGameTests {

    private TechNeoForgeGameTests() {
    }

    @SubscribeEvent
    public static void registerGameTests(final RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> TechGameTests.forEach(
                (name, function) -> helper.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, name), function)));
    }
}
