package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Puts this mod's test functions into {@code Registries.TEST_FUNCTION}.
 * <p>
 * Deliberately an {@link EventBusSubscriber} rather than a listener added by
 * {@code AssortedTechNeoForge}: nothing in {@code main} may reference the gametest source set, or
 * the tests would have to live in {@code main} and would ship in the jar. FML discovers this class
 * by scanning the mod's own classes, and in a release build it is simply not there.
 * <p>
 * The annotation no longer picks a bus in 26.2 - it has only {@code value} (dist) and
 * {@code modid}. {@code RegisterEvent} is an {@code IModBusEvent}, so it lands on the mod bus.
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
