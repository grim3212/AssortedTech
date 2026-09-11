package com.grim3212.assorted.tech.gametest;

import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.component.DataComponents;
import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.BridgeControlBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeControlBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.tech.gametest.TechTestSupport.*;

/**
 * Bridge controls: projecting and clearing, which blocks they take, the stored state and bridge effects.
 */
final class BridgeTests {

    private BridgeTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("bridge_control_projects_and_clears", BridgeTests::bridgeControlProjectsAndClears);
        out.accept("bridge_control_refuses_non_full_cube", BridgeTests::bridgeControlRefusesNonFullCube);
        out.accept("every_bridge_control_projects_and_clears", BridgeTests::everyBridgeControlProjectsAndClears);
        out.accept("bridge_copies_controls_stored_state", BridgeTests::bridgeCopiesControlsStoredState);
        out.accept("bridge_placed_from_an_item_keeps_its_block", BridgeTests::bridgePlacedFromAnItemKeepsItsBlock);
        out.accept("bridge_effects_apply_to_entities", BridgeTests::bridgeEffectsApplyToEntities);
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
     * A bridge control only accepts a block that fills its collision box: a projected segment
     * always renders as a full cube, so a slab is refused.
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
     * All five controls project their own kind of bridge while powered and clear it when the signal
     * goes. The types differ in {@link BridgeType#isSolid()} and {@code entityInside}, and a wrong
     * type would still look right in a screenshot.
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
     * Every segment of a projected bridge carries the controller's stored state (what it draws as)
     * and the run's direction (how a broken segment finds its controller). The state is set by
     * right-clicking the controller with a block item, as a player would.
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
     * The bridges that act on what touches them: accel gives Speed on {@code stepOn}, death hurts
     * on {@code entityInside}, gravity flings along its facing. Health is read five ticks in,
     * inside the invulnerability window after the first hit.
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
     * A bridge placed from an item that carries a stored block keeps it. Nothing in play writes one
     * onto a stack - pick-block writes air - but a command or another mod can, and the bridge item
     * model already draws it.
     */
    private static void bridgePlacedFromAnItemKeepsItsBlock(GameTestHelper helper) {
        BlockPos floor = new BlockPos(4, 1, 4);
        helper.setBlock(floor, Blocks.STONE);

        ItemStack stack = new ItemStack(TechBlocks.BRIDGE.get());
        NBTHelper.putTag(stack, "stored_state", NbtUtils.writeBlockState(Blocks.GOLD_BLOCK.defaultBlockState()));
        ServerPlayer player = survivalPlayer(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        BlockPos at = helper.absolutePos(floor);
        BlockHitResult top = new BlockHitResult(Vec3.atCenterOf(at).relative(Direction.UP, 0.5D), Direction.UP, at, false);
        InteractionResult result = player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND, top);
        helper.assertTrue(result.consumesAction(), "placing a bridge from an item came back as " + result);

        BridgeBlockEntity bridge = helper.getBlockEntity(floor.above(), BridgeBlockEntity.class);
        helper.assertTrue(bridge.getStoredBlockState().is(Blocks.GOLD_BLOCK), "a bridge placed from an item holding gold stores " + bridge.getStoredBlockState());
        helper.assertTrue(bridge.components().get(DataComponents.CUSTOM_DATA) == null, "the stored block was also kept on the bridge as custom data");
        helper.succeed();
    }
}
