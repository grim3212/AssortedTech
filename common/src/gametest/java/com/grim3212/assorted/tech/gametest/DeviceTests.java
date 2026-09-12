package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopTorchBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopWallTorchBlock;
import com.grim3212.assorted.tech.common.block.GlowstoneTorchBlock;
import com.grim3212.assorted.tech.common.block.GlowstoneWallTorchBlock;
import com.grim3212.assorted.tech.common.block.GravityDirectionalBlock;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GravityBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GravityDirectionalBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.SensorBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.lib.test.TestSupport.*;
import static com.grim3212.assorted.tech.gametest.TechTestSupport.*;

/**
 * Redstone devices: flip flop and glowstone torches, the alarm, the fan, and block entity data across a reload.
 */
final class DeviceTests {

    private DeviceTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("flip_flop_torch_latches", DeviceTests::flipFlopTorchLatches);
        out.accept("alarm_has_its_own_block_entity", DeviceTests::alarmHasItsOwnBlockEntity);
        out.accept("block_entity_data_survives_reload", DeviceTests::blockEntityDataSurvivesReload);
        out.accept("fan_blows_and_sucks", DeviceTests::fanBlowsAndSucks);
        out.accept("glowstone_torches_light_with_signal", DeviceTests::glowstoneTorchesLightWithSignal);
        out.accept("flip_flop_wall_torch_latches", DeviceTests::flipFlopWallTorchLatches);
    }

    /**
     * The flip flop torch toggles once per rising edge and holds while unpowered, which depends on
     * {@code LIT}/{@code PREV_LIT} updating in the right order. It reads the block it stands on, so
     * that is the block that gets powered.
     */
    private static void flipFlopTorchLatches(GameTestHelper helper) {
        BlockPos support = new BlockPos(4, 1, 4);
        BlockPos torch = new BlockPos(4, 2, 4);

        helper.startSequence()
                .thenExecute(() -> {
                    helper.setBlock(support, Blocks.STONE);
                    helper.setBlock(torch, TechBlocks.FLIP_FLOP_TORCH.get());
                })
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, false))
                .thenExecute(() -> helper.setBlock(support, Blocks.REDSTONE_BLOCK))
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, true))
                .thenExecute(() -> helper.setBlock(support, Blocks.STONE))
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, true))
                .thenExecute(() -> helper.setBlock(support, Blocks.REDSTONE_BLOCK))
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, false))
                .thenSucceed();
    }

    /**
     * The alarm gets an {@link AlarmBlockEntity} whose type is bound to the alarm block. Bound to
     * the wrong block, every alarm block entity is invalid, and nothing catches it at compile time.
     */
    private static void alarmHasItsOwnBlockEntity(GameTestHelper helper) {
        BlockPos alarm = new BlockPos(4, 1, 4);
        BlockPos fan = new BlockPos(6, 1, 4);

        helper.setBlock(alarm, TechBlocks.ALARM.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        helper.setBlock(fan, TechBlocks.FAN.get());

        // Throws with "wrong_block_entity" if the alarm ever gets anything else.
        helper.getBlockEntity(alarm, AlarmBlockEntity.class);

        helper.assertTrue(TechBlockEntityTypes.ALARM.get().isValid(helper.getBlockState(alarm)),
                "the alarm block entity type is not valid for the alarm block");
        helper.assertFalse(TechBlockEntityTypes.ALARM.get().isValid(helper.getBlockState(fan)),
                "the alarm block entity type is still bound to the fan block");
        helper.assertTrue(TechBlockEntityTypes.FAN.get().isValid(helper.getBlockState(fan)),
                "the fan block entity type is not valid for the fan block");

        helper.succeed();
    }

    /**
     * Every block entity field (gravity and sensor ranges, fan range and mode, bridge facing and
     * stored state) survives the {@code saveWithFullMetadata} / {@code loadStatic} round trip a
     * chunk uses. A mismatched {@code ValueOutput} / {@code ValueInput} key or a wrong default is
     * silent.
     */
    private static void blockEntityDataSurvivesReload(GameTestHelper helper) {
        BlockPos gravity = new BlockPos(1, 1, 1);
        helper.setBlock(gravity, TechBlocks.ATTRACTOR.get());
        GravityBlockEntity gravityEntity = helper.getBlockEntity(gravity, GravityBlockEntity.class);
        gravityEntity.cycleRange();
        gravityEntity.cycleRange();
        gravityEntity.toggleShowRange();

        BlockPos directional = new BlockPos(3, 1, 1);
        helper.setBlock(directional, TechBlocks.REPULSOR_DIRECTIONAL.get().defaultBlockState()
                .setValue(GravityDirectionalBlock.FACING, Direction.EAST));
        helper.getBlockEntity(directional, GravityDirectionalBlockEntity.class).cycleRange();

        BlockPos sensor = new BlockPos(5, 1, 1);
        helper.setBlock(sensor, sensorOf(SensorType.WOOD).defaultBlockState().setValue(SensorBlock.FACING, Direction.NORTH));
        SensorBlockEntity sensorEntity = helper.getBlockEntity(sensor, SensorBlockEntity.class);
        sensorEntity.cycleRange();
        sensorEntity.cycleRange();
        sensorEntity.cycleRange();

        BlockPos fan = new BlockPos(7, 1, 1);
        helper.setBlock(fan, TechBlocks.FAN.get().defaultBlockState().setValue(FanBlock.FACING, Direction.EAST));
        FanBlockEntity fanEntity = helper.getBlockEntity(fan, FanBlockEntity.class);
        fanEntity.setRange(7);
        fanEntity.setMode(FanMode.SUCK);

        BlockPos bridge = new BlockPos(1, 1, 4);
        helper.setBlock(bridge, TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, BridgeType.ACCEL));
        BridgeBlockEntity bridgeEntity = helper.getBlockEntity(bridge, BridgeBlockEntity.class);
        bridgeEntity.setFacing(Direction.WEST);
        bridgeEntity.setStoredBlockState(Blocks.GOLD_BLOCK.defaultBlockState());

        helper.assertValueEqual(afterReload(helper, gravity, GravityBlockEntity.class).getRange(), 3,
                "gravity range after a reload");
        helper.assertTrue(afterReload(helper, gravity, GravityBlockEntity.class).shouldShowRange(),
                "the gravity range overlay flag did not survive a reload");
        helper.assertValueEqual(afterReload(helper, directional, GravityDirectionalBlockEntity.class).getRange(), 2,
                "directional gravity range after a reload");
        helper.assertBlockProperty(directional, GravityDirectionalBlock.FACING, Direction.EAST);
        helper.assertValueEqual(afterReload(helper, sensor, SensorBlockEntity.class).getRange(), 4,
                "sensor range after a reload");
        helper.assertValueEqual(afterReload(helper, fan, FanBlockEntity.class).getRange(), 7,
                "fan range after a reload");
        helper.assertValueEqual(afterReload(helper, fan, FanBlockEntity.class).getMode(), FanMode.SUCK,
                "fan mode after a reload");
        helper.assertValueEqual(afterReload(helper, bridge, BridgeBlockEntity.class).getFacing(), Direction.WEST,
                "bridge facing after a reload");
        helper.assertTrue(afterReload(helper, bridge, BridgeBlockEntity.class).getStoredBlockState().is(Blocks.GOLD_BLOCK),
                "the bridge's stored block state did not survive a reload");

        helper.succeed();
    }

    /**
     * A blowing fan pushes entities away from its face and a sucking one pulls them in. They run in
     * separate lanes because at range 4 their volumes would overlap.
     */
    private static void fanBlowsAndSucks(GameTestHelper helper) {
        BlockPos blower = new BlockPos(1, 1, 1);
        BlockPos sucker = new BlockPos(7, 1, 6);

        helper.setBlock(blower, TechBlocks.FAN.get().defaultBlockState()
                .setValue(FanBlock.FACING, Direction.EAST).setValue(FanBlock.MODE, FanMode.BLOW));
        FanBlockEntity blowing = helper.getBlockEntity(blower, FanBlockEntity.class);
        blowing.setMode(FanMode.BLOW);
        blowing.setRange(4);

        helper.setBlock(sucker, TechBlocks.FAN.get().defaultBlockState()
                .setValue(FanBlock.FACING, Direction.WEST).setValue(FanBlock.MODE, FanMode.SUCK));
        FanBlockEntity sucking = helper.getBlockEntity(sucker, FanBlockEntity.class);
        sucking.setMode(FanMode.SUCK);
        sucking.setRange(4);

        Pig blown = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(2, 1, 1));
        Pig drawn = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 6));

        Vec3 blowerCorner = Vec3.atLowerCornerOf(helper.absolutePos(blower));
        Vec3 suckerCorner = Vec3.atLowerCornerOf(helper.absolutePos(sucker));
        double blownStart = blown.position().distanceTo(blowerCorner);
        double drawnStart = drawn.position().distanceTo(suckerCorner);
        double[] reach = {blownStart, drawnStart};

        helper.startSequence()
                .thenExecuteFor(40, () -> {
                    reach[0] = Math.max(reach[0], blown.position().distanceTo(blowerCorner));
                    reach[1] = Math.min(reach[1], drawn.position().distanceTo(suckerCorner));
                })
                .thenExecute(() -> {
                    helper.assertTrue(reach[0] > blownStart + 0.5D,
                            "a blowing fan did not push an entity away - it went from " + blownStart + " to " + reach[0]);
                    helper.assertTrue(reach[1] < drawnStart - 0.5D,
                            "a sucking fan did not pull an entity in - it went from " + drawnStart + " to " + reach[1]);
                })
                .thenSucceed();
    }

    /**
     * The standing and wall glowstone torches light on a redstone signal, go dark without one, and
     * report the light level the block claims. A lit torch outreaches the gap between test boxes,
     * so it stays mid-box and is put out before the test ends.
     */
    private static void glowstoneTorchesLightWithSignal(GameTestHelper helper) {
        BlockPos standSupport = new BlockPos(3, 2, 4);
        BlockPos stand = new BlockPos(3, 3, 4);
        BlockPos wallSupport = new BlockPos(6, 3, 4);
        BlockPos wall = new BlockPos(5, 3, 4);

        helper.runBeforeTestEnd(() -> extinguish(helper, stand, wall, standSupport, wallSupport));

        helper.startSequence()
                .thenExecute(() -> {
                    helper.setBlock(standSupport, Blocks.STONE);
                    helper.setBlock(stand, TechBlocks.GLOWSTONE_TORCH.get());
                    helper.setBlock(wallSupport, Blocks.STONE);
                    helper.setBlock(wall, TechBlocks.GLOWSTONE_WALL_TORCH.get().defaultBlockState()
                            .setValue(GlowstoneWallTorchBlock.FACING, Direction.WEST));
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertBlockProperty(stand, GlowstoneTorchBlock.LIT, false);
                    helper.assertBlockProperty(wall, GlowstoneTorchBlock.LIT, false);
                    helper.assertValueEqual(lightEmission(helper, stand), 0, "unlit glowstone torch light level");
                    helper.assertValueEqual(lightEmission(helper, wall), 0, "unlit glowstone wall torch light level");
                    helper.setBlock(standSupport, Blocks.REDSTONE_BLOCK);
                    helper.setBlock(wallSupport, Blocks.REDSTONE_BLOCK);
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertBlockProperty(stand, GlowstoneTorchBlock.LIT, true);
                    helper.assertBlockProperty(wall, GlowstoneTorchBlock.LIT, true);
                    helper.assertValueEqual(lightEmission(helper, stand), 15, "lit glowstone torch light level");
                    helper.assertValueEqual(lightEmission(helper, wall), 15, "lit glowstone wall torch light level");
                    helper.setBlock(standSupport, Blocks.STONE);
                    helper.setBlock(wallSupport, Blocks.STONE);
                })
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertBlockProperty(stand, GlowstoneTorchBlock.LIT, false);
                    helper.assertBlockProperty(wall, GlowstoneTorchBlock.LIT, false);
                })
                .thenSucceed();
    }

    /**
     * The wall flip flop torch latches like the standing one, but off the block it hangs on: a
     * separate {@code hasNeighborSignal} override. Lit torches are handled as in {@link
     * #glowstoneTorchesLightWithSignal}.
     */
    private static void flipFlopWallTorchLatches(GameTestHelper helper) {
        BlockPos support = new BlockPos(3, 3, 4);
        BlockPos torch = new BlockPos(4, 3, 4);

        helper.runBeforeTestEnd(() -> extinguish(helper, torch, support));

        helper.startSequence()
                .thenExecute(() -> {
                    helper.setBlock(support, Blocks.STONE);
                    helper.setBlock(torch, TechBlocks.FLIP_FLOP_WALL_TORCH.get().defaultBlockState()
                            .setValue(FlipFlopWallTorchBlock.FACING, Direction.EAST));
                })
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, false))
                .thenExecute(() -> helper.setBlock(support, Blocks.REDSTONE_BLOCK))
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, true))
                .thenExecute(() -> helper.setBlock(support, Blocks.STONE))
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, true))
                .thenExecute(() -> helper.setBlock(support, Blocks.REDSTONE_BLOCK))
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(torch, FlipFlopTorchBlock.LIT, false))
                .thenSucceed();
    }
}
