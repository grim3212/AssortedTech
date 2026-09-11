package com.grim3212.assorted.tech.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.MinecraftServer;
import com.grim3212.assorted.lib.platform.Services;
import java.io.BufferedReader;
import java.io.IOException;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.BridgeControlBlock;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopTorchBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopWallTorchBlock;
import com.grim3212.assorted.tech.common.block.GlowstoneTorchBlock;
import com.grim3212.assorted.tech.common.block.GlowstoneWallTorchBlock;
import com.grim3212.assorted.tech.common.block.GravityBlock;
import com.grim3212.assorted.tech.common.block.GravityDirectionalBlock;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeControlBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GravityBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GravityDirectionalBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.SensorBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.handlers.TechCreativeItems;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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

    /** Keeps every test player's profile name distinct when a test places more than one. */
    private static final AtomicInteger PLAYERS = new AtomicInteger();

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
        out.accept("attractor_pulls_entity_in", TechGameTests::attractorPullsEntityIn);
        out.accept("repulsor_pushes_entity_away", TechGameTests::repulsorPushesEntityAway);
        out.accept("directional_gravity_ignores_off_axis", TechGameTests::directionalGravityIgnoresOffAxis);
        out.accept("redstone_toggles_gravity_blocks", TechGameTests::redstoneTogglesGravityBlocks);
        out.accept("gravity_boots_exempt_player", TechGameTests::gravityBootsExemptPlayer);
        out.accept("empty_hand_cycles_gravity_range", TechGameTests::emptyHandCyclesGravityRange);
        out.accept("block_entity_data_survives_reload", TechGameTests::blockEntityDataSurvivesReload);
        out.accept("every_bridge_control_projects_and_clears", TechGameTests::everyBridgeControlProjectsAndClears);
        out.accept("bridge_copies_controls_stored_state", TechGameTests::bridgeCopiesControlsStoredState);
        out.accept("bridge_effects_apply_to_entities", TechGameTests::bridgeEffectsApplyToEntities);
        out.accept("every_sensor_type_detects", TechGameTests::everySensorTypeDetects);
        out.accept("every_spike_type_damages", TechGameTests::everySpikeTypeDamages);
        out.accept("fan_blows_and_sucks", TechGameTests::fanBlowsAndSucks);
        out.accept("glowstone_torches_light_with_signal", TechGameTests::glowstoneTorchesLightWithSignal);
        out.accept("flip_flop_wall_torch_latches", TechGameTests::flipFlopWallTorchLatches);
        out.accept("assets_have_models_and_names", TechGameTests::assetsHaveModelsAndNames);
        out.accept("every_recipe_loads_or_is_conditioned_off", TechGameTests::everyRecipeLoadsOrIsConditionedOff);
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

    /**
     * A powered attractor drags whatever is in its radius towards itself.
     * <p>
     * Measured as the distance to the block rather than as a rise, because pulling <em>in</em> is
     * the whole difference from the repulsor - and measured against the same reference point the
     * block entity uses, the block's lower corner, not its centre.
     */
    private static void attractorPullsEntityIn(GameTestHelper helper) {
        BlockPos attractor = new BlockPos(4, 4, 4);
        BlockPos lever = new BlockPos(4, 5, 4);

        helper.setBlock(attractor, TechBlocks.ATTRACTOR.get());
        // The gravity blocks have no onPlace sweep, so the redstone has to arrive second: it is the
        // redstone block's own neighbour update that runs neighborChanged and flips POWERED.
        helper.setBlock(lever, Blocks.REDSTONE_BLOCK);

        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 4));
        Vec3 corner = Vec3.atLowerCornerOf(helper.absolutePos(attractor));
        double start = pig.position().distanceTo(corner);
        double[] closest = {start};

        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertBlockProperty(attractor, GravityBlock.POWERED, true);
                    // Range 1 reaches one block out, which is not far enough down to touch something
                    // standing on the floor three blocks below.
                    GravityBlockEntity gravity = helper.getBlockEntity(attractor, GravityBlockEntity.class);
                    gravity.cycleRange();
                    gravity.cycleRange();
                    helper.assertValueEqual(gravity.getRange(), 3, "attractor range after two cycles");
                })
                .thenExecuteFor(40, () -> closest[0] = Math.min(closest[0], pig.position().distanceTo(corner)))
                .thenExecute(() -> helper.assertTrue(closest[0] < start - 1.0D,
                        "a powered attractor did not pull an entity in - it went from " + start + " to " + closest[0]))
                .thenSucceed();
    }

    /** The mirror of the attractor: a powered repulsor drives whatever is in its radius away. */
    private static void repulsorPushesEntityAway(GameTestHelper helper) {
        BlockPos repulsor = new BlockPos(4, 1, 4);
        BlockPos lever = new BlockPos(4, 0, 4);

        helper.setBlock(repulsor, TechBlocks.REPULSOR.get());
        helper.setBlock(lever, Blocks.REDSTONE_BLOCK);

        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 2, 4));
        Vec3 corner = Vec3.atLowerCornerOf(helper.absolutePos(repulsor));
        double start = pig.position().distanceTo(corner);
        double[] furthest = {start};

        helper.startSequence()
                .thenExecute(() -> helper.assertBlockProperty(repulsor, GravityBlock.POWERED, true))
                .thenExecuteFor(40, () -> furthest[0] = Math.max(furthest[0], pig.position().distanceTo(corner)))
                .thenExecute(() -> helper.assertTrue(furthest[0] > start + 1.0D,
                        "a powered repulsor did not push an entity away - it went from " + start + " to " + furthest[0]))
                .thenSucceed();
    }

    /**
     * The directional blocks act along a beam out of the face they point at, and nowhere else.
     * <p>
     * That volume is the block's own column expanded by {@code range + 1} along that face and then
     * deflated by one on every side, so an entity one block to the side is outside it. Both pigs
     * stand at the same height on the same tick; only the axis separates them.
     */
    private static void directionalGravityIgnoresOffAxis(GameTestHelper helper) {
        BlockPos repulsor = new BlockPos(4, 1, 4);
        BlockPos lever = new BlockPos(4, 0, 4);
        BlockPos perch = new BlockPos(7, 1, 4);

        helper.setBlock(perch, Blocks.STONE);
        helper.setBlock(repulsor, TechBlocks.REPULSOR_DIRECTIONAL.get().defaultBlockState()
                .setValue(GravityDirectionalBlock.FACING, Direction.UP));
        helper.setBlock(lever, Blocks.REDSTONE_BLOCK);

        Pig onAxis = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 2, 4));
        Pig offAxis = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(7, 2, 4));

        double onStart = onAxis.getY();
        Vec3 offStart = offAxis.position();
        double[] moved = {0.0D, 0.0D};

        helper.startSequence()
                .thenExecute(() -> helper.assertBlockProperty(repulsor, GravityDirectionalBlock.POWERED, true))
                .thenExecuteFor(30, () -> {
                    moved[0] = Math.max(moved[0], onAxis.getY() - onStart);
                    moved[1] = Math.max(moved[1], offAxis.position().distanceTo(offStart));
                })
                .thenExecute(() -> {
                    helper.assertTrue(moved[0] > 0.5D,
                            "an upward repulsor did not push the entity in front of its face - it rose " + moved[0]);
                    helper.assertTrue(moved[1] < 0.1D,
                            "an upward repulsor moved an entity that was off its axis - it moved " + moved[1]);
                })
                .thenSucceed();
    }

    /**
     * Every gravity block - not just the gravitor the lift test uses - follows its redstone signal
     * on and off.
     * <p>
     * {@link GravityBlock#POWERED} and {@link GravityDirectionalBlock#POWERED} are two different
     * property instances that share a name, and 26.2 resolves properties by identity, so the state
     * has to be asked with the property its own block declared. Reading it through the wrong one is
     * exactly the mistake that made the directional blocks crash on their first tick.
     */
    private static void redstoneTogglesGravityBlocks(GameTestHelper helper) {
        List<Block> blocks = List.of(TechBlocks.ATTRACTOR.get(), TechBlocks.REPULSOR.get(), TechBlocks.GRAVITOR.get(),
                TechBlocks.ATTRACTOR_DIRECTIONAL.get(), TechBlocks.REPULSOR_DIRECTIONAL.get(),
                TechBlocks.GRAVITOR_DIRECTIONAL.get());
        // Three apart, so no lever reaches its neighbour's block.
        List<BlockPos> at = List.of(new BlockPos(1, 2, 1), new BlockPos(4, 2, 1), new BlockPos(7, 2, 1),
                new BlockPos(1, 2, 4), new BlockPos(4, 2, 4), new BlockPos(7, 2, 4));

        helper.startSequence()
                .thenExecute(() -> {
                    for (int i = 0; i < blocks.size(); i++) {
                        helper.setBlock(at.get(i), blocks.get(i));
                    }
                })
                .thenExecute(() -> {
                    for (int i = 0; i < blocks.size(); i++) {
                        helper.assertFalse(isPowered(helper, at.get(i)),
                                blocks.get(i).getDescriptionId() + " was powered with no signal");
                    }
                    at.forEach(pos -> helper.setBlock(pos.below(), Blocks.REDSTONE_BLOCK));
                })
                .thenIdle(2)
                .thenExecute(() -> {
                    for (int i = 0; i < blocks.size(); i++) {
                        helper.assertTrue(isPowered(helper, at.get(i)),
                                blocks.get(i).getDescriptionId() + " did not power up from a redstone signal");
                    }
                    at.forEach(pos -> helper.setBlock(pos.below(), Blocks.STONE));
                })
                // Powering down goes through a scheduled tick four ticks out, not straight away.
                .thenIdle(10)
                .thenExecute(() -> {
                    for (int i = 0; i < blocks.size(); i++) {
                        helper.assertFalse(isPowered(helper, at.get(i)),
                                blocks.get(i).getDescriptionId() + " stayed powered after the signal was removed");
                    }
                })
                .thenSucceed();
    }

    /**
     * The gravity <em>block</em> half of the boots, which the bridge test cannot reach: its
     * exemption tests {@code instanceof Player}, so the subject has to be a real player.
     * <p>
     * The assertion is on delta movement rather than on height. The block entity sets it directly
     * every tick it is powered, and a headless server has no client telling it where the player
     * went, so the velocity is the honest observation and the position is not.
     */
    private static void gravityBootsExemptPlayer(GameTestHelper helper) {
        BlockPos gravitor = new BlockPos(4, 1, 4);
        BlockPos lever = new BlockPos(4, 0, 4);
        BlockPos bareStand = new BlockPos(3, 1, 4);
        BlockPos bootedStand = new BlockPos(5, 1, 4);

        helper.setBlock(bareStand, Blocks.STONE);
        helper.setBlock(bootedStand, Blocks.STONE);
        helper.setBlock(gravitor, TechBlocks.GRAVITOR.get());
        helper.setBlock(lever, Blocks.REDSTONE_BLOCK);

        ServerPlayer bare = survivalPlayer(helper);
        ServerPlayer booted = survivalPlayer(helper);
        booted.setItemSlot(EquipmentSlot.FEET, new ItemStack(TechItems.GRAVITY_BOOTS.get()));

        stand(helper, bare, bareStand);
        stand(helper, booted, bootedStand);

        double[] lift = {0.0D, 0.0D};

        helper.startSequence()
                .thenExecute(() -> helper.assertBlockProperty(gravitor, GravityBlock.POWERED, true))
                .thenExecuteFor(30, () -> {
                    lift[0] = Math.max(lift[0], bare.getDeltaMovement().y);
                    lift[1] = Math.max(lift[1], booted.getDeltaMovement().y);
                })
                .thenExecute(() -> {
                    helper.assertTrue(lift[0] > 0.05D,
                            "a powered gravitor did not lift a bare-footed player - its best upward speed was " + lift[0]);
                    helper.assertTrue(lift[1] < 0.01D,
                            "a powered gravitor lifted a player wearing gravity boots - it reached " + lift[1]);
                })
                .thenSucceed();
    }

    /**
     * Right-clicking a gravity block with an empty hand steps the range on, and shift-clicking steps
     * it back. Only the server-side number is checked here; the action bar it is echoed on needs a
     * human, and stays on TESTING-CHECKLIST.md.
     * <p>
     * The subject is a real {@link ServerPlayer} rather than a mock, because the branch under test
     * ends in {@code ServerPlayer#sendSystemMessage} - a mock server player has no connection and
     * NPEs there, and a plain mock player skips the call altogether.
     */
    private static void emptyHandCyclesGravityRange(GameTestHelper helper) {
        BlockPos attractor = new BlockPos(4, 1, 4);
        helper.setBlock(attractor, TechBlocks.ATTRACTOR.get());

        ServerPlayer player = survivalPlayer(helper);
        BlockPos absolute = helper.absolutePos(attractor);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);

        GravityBlockEntity gravity = helper.getBlockEntity(attractor, GravityBlockEntity.class);
        helper.assertValueEqual(gravity.getRange(), 1, "gravity range before any click");

        helper.useBlock(attractor, player, hit);
        helper.assertValueEqual(gravity.getRange(), 2, "gravity range after one empty-handed click");

        helper.useBlock(attractor, player, hit);
        helper.assertValueEqual(gravity.getRange(), 3, "gravity range after two empty-handed clicks");

        player.setShiftKeyDown(true);
        helper.useBlock(attractor, player, hit);
        helper.assertValueEqual(gravity.getRange(), 2, "gravity range after a shift-click");

        helper.succeed();
    }

    /**
     * Everything this mod keeps on a block entity survives being written out and read back - the
     * gravity and sensor ranges, the fan's range and mode, and the bridge's facing and stored state.
     * <p>
     * This is the chunk round trip rather than an approximation of it: the same
     * {@code saveWithFullMetadata} / {@code loadStatic} pair a chunk save and load uses. The port
     * moved every one of these off {@code CompoundTag} and onto {@code ValueOutput} /
     * {@code ValueInput}, where a mismatched key or a wrong default is silent.
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
     * All five controls project their own kind of bridge while powered and tear it down again when
     * the signal goes, in five lanes that cannot reach each other.
     * <p>
     * Only the laser control was covered before, and it is the one with the least to go wrong: the
     * other four differ in {@link BridgeType#isSolid()} and in what {@code entityInside} does, and a
     * control that projected the wrong type would still look right in a screenshot.
     */
    private static void everyBridgeControlProjectsAndClears(GameTestHelper helper) {
        List<IRegistryObject<BridgeControlBlock>> controls = List.of(TechBlocks.BRIDGE_CONTROL_LASER,
                TechBlocks.BRIDGE_CONTROL_ACCEL, TechBlocks.BRIDGE_CONTROL_TRICK, TechBlocks.BRIDGE_CONTROL_DEATH,
                TechBlocks.BRIDGE_CONTROL_GRAVITY);

        helper.startSequence()
                .thenExecute(() -> {
                    for (int lane = 0; lane < controls.size(); lane++) {
                        int z = lane * 2;
                        helper.setBlock(new BlockPos(5, 1, z), Blocks.STONE);
                        helper.setBlock(new BlockPos(1, 1, z), controls.get(lane).get().defaultBlockState()
                                .setValue(BridgeControlBlock.FACING, Direction.EAST));
                        helper.setBlock(new BlockPos(0, 1, z), Blocks.REDSTONE_BLOCK);
                    }
                })
                .thenIdle(20)
                .thenExecute(() -> {
                    for (int lane = 0; lane < controls.size(); lane++) {
                        int z = lane * 2;
                        BridgeType type = controls.get(lane).get().getType();
                        helper.assertBlockProperty(new BlockPos(1, 1, z), BridgeControlBlock.POWERED, true);
                        for (int x = 2; x <= 4; x++) {
                            BlockPos segment = new BlockPos(x, 1, z);
                            helper.assertBlockPresent(TechBlocks.BRIDGE.get(), segment);
                            helper.assertBlockProperty(segment, BridgeBlock.TYPE, type);
                        }
                        // Stone is not in assortedtech:laser_breakables, so every run has to stop at it.
                        helper.assertBlockPresent(Blocks.STONE, new BlockPos(5, 1, z));
                    }
                })
                .thenExecute(() -> {
                    for (int lane = 0; lane < controls.size(); lane++) {
                        helper.setBlock(new BlockPos(0, 1, lane * 2), Blocks.AIR);
                    }
                })
                .thenIdle(20)
                .thenExecute(() -> {
                    for (int lane = 0; lane < controls.size(); lane++) {
                        int z = lane * 2;
                        helper.assertBlockProperty(new BlockPos(1, 1, z), BridgeControlBlock.POWERED, false);
                        for (int x = 2; x <= 4; x++) {
                            helper.assertBlockNotPresent(TechBlocks.BRIDGE.get(), new BlockPos(x, 1, z));
                        }
                    }
                })
                .thenSucceed();
    }

    /**
     * Every segment of a projected bridge carries the controller's stored block state and the
     * direction the run travels in - the first is what the segment is drawn as, the second is how a
     * broken segment finds its way back to the controller.
     * <p>
     * The state gets onto the controller the way a player puts it there, by right-clicking with a
     * block item, so the whole path is covered rather than just the field. Note that the block comes
     * from the click, not from a block placed behind or above the controller.
     */
    private static void bridgeCopiesControlsStoredState(GameTestHelper helper) {
        BlockPos control = new BlockPos(1, 1, 4);
        BlockPos lever = new BlockPos(0, 1, 4);
        BlockPos stop = new BlockPos(5, 1, 4);

        helper.setBlock(stop, Blocks.STONE);
        helper.setBlock(control, TechBlocks.BRIDGE_CONTROL_LASER.get().defaultBlockState()
                .setValue(BridgeControlBlock.FACING, Direction.EAST));

        Player player = helper.makeMockPlayer(GameType.CREATIVE);
        BlockPos absolute = helper.absolutePos(control);
        // The underside, so the place attempt useBlock falls through to lands on the floor.
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.DOWN, absolute, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Blocks.GOLD_BLOCK));
        helper.useBlock(control, player, hit);

        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertTrue(helper.getBlockEntity(control, BridgeControlBlockEntity.class)
                                    .getStoredBlockState().is(Blocks.GOLD_BLOCK),
                            "the bridge control did not take the block it was clicked with");
                    helper.setBlock(lever, Blocks.REDSTONE_BLOCK);
                })
                .thenIdle(20)
                .thenExecute(() -> {
                    for (int x = 2; x <= 4; x++) {
                        BlockPos segment = new BlockPos(x, 1, 4);
                        helper.assertBlockPresent(TechBlocks.BRIDGE.get(), segment);
                        BridgeBlockEntity bridge = helper.getBlockEntity(segment, BridgeBlockEntity.class);
                        helper.assertTrue(bridge.getStoredBlockState().is(Blocks.GOLD_BLOCK),
                                "bridge segment " + x + " did not take the control's stored block state");
                        helper.assertValueEqual(bridge.getFacing(), Direction.EAST, "bridge segment " + x + " facing");
                    }
                })
                .thenSucceed();
    }

    /**
     * The three bridge types that do something to whatever touches them: accel hands out Speed on
     * {@code stepOn}, death hurts on {@code entityInside}, and gravity flings along its stored
     * facing. Health is read five ticks in, inside the twenty-tick invulnerability window that
     * follows the first hit.
     */
    private static void bridgeEffectsApplyToEntities(GameTestHelper helper) {
        BlockPos accel = new BlockPos(2, 1, 4);
        BlockPos death = new BlockPos(5, 1, 4);
        BlockPos gravity = new BlockPos(7, 1, 4);

        helper.setBlock(accel, TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, BridgeType.ACCEL));
        helper.setBlock(death, TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, BridgeType.DEATH));
        helper.setBlock(gravity, TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, BridgeType.GRAVITY));
        helper.getBlockEntity(gravity, BridgeBlockEntity.class).setFacing(Direction.UP);

        // Accel is the only one of the three that is solid, so its subject stands on top of it while
        // the other two stand inside a segment that has no collision at all.
        Pig runner = helper.spawnWithNoFreeWill(EntityTypes.PIG, accel.above());
        Pig victim = helper.spawnWithNoFreeWill(EntityTypes.PIG, death);
        Pig flier = helper.spawnWithNoFreeWill(EntityTypes.PIG, gravity);

        float victimStart = victim.getHealth();
        double flierStart = flier.getY();
        double[] rise = {0.0D};

        helper.startSequence()
                .thenIdle(5)
                .thenExecute(() -> {
                    helper.assertLivingEntityHasMobEffect(runner, MobEffects.SPEED, 2);
                    helper.assertTrue(victim.getHealth() < victimStart,
                            "a death bridge did not hurt the entity standing in it");
                })
                .thenExecuteFor(40, () -> rise[0] = Math.max(rise[0], flier.getY() - flierStart))
                .thenExecute(() -> helper.assertTrue(rise[0] > 1.0D,
                        "an upward gravity bridge did not change an entity's fall - it rose " + rise[0]))
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

    /** One subject per sensor type - the narrowest thing that satisfies that type's predicate. */
    private static void spawnSensorSubject(GameTestHelper helper, SensorType type, BlockPos pos) {
        switch (type) {
            // Any entity at all, so a dropped item proves it is not quietly a living-entity check.
            case WOOD -> helper.spawnItem(Items.STICK, Vec3.atCenterOf(pos));
            case STONE -> helper.spawnWithNoFreeWill(EntityTypes.PIG, pos);
            case IRON -> stand(helper, survivalPlayer(helper), pos.below());
            case MOSSY_COBBLESTONE -> helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
            case PRISMARINE -> helper.spawnWithNoFreeWill(EntityTypes.SQUID, pos);
            case GOLD -> helper.spawnItem(Items.STICK, Vec3.atCenterOf(pos));
            case EMERALD -> helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, pos);
            case NETHERRACK -> helper.spawnWithNoFreeWill(EntityTypes.PIGLIN, pos);
            case COBWEB -> helper.spawnWithNoFreeWill(EntityTypes.SILVERFISH, pos);
            case END_STONE -> helper.spawnWithNoFreeWill(EntityTypes.ENDERMITE, pos);
            case HAY_BALE -> helper.spawnWithNoFreeWill(EntityTypes.WOLF, pos);
            case FEATHER -> helper.spawnWithNoFreeWill(EntityTypes.CHICKEN, pos);
        }
    }

    /**
     * Every {@link SpikeType}, each with its own pig standing on it, checked against the exact
     * damage the enum declares rather than against "more" and "less".
     * <p>
     * A spike reads its signal from the block <em>behind</em> its face, so the redstone goes in the
     * floor rows between the spikes rather than under them.
     * <p>
     * Only the <em>first</em> hit carries the material's damage, so each pig is watched every tick
     * and the number is taken from the tick its health first moves. Waiting a fixed number of ticks
     * and then reading does not work: a spike is standing on its victim, so it hits again as soon as
     * the invulnerability window lets it, and the second hit lands well inside any window wide enough
     * to be sure the first one has. The spike damage type scales only
     * {@code when_caused_by_living_non_player} and this source has no entity, so difficulty does not
     * enter into it.
     */
    private static void everySpikeTypeDamages(GameTestHelper helper) {
        SpikeType[] types = SpikeType.values();
        Pig[] victims = new Pig[types.length];

        for (int x = 0; x <= 8; x += 2) {
            for (int z = 1; z <= 7; z += 2) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.REDSTONE_BLOCK);
            }
        }

        for (int i = 0; i < types.length; i++) {
            BlockPos pos = spikeSlot(i);
            helper.setBlock(pos, spike(types[i]).defaultBlockState().setValue(SpikeBlock.FACING, Direction.UP));
            victims[i] = helper.spawnWithNoFreeWill(EntityTypes.PIG, pos);
        }

        float[] firstHit = new float[types.length];
        Arrays.fill(firstHit, -1.0F);

        helper.startSequence()
                .thenExecuteFor(20, () -> {
                    for (int i = 0; i < types.length; i++) {
                        if (firstHit[i] < 0.0F) {
                            float taken = victims[i].getMaxHealth() - victims[i].getHealth();
                            if (taken > 0.0F) {
                                firstHit[i] = taken;
                            }
                        }
                    }
                })
                .thenExecute(() -> {
                    for (int i = 0; i < types.length; i++) {
                        SpikeType type = types[i];
                        // A spike cannot take more than the pig has, so the lethal materials all
                        // land on its full ten health.
                        float expected = Math.min(type.getDamage(), victims[i].getMaxHealth());

                        helper.assertBlockProperty(spikeSlot(i), SpikeBlock.POWERED, true);
                        helper.assertTrue(firstHit[i] > 0.0F,
                                "a powered " + type + " spike never hurt the pig standing on it");
                        helper.assertTrue(Math.abs(firstHit[i] - expected) < 0.01F,
                                "a " + type + " spike is worth " + type.getDamage() + " damage but its first hit took "
                                        + firstHit[i]);
                    }
                })
                .thenSucceed();
    }

    /** Two apart in both axes, so no pig can reach into a neighbouring spike. */
    private static BlockPos spikeSlot(int index) {
        return new BlockPos((index % 5) * 2, 1, (index / 5) * 2);
    }

    /**
     * A blowing fan drives entities away from its face and a sucking one draws them in. The two run
     * in separate lanes because at range 4 their volumes would otherwise overlap.
     * <p>
     * The fan screen that sets all of this in game needs a human; the range is set on the block
     * entity here, and that it survives a reload is covered by the reload test.
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
     * The glowstone torch and its wall form both light on a redstone signal and go dark again, and
     * both report the light level the block itself claims.
     * <p>
     * The wall form is a separate block rather than a state of the standing one, so nothing about it
     * is covered by testing the standing torch. Their names are covered by the assets test - the two
     * wall torches needed lang keys of their own once {@code Block#getDescriptionId()} became final.
     * <p>
     * A lit glowstone torch is a light level 15 source, which outreaches the gap between one test
     * box and the next: it is kept in the middle of the box and put out again before the test ends,
     * so it cannot raise the light a neighbouring test measures.
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
     * The wall form of the flip flop torch latches the way the standing one does, but off the block
     * it hangs on rather than the block below it - a different {@code hasNeighborSignal} override,
     * so a different thing to get wrong.
     * <p>
     * A lit torch is a light source, so it stays in the middle of the box and is put out before the
     * test ends - see {@link #glowstoneTorchesLightWithSignal}.
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

    /**
     * Every block and item this mod registers has a model and a name, and the creative tab they all
     * live in exists.
     * <p>
     * The mod's own assets are on the classpath even on a headless server, so this is the real check
     * and not a proxy for one: a blockstate json per block, an item model json per item, and a key
     * in {@code en_us.json} for every {@code getDescriptionId()}. Missing models and missing lang
     * keys are the most repeated failure of this port - the two wall torches needed keys of their
     * own once {@code Block#getDescriptionId()} became final - and they are invisible to a compiler
     * and to a green datagen run alike.
     * <p>
     * Everything missing is reported in one message, because fixing them one failure at a time is a
     * slow loop.
     */
    private static void assetsHaveModelsAndNames(GameTestHelper helper) {
        JsonObject lang = readJson("/assets/" + Constants.MOD_ID + "/lang/en_us.json");
        helper.assertTrue(lang != null, "assets/" + Constants.MOD_ID + "/lang/en_us.json is not on the classpath");

        List<String> missing = new ArrayList<>();
        int blocks = 0;
        int items = 0;

        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!Constants.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            blocks++;
            if (!resourceExists("/assets/" + id.getNamespace() + "/blockstates/" + id.getPath() + ".json")) {
                missing.add("blockstates/" + id.getPath() + ".json");
            }
            String key = entry.getValue().getDescriptionId();
            if (!lang.has(key)) {
                missing.add("lang key " + key + " (block " + id + ")");
            }
        }

        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!Constants.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            items++;
            if (!resourceExists("/assets/" + id.getNamespace() + "/items/" + id.getPath() + ".json")) {
                missing.add("items/" + id.getPath() + ".json");
            }
            String key = entry.getValue().getDescriptionId();
            if (!lang.has(key)) {
                missing.add("lang key " + key + " (item " + id + ")");
            }
        }

        if (!BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(TechCreativeItems.CREATIVE_TAB_KEY)) {
            missing.add("the " + TechCreativeItems.CREATIVE_TAB_KEY.identifier() + " creative tab");
        }
        if (!lang.has("itemGroup." + Constants.MOD_ID)) {
            missing.add("lang key itemGroup." + Constants.MOD_ID);
        }

        // Guards against the whole walk passing because the registries came back empty.
        helper.assertTrue(blocks > 40, "only " + blocks + " blocks are registered under " + Constants.MOD_ID);
        helper.assertTrue(items > 40, "only " + items + " items are registered under " + Constants.MOD_ID);
        helper.assertTrue(missing.isEmpty(), missing.size() + " missing asset(s) across " + blocks + " blocks and "
                + items + " items: " + String.join(", ", missing));

        helper.succeed();
    }

    /** Reads a json off the mod's own classpath, or null if it is not there. */
    private static JsonObject readJson(String path) {
        try (InputStream in = TechGameTests.class.getResourceAsStream(path)) {
            return in == null ? null : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean resourceExists(String path) {
        try (InputStream in = TechGameTests.class.getResourceAsStream(path)) {
            return in != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads POWERED off whichever gravity block is actually there.
     * <p>
     * The plain and the directional block each declare their own {@code BooleanProperty} named
     * "powered", and 26.2 looks properties up by identity, so asking a directional block for
     * {@link GravityBlock#POWERED} throws rather than answering.
     */
    private static boolean isPowered(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getBlockState(pos);
        return state.getBlock() instanceof GravityDirectionalBlock
                ? state.getValue(GravityDirectionalBlock.POWERED)
                : state.getValue(GravityBlock.POWERED);
    }

    /**
     * Clears every position back to air.
     * <p>
     * Registered through {@code runBeforeTestEnd} by anything that lights a torch, so it runs
     * whether the test passed or failed. Block light does not stop at the wall of a test box, and a
     * lit block left behind can raise the level a neighbouring test measures - which shows up as an
     * unrelated, pre-existing test failing depending on where the runner happened to place it.
     */
    private static void extinguish(GameTestHelper helper, BlockPos... positions) {
        for (BlockPos pos : positions) {
            helper.setBlock(pos, Blocks.AIR);
        }
    }

    /** The light level the block itself claims for the state at {@code pos}. */
    private static int lightEmission(GameTestHelper helper, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockState state = helper.getLevel().getBlockState(absolute);
        return ((GlowstoneTorchBlock) state.getBlock()).getLightEmission(state, helper.getLevel(), absolute);
    }

    /**
     * Writes a block entity out and reads it back exactly as a chunk save and load would, and hands
     * back the copy that came off the tag.
     */
    private static <T extends BlockEntity> T afterReload(GameTestHelper helper, BlockPos pos, Class<T> type) {
        ServerLevel level = helper.getLevel();
        CompoundTag saved = helper.getBlockEntity(pos, type).saveWithFullMetadata(level.registryAccess());
        BlockEntity reloaded = BlockEntity.loadStatic(helper.absolutePos(pos), helper.getBlockState(pos), saved, level.registryAccess());
        helper.assertTrue(type.isInstance(reloaded),
                "a " + type.getSimpleName() + " did not come back from a save and load round trip");
        return type.cast(reloaded);
    }

    /**
     * A real, survival mode {@link ServerPlayer} placed in the test level.
     * <p>
     * {@code makeMockServerPlayerInLevel} is deprecated for removal and hard-codes creative;
     * {@code makeMockPlayer} and {@code makeMockServerPlayer} hand back a player that was never
     * placed in a level, so its connection is null and any message to it NPEs. This is the same
     * helper AssortedTools' gametests use.
     */
    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "tech-test-" + PLAYERS.incrementAndGet());
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        helper.runBeforeTestEnd(() -> level.getServer().getPlayerList().remove(player));

        player.setGameMode(GameType.SURVIVAL);
        helper.assertFalse(player.isCreative(), "the test player is in creative, which changes every path under test");
        return player;
    }

    /** Stands {@code entity} on top of {@code rel}. */
    private static <T extends Entity> T stand(GameTestHelper helper, T entity, BlockPos rel) {
        Vec3 on = helper.absoluteVec(Vec3.atBottomCenterOf(rel.above()));
        entity.snapTo(on.x, on.y, on.z, 0.0F, 0.0F);
        return entity;
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
    /**
     * Every recipe file this mod ships either loaded, or carries this loader's load conditions and was
     * skipped by them. A file with neither failed to parse. On Fabric that was every conditional
     * recipe for a while: Fabric's datagen wrote them without conditions, and the NeoForge copy that
     * shadowed it carries a key Fabric ignores - so only this loader's own key counts.
     */
    private static void everyRecipeLoadsOrIsConditionedOff(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        FileToIdConverter recipes = FileToIdConverter.json("recipe");
        String conditionsKey = Services.PLATFORM.getPlatformName().equals("Fabric") ? "fabric:load_conditions" : "neoforge:conditions";
        List<String> failed = new ArrayList<>();

        recipes.listMatchingResources(server.getResourceManager()).forEach((file, resource) -> {
            Identifier id = recipes.fileToId(file);
            if (!id.getNamespace().equals(Constants.MOD_ID) || server.getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, id)).isPresent()) {
                return;
            }

            try (BufferedReader reader = resource.openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (!json.has(conditionsKey)) {
                    failed.add(id.toString());
                }
            } catch (IOException e) {
                failed.add(id + " (" + e.getMessage() + ")");
            }
        });

        helper.assertTrue(failed.isEmpty(), failed.size() + " recipes failed to load without being conditioned off: " + String.join(", ", failed.subList(0, Math.min(10, failed.size()))));
        helper.succeed();
    }
}
