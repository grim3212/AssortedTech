package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.BridgeControlBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopTorchBlock;
import com.grim3212.assorted.tech.common.block.GravityBlock;
import com.grim3212.assorted.tech.common.block.GravityDirectionalBlock;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeControlBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
 * Manual checks that need a human are in {@code TESTING-CHECKLIST.md}.
 */
public final class TechGameTests {

    private TechGameTests() {
    }

    /** Every test in this mod, named once, so both loaders register the same set. */
    public static void forEach(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("gravitor_lifts_only_when_powered", TechGameTests::gravitorLiftsOnlyWhenPowered);
        out.accept("directional_repulsor_pushes_entity", TechGameTests::directionalRepulsorPushesEntity);
        out.accept("gravity_boots_exempt_wearer", TechGameTests::gravityBootsExemptWearer);
        out.accept("spike_damage_scales_with_type", TechGameTests::spikeDamageScalesWithType);
        out.accept("sensor_detects_and_clears", TechGameTests::sensorDetectsAndClears);
        out.accept("bridge_control_projects_and_clears", TechGameTests::bridgeControlProjectsAndClears);
        out.accept("bridge_control_refuses_non_full_cube", TechGameTests::bridgeControlRefusesNonFullCube);
        out.accept("flip_flop_torch_latches", TechGameTests::flipFlopTorchLatches);
        out.accept("alarm_has_its_own_block_entity", TechGameTests::alarmHasItsOwnBlockEntity);
    }

