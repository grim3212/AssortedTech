package com.grim3212.assorted.tech.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Automated in-world checks for AssortedTech. Entity subjects are pigs spawned with no free will:
 * they still run {@code travel}/{@code move} every tick, which motion and {@code entityInside}
 * need. Item entities skip {@code move} most ticks while resting, so they are not used. The tests
 * live in the {@code *Tests} classes, with helpers in {@code TechTestSupport}; this only lists
 * them.
 */
public final class TechGameTests {

    private TechGameTests() {
    }

    /** Every test in this mod, named once, so both loaders register the same set. */
    public static void forEach(BiConsumer<String, Consumer<GameTestHelper>> out) {
        GravityTests.register(out);
        BridgeTests.register(out);
        SensorTests.register(out);
        SpikeTests.register(out);
        DeviceTests.register(out);
        AssetTests.register(out);
    }
}
