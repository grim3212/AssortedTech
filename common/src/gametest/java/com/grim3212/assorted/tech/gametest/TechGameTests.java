package com.grim3212.assorted.tech.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Automated in-world checks for AssortedTech.
 * <p>
 * The bodies live in common because the behaviour they check is common; each loader module only
 * registers them into {@code Registries.TEST_FUNCTION} through its own hook, and
 * {@code data/assortedtech/test_instance/*.json} pairs each one with the shared {@code test_box}
 * structure.
 * <p>
 * Everything this mod does to an entity happens on a block entity tick or on
 * {@code entityInside}, so the subject is almost always a pig spawned with no free will: it has no
 * goals to wander with, but {@code LivingEntity} still runs {@code travel}/{@code move} every tick,
 * which is what makes both the motion and the {@code entityInside} paths fire. Item entities are
 * not used for motion - they skip {@code move} on three ticks out of four while resting on the
 * ground.
 * <p>
 * The tests themselves are split by feature into the {@code *Tests} classes in this package,
 * with shared helpers in {@code TechTestSupport}; this only lists them.
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
