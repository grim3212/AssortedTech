package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.GravityBlock;
import com.grim3212.assorted.tech.common.block.GravityDirectionalBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GravityBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GravityDirectionalBlockEntity;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.tech.gametest.TechTestSupport.*;

/**
 * Gravitors, attractors and repulsors, their directional forms, redstone, range and gravity boots.
 */
final class GravityTests {

    private GravityTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("gravitor_lifts_only_when_powered", GravityTests::gravitorLiftsOnlyWhenPowered);
        out.accept("directional_repulsor_pushes_entity", GravityTests::directionalRepulsorPushesEntity);
        out.accept("gravity_boots_exempt_wearer", GravityTests::gravityBootsExemptWearer);
        out.accept("attractor_pulls_entity_in", GravityTests::attractorPullsEntityIn);
        out.accept("repulsor_pushes_entity_away", GravityTests::repulsorPushesEntityAway);
        out.accept("directional_gravity_ignores_off_axis", GravityTests::directionalGravityIgnoresOffAxis);
        out.accept("redstone_toggles_gravity_blocks", GravityTests::redstoneTogglesGravityBlocks);
        out.accept("gravity_boots_exempt_player", GravityTests::gravityBootsExemptPlayer);
        out.accept("gravity_boots_exempt_a_mob_from_a_gravitor", GravityTests::gravityBootsExemptAMobFromAGravitor);
        out.accept("empty_hand_cycles_gravity_range", GravityTests::emptyHandCyclesGravityRange);
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
     * This is the gravity <em>bridge</em>; {@code gravity_boots_exempt_player} and
     * {@code gravity_boots_exempt_a_mob_from_a_gravitor} cover the gravity blocks.
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
     * human to check.
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
     * A mob wearing gravity boots is exempt from a gravitor, as a player is. The gravity blocks used to
     * exempt only players, while the gravity bridge exempted any living entity.
     */
    private static void gravityBootsExemptAMobFromAGravitor(GameTestHelper helper) {
        BlockPos gravitor = new BlockPos(4, 1, 4);
        BlockPos lever = new BlockPos(4, 0, 4);
        BlockPos bareStand = new BlockPos(3, 1, 4);
        BlockPos bootedStand = new BlockPos(5, 1, 4);

        helper.setBlock(bareStand, Blocks.STONE);
        helper.setBlock(bootedStand, Blocks.STONE);
        helper.setBlock(gravitor, TechBlocks.GRAVITOR.get());
        helper.setBlock(lever, Blocks.REDSTONE_BLOCK);

        Pig bare = helper.spawnWithNoFreeWill(EntityTypes.PIG, bareStand.above());
        Pig booted = helper.spawnWithNoFreeWill(EntityTypes.PIG, bootedStand.above());
        booted.setItemSlot(EquipmentSlot.FEET, new ItemStack(TechItems.GRAVITY_BOOTS.get()));

        double[] lift = {0.0D, 0.0D};

        helper.startSequence()
                .thenExecute(() -> helper.assertBlockProperty(gravitor, GravityBlock.POWERED, true))
                .thenExecuteFor(30, () -> {
                    lift[0] = Math.max(lift[0], bare.getDeltaMovement().y);
                    lift[1] = Math.max(lift[1], booted.getDeltaMovement().y);
                })
                .thenExecute(() -> {
                    helper.assertTrue(lift[0] > 0.05D,
                            "a powered gravitor did not lift a bare pig - its best upward speed was " + lift[0]);
                    helper.assertTrue(lift[1] < 0.01D,
                            "a powered gravitor lifted a pig wearing gravity boots - it reached " + lift[1]);
                })
                .thenSucceed();
    }
}
