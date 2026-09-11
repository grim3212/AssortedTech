package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.tech.gametest.TechTestSupport.*;

/**
 * Sensors detecting and clearing, for every sensor type.
 */
final class SensorTests {

    private SensorTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("sensor_detects_and_clears", SensorTests::sensorDetectsAndClears);
        out.accept("every_sensor_type_detects", SensorTests::everySensorTypeDetects);
    }

    /**
     * A sensor raises its signal to 15 while something its {@link SensorType} accepts is in front of
     * it, and drops back to 0 once that is gone. The wood sensor takes any entity, so the subject
     * only has to be there.
     * <p>
     * The detection volume is one block deep in front of the face at range 1, so the pig goes at
     * {@code sensor.north(1)}, resting on the same floor.
     */
    private static void sensorDetectsAndClears(GameTestHelper helper) {
        BlockPos sensor = new BlockPos(4, 1, 4);

        helper.setBlock(sensor, sensorOf(SensorType.WOOD).defaultBlockState().setValue(SensorBlock.FACING, Direction.NORTH));
        Pig[] subject = new Pig[1];

        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertBlockProperty(sensor, SensorBlock.DETECTED, false);
                    subject[0] = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 3));
                })
                .thenWaitUntil(() -> {
                    helper.assertBlockProperty(sensor, SensorBlock.DETECTED, true);
                    helper.assertRedstoneSignal(sensor, Direction.NORTH, signal -> signal == 15,
                            () -> Component.literal("a detecting sensor did not emit 15"));
                })
                .thenExecute(() -> helper.discard(subject[0]))
                .thenWaitUntil(() -> {
                    helper.assertBlockProperty(sensor, SensorBlock.DETECTED, false);
                    helper.assertRedstoneSignal(sensor, Direction.NORTH, signal -> signal == 0,
                            () -> Component.literal("a sensor kept its signal after the entity left"));
                })
                .thenSucceed();
    }

    /**
     * Every {@link SensorType}, each in its own cell with the one thing its predicate accepts in
     * front of it.
     * <p>
     * The cells are spaced so nothing can drift into a neighbour's detection volume, which at range
     * 1 is the single block in front of the face. Driving this off the enum rather than off a
     * hand-written list is the point: a thirteenth sensor type gets a test for free, and a predicate
     * that quietly stopped matching cannot hide behind the eleven that still do.
     */
    private static void everySensorTypeDetects(GameTestHelper helper) {
        SensorType[] types = SensorType.values();
        BlockPos[] sensors = new BlockPos[types.length];

        for (int i = 0; i < types.length; i++) {
            sensors[i] = new BlockPos((i % 5) * 2, 1, 1 + (i / 5) * 3);
            helper.setBlock(sensors[i], sensorOf(types[i]).defaultBlockState().setValue(SensorBlock.FACING, Direction.NORTH));
        }

        helper.startSequence()
                .thenExecute(() -> {
                    for (int i = 0; i < types.length; i++) {
                        helper.assertBlockProperty(sensors[i], SensorBlock.DETECTED, false);
                    }
                })
                .thenExecute(() -> {
                    for (int i = 0; i < types.length; i++) {
                        spawnSensorSubject(helper, types[i], sensors[i].north());
                    }
                })
                .thenWaitUntil(() -> {
                    for (int i = 0; i < types.length; i++) {
                        SensorType type = types[i];
                        helper.assertBlockProperty(sensors[i], SensorBlock.DETECTED, true);
                        helper.assertRedstoneSignal(sensors[i], Direction.NORTH, signal -> signal == 15,
                                () -> Component.literal("the " + type + " sensor did not emit 15 for what it detects"));
                    }
                })
                .thenSucceed();
    }
}