    /**
     * A powered gravitor lifts whatever is inside its radius, and an unpowered one does nothing.
     * <p>
     * Powered is reached the way it is in game - a redstone block next to it, so
     * {@code neighborChanged} does the toggle - rather than by writing the property, because writing
     * it would also rebuild the block entity the tick depends on.
     */
    private static void gravitorLiftsOnlyWhenPowered(GameTestHelper helper) {
        BlockPos gravitor = new BlockPos(4, 1, 4);
        BlockPos lever = new BlockPos(3, 1, 4);

        helper.setBlock(gravitor, TechBlocks.GRAVITOR.get());
        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 2, 4));

        // Heights are compared as a rise from where the pig started: a test structure is placed at
        // whatever world position the runner picked, so an absolute y means nothing here.
        double[] rise = {pig.getY(), 0.0D};

        helper.startSequence()
                .thenExecuteFor(20, () -> helper.assertTrue(pig.getY() - rise[0] < 0.1D,
                        "an unpowered gravitor moved an entity"))
                .thenExecute(() -> {
                    helper.assertBlockProperty(gravitor, GravityBlock.POWERED, false);
                    helper.setBlock(lever, Blocks.REDSTONE_BLOCK);
                    rise[0] = pig.getY();
                })
                .thenExecute(() -> helper.assertBlockProperty(gravitor, GravityBlock.POWERED, true))
                .thenExecuteFor(30, () -> rise[1] = Math.max(rise[1], pig.getY() - rise[0]))
                .thenExecute(() -> helper.assertTrue(rise[1] > 0.5D,
                        "a powered gravitor did not lift an entity - it rose " + rise[1]))
                .thenSucceed();
    }

    /**
     * The directional gravity blocks crashed on their first tick as recently as 2026-09-10 -
     * {@code GravityDirectionalBlockEntity#tick} read {@code GravityBlock.POWERED}, a different
     * property instance with the same name, and 26.2 resolves properties by identity. So this ticks
     * a repulsor deliberately and asserts the push actually lands, not just that nothing threw.
     */
    private static void directionalRepulsorPushesEntity(GameTestHelper helper) {
        BlockPos repulsor = new BlockPos(4, 1, 4);
        BlockPos lever = new BlockPos(3, 1, 4);

        helper.setBlock(repulsor, TechBlocks.REPULSOR_DIRECTIONAL.get().defaultBlockState()
                .setValue(GravityDirectionalBlock.FACING, Direction.UP));
        helper.setBlock(lever, Blocks.REDSTONE_BLOCK);

        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 2, 4));
        double start = pig.getY();
        double[] rise = {0.0D};

        helper.startSequence()
                .thenExecute(() -> helper.assertBlockProperty(repulsor, GravityDirectionalBlock.POWERED, true))
                .thenExecuteFor(30, () -> rise[0] = Math.max(rise[0], pig.getY() - start))
                .thenExecute(() -> helper.assertTrue(rise[0] > 0.5D,
                        "an upward repulsor did not push an entity - it rose " + rise[0]))
                .thenSucceed();
    }

    /**
     * Gravity boots exempt their wearer from a gravity effect.
     * <p>
     * The gravity <em>bridge</em> is the exemption that is reachable from a gametest: it tests any
     * {@code LivingEntity}, so a mob can wear the boots. The gravity <em>block</em> exempts
     * {@code Player} only, and there is no way to put a real player in a headless test world - see
     * TESTING-CHECKLIST.md.
     */
    private static void gravityBootsExemptWearer(GameTestHelper helper) {
        BlockPos bare = new BlockPos(2, 1, 4);
        BlockPos booted = new BlockPos(6, 1, 4);

        BlockState gravityBridge = TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, BridgeType.GRAVITY);
        helper.setBlock(bare, gravityBridge);
        helper.setBlock(booted, gravityBridge);
        helper.getBlockEntity(bare, BridgeBlockEntity.class).setFacing(Direction.UP);
        helper.getBlockEntity(booted, BridgeBlockEntity.class).setFacing(Direction.UP);

        Pig barePig = helper.spawnWithNoFreeWill(EntityTypes.PIG, bare);
        Pig bootedPig = helper.spawnWithNoFreeWill(EntityTypes.PIG, booted);
        bootedPig.setItemSlot(EquipmentSlot.FEET, new ItemStack(TechItems.GRAVITY_BOOTS.get()));

        double bareStart = barePig.getY();
        double bootedStart = bootedPig.getY();
        double[] rise = {0.0D, 0.0D};

        helper.startSequence()
                .thenExecuteFor(40, () -> {
                    rise[0] = Math.max(rise[0], barePig.getY() - bareStart);
                    rise[1] = Math.max(rise[1], bootedPig.getY() - bootedStart);
                })
                .thenExecute(() -> {
                    helper.assertTrue(rise[0] > 1.0D,
                            "a gravity bridge did not lift a bare-footed entity - it rose " + rise[0]);
                    helper.assertTrue(rise[1] < 0.2D,
                            "a gravity bridge lifted an entity wearing gravity boots - it rose " + rise[1]);
                })
                .thenSucceed();
    }

    /**
     * A powered spike hurts what stands on it, and {@link SpikeType#getDamage()} is what decides how
     * much: 14 damage kills a 10 health pig outright, 2 damage does not come close.
     * <p>
     * The redstone goes in first so the spike powers itself from its own {@code onPlace} neighbour
     * sweep; setting {@code POWERED} by hand would be undone by the scheduled tick that follows.
     */
    private static void spikeDamageScalesWithType(GameTestHelper helper) {
        BlockPos weak = new BlockPos(2, 1, 4);
        BlockPos strong = new BlockPos(6, 1, 4);

        helper.setBlock(new BlockPos(2, 0, 3), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(6, 0, 3), Blocks.REDSTONE_BLOCK);
        helper.setBlock(weak, spike(SpikeType.WOOD).defaultBlockState().setValue(SpikeBlock.FACING, Direction.UP));
        helper.setBlock(strong, spike(SpikeType.NETHERITE).defaultBlockState().setValue(SpikeBlock.FACING, Direction.UP));

        Pig weakVictim = helper.spawnWithNoFreeWill(EntityTypes.PIG, weak);
        Pig strongVictim = helper.spawnWithNoFreeWill(EntityTypes.PIG, strong);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(weak, SpikeBlock.POWERED, true);
            helper.assertBlockProperty(strong, SpikeBlock.POWERED, true);
            helper.assertFalse(strongVictim.isAlive(),
                    "a netherite spike (14 damage) did not kill a 10 health pig");
            helper.assertTrue(weakVictim.isAlive(),
                    "a wood spike (2 damage) killed a 10 health pig");
            helper.assertTrue(weakVictim.getHealth() < weakVictim.getMaxHealth(),
                    "a wood spike did not hurt the pig standing on it");
        });
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
     * A powered bridge control projects one segment per tick until something it cannot break is in
     * the way, and clears the whole run again once the signal goes.
     */
    private static void bridgeControlProjectsAndClears(GameTestHelper helper) {
        BlockPos control = new BlockPos(2, 1, 4);
        BlockPos lever = new BlockPos(2, 1, 3);
        BlockPos stop = new BlockPos(6, 1, 4);
        BlockPos[] run = {new BlockPos(3, 1, 4), new BlockPos(4, 1, 4), new BlockPos(5, 1, 4)};

        helper.startSequence()
                .thenExecute(() -> {
                    helper.setBlock(stop, Blocks.STONE);
                    helper.setBlock(control, TechBlocks.BRIDGE_CONTROL_LASER.get().defaultBlockState()
                            .setValue(BridgeControlBlock.FACING, Direction.EAST));
                    helper.setBlock(lever, Blocks.REDSTONE_BLOCK);
                })
                .thenExecute(() -> helper.assertBlockProperty(control, BridgeControlBlock.POWERED, true))
                .thenIdle(10)
                .thenExecute(() -> {
                    for (BlockPos pos : run) {
                        helper.assertBlockPresent(TechBlocks.BRIDGE.get(), pos);
                        helper.assertBlockProperty(pos, BridgeBlock.TYPE, BridgeType.LASER);
                    }
                    // Stone is not in assortedtech:laser_breakables, so the run has to stop at it.
                    helper.assertBlockPresent(Blocks.STONE, stop);
                })
                .thenExecute(() -> helper.setBlock(lever, Blocks.AIR))
                .thenIdle(15)
                .thenExecute(() -> {
                    helper.assertBlockProperty(control, BridgeControlBlock.POWERED, false);
                    for (BlockPos pos : run) {
                        helper.assertBlockNotPresent(TechBlocks.BRIDGE.get(), pos);
                    }
                    helper.assertBlockPresent(Blocks.STONE, stop);
                })
                .thenSucceed();
    }

    /**
     * A bridge control only accepts a block that fills its own collision box.
     * <p>
     * This is a behaviour change worth pinning: the 1.20.1 check was the deprecated
     * {@code BlockState#isSolid} (the pre-1.13 "legacy solid" flag), and the live equivalent is
     * {@code isCollisionShapeFullBlock}. A slab passed the old flag and must not pass this one -
     * a projected segment always renders as a full cube.
     */
    private static void bridgeControlRefusesNonFullCube(GameTestHelper helper) {
        BlockPos control = new BlockPos(4, 1, 4);
        helper.setBlock(control, TechBlocks.BRIDGE_CONTROL_LASER.get());

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos absolute = helper.absolutePos(control);
        // Hit the underside, so the fall-through place attempt that useBlock ends with lands on the
        // solid floor and does nothing rather than dropping a stray block in the test volume.
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.DOWN, absolute, false);

        BridgeControlBlockEntity blockEntity = helper.getBlockEntity(control, BridgeControlBlockEntity.class);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Blocks.STONE_SLAB));
        helper.useBlock(control, player, hit);
        helper.assertTrue(blockEntity.getStoredBlockState().isAir(),
                "a bridge control accepted a slab, which is not a full cube");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Blocks.STONE));
        helper.useBlock(control, player, hit);
        helper.assertTrue(blockEntity.getStoredBlockState().is(Blocks.STONE),
                "a bridge control refused a full cube");

        helper.succeed();
    }

    /**
     * The flip flop torch toggles once per rising edge and holds that state while unpowered - the
     * whole point of the block, and entirely a matter of the {@code LIT}/{@code PREV_LIT} pair being
     * updated in the right order.
     * <p>
     * It reads its signal from the block it stands on ({@code hasSignal(pos.below(), DOWN)}), so the
     * support block is what gets swapped rather than a neighbour.
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
     * The alarm gets an {@link AlarmBlockEntity}, and its block entity type is bound to the alarm
     * block rather than the fan.
     * <p>
     * They were crossed before the port fixed it - {@code assortedtech:alarm} listed
     * {@code TechBlocks.FAN} as its only valid block - which makes every alarm block entity invalid
     * for the block it lives on. Nothing about that is visible to the compiler.
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

    /** The spike and sensor blocks are registered as one list per enum, in enum order. */
    private static SpikeBlock spike(SpikeType type) {
        return TechBlocks.SPIKES.stream().map(IRegistryObject::get)
                .filter(block -> block.getSpikeType() == type).findFirst().orElseThrow();
    }

    private static SensorBlock sensorOf(SensorType type) {
        return TechBlocks.SENSORS.stream().map(IRegistryObject::get)
                .filter(block -> block.getSensorType() == type).findFirst().orElseThrow();
    }
}
