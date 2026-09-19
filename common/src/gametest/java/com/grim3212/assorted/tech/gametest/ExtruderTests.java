package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.common.entity.ExtruderEntity;
import com.grim3212.assorted.tech.common.entity.TechEntities;
import com.grim3212.assorted.tech.common.inventory.ExtruderMenu;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.lib.test.TestSupport.*;

/**
 * The extruder: placing it, running it through stone, stopping at obsidian, its screen buttons, and
 * breaking it.
 */
final class ExtruderTests {

    private ExtruderTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("extruder_is_placed_facing_where_the_player_looks", ExtruderTests::extruderIsPlacedFacingWhereThePlayerLooks);
        out.accept("extruder_mines_ahead_and_lays_blocks_behind", ExtruderTests::extruderMinesAheadAndLaysBlocksBehind);
        out.accept("extruder_stops_at_obsidian", ExtruderTests::extruderStopsAtObsidian);
        out.accept("extruder_screen_buttons_turn_and_start_it", ExtruderTests::extruderScreenButtonsTurnAndStartIt);
        out.accept("extruder_empty_handed_punch_starts_and_stops_it", ExtruderTests::extruderEmptyHandedPunchStartsAndStopsIt);
        out.accept("extruder_creative_double_punch_breaks_it", ExtruderTests::extruderCreativeDoublePunchBreaksIt);
        out.accept("extruder_estimates_how_far_its_fuel_lasts", ExtruderTests::extruderEstimatesHowFarItsFuelLasts);
        out.accept("extruder_material_decides_what_it_mines_and_holds", ExtruderTests::extruderMaterialDecidesWhatItMinesAndHolds);
        out.accept("extruder_recipes_climb_a_level_at_a_time", ExtruderTests::extruderRecipesClimbALevelAtATime);
        out.accept("extruder_breaks_into_itself_and_its_contents", ExtruderTests::extruderBreaksIntoItselfAndItsContents);
    }

    /**
     * Built straight from the extruder: right clicking opens it through NeoForge's open screen
     * payload, which the test player's connection cannot take, as in the GPS sensor tests.
     */
    private static ExtruderMenu openMenu(ServerPlayer player, ExtruderEntity extruder) {
        ExtruderMenu menu = (ExtruderMenu) extruder.createMenu(1, player.getInventory(), player);
        player.containerMenu = menu;
        return menu;
    }

    /** An iron extruder, which mines stone and has every slot the tests use. */
    private static ExtruderEntity extruder(GameTestHelper helper, BlockPos rel, Direction facing) {
        return extruder(helper, rel, facing, ExtruderType.IRON);
    }

    private static ExtruderEntity extruder(GameTestHelper helper, BlockPos rel, Direction facing, ExtruderType type) {
        ExtruderEntity extruder = TechEntities.EXTRUDER.get().create(helper.getLevel(), EntitySpawnReason.MOB_SUMMONED);
        extruder.setExtruderType(type);
        extruder.placeAt(helper.absolutePos(rel));
        extruder.setFacing(facing);
        helper.getLevel().addFreshEntity(extruder);
        // A running extruder would drive out of the test box and through its neighbours.
        helper.runBeforeTestEnd(() -> {
            extruder.getContainer().clearContent();
            extruder.discard();
        });
        return extruder;
    }

    private static void extruderIsPlacedFacingWhereThePlayerLooks(GameTestHelper helper) {
        final BlockPos wall = new BlockPos(4, 1, 5);
        helper.setBlock(wall, Blocks.STONE);
        ServerPlayer player = survivalPlayer(helper, new ItemStack(TechItems.extruder(ExtruderType.DIAMOND)));
        stand(helper, player, new BlockPos(4, 0, 2));
        player.setYRot(0.0F);
        player.setXRot(0.0F);

        useOnTopOf(helper, player, wall);
        ExtruderEntity placed = helper.getEntities(TechEntities.EXTRUDER.get()).stream().findFirst().orElse(null);
        helper.assertTrue(placed != null, "no extruder was placed");
        helper.assertValueEqual(placed.currentBlock(), helper.absolutePos(wall.above()), "the block the extruder was placed in");
        helper.assertValueEqual(placed.getFacing(), Direction.SOUTH, "the facing of an extruder placed by a player looking south");
        helper.assertValueEqual(placed.getExtruderType(), ExtruderType.DIAMOND, "the material of an extruder placed from a diamond extruder");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "placing the extruder did not use up the item");

        helper.succeed();
    }

    /**
     * Driving east through stone: what it mines goes into its inventory, and the first extrusion slot
     * is laid into the block it started in once it has moved clear of it.
     */
    private static void extruderMinesAheadAndLaysBlocksBehind(GameTestHelper helper) {
        final BlockPos start = new BlockPos(1, 2, 4);
        final BlockPos first = start.east();
        final BlockPos second = first.east();
        helper.setBlock(first, Blocks.STONE);
        helper.setBlock(second, Blocks.STONE);

        ExtruderEntity extruder = extruder(helper, start, Direction.EAST);
        extruder.getContainer().setItem(ExtruderEntity.FUEL_SLOT, new ItemStack(Items.COAL));
        extruder.getContainer().setItem(ExtruderEntity.FIRST_EXTRUDE_SLOT, new ItemStack(Blocks.OAK_PLANKS, 4));
        extruder.setRunning(true);

        helper.startSequence()
                .thenWaitUntil(() -> {
                    helper.assertBlockPresent(Blocks.AIR, second);
                    helper.assertBlockPresent(Blocks.OAK_PLANKS, start);
                })
                .thenExecute(() -> {
                    int mined = 0;
                    for (int slot = ExtruderEntity.FIRST_MINED_SLOT; slot < ExtruderEntity.SLOTS; slot++) {
                        if (extruder.getContainer().getItem(slot).is(Items.COBBLESTONE)) {
                            mined += extruder.getContainer().getItem(slot).getCount();
                        }
                    }
                    helper.assertValueEqual(mined, 2, "cobblestone mined into the extruder");
                    helper.assertTrue(extruder.getContainer().getItem(ExtruderEntity.FUEL_SLOT).isEmpty(), "the coal was not burnt");
                    helper.assertValueEqual(extruder.getContainer().getItem(ExtruderEntity.FIRST_EXTRUDE_SLOT).getCount(), 3, "planks left after laying one");
                })
                .thenSucceed();
    }

    /** A block it cannot mine switches it off, as its Stop button would, rather than leaving it pushing. */
    private static void extruderStopsAtObsidian(GameTestHelper helper) {
        final BlockPos start = new BlockPos(1, 2, 4);
        helper.setBlock(start.east(), Blocks.OBSIDIAN);

        ExtruderEntity extruder = extruder(helper, start, Direction.EAST);
        extruder.getContainer().setItem(ExtruderEntity.FUEL_SLOT, new ItemStack(Items.COAL));
        extruder.setRunning(true);
        Vec3 from = extruder.position();

        helper.startSequence()
                .thenIdle(20)
                .thenExecute(() -> {
                    helper.assertBlockPresent(Blocks.OBSIDIAN, start.east());
                    helper.assertTrue(extruder.position().distanceTo(from) < 0.01D, "the extruder moved into obsidian");
                    helper.assertFalse(extruder.isRunning(), "the extruder kept running at obsidian");
                })
                .thenSucceed();
    }

    /** The screen's buttons are vanilla menu button clicks, handled by the server's menu. */
    private static void extruderScreenButtonsTurnAndStartIt(GameTestHelper helper) {
        ExtruderEntity extruder = extruder(helper, new BlockPos(4, 2, 4), Direction.NORTH);
        ServerPlayer player = survivalPlayer(helper);
        stand(helper, player, new BlockPos(4, 1, 2));

        ExtruderMenu menu = openMenu(player, extruder);

        menu.clickMenuButton(player, Direction.UP.get3DDataValue());
        helper.assertValueEqual(extruder.getFacing(), Direction.UP, "the extruder's facing after the Up button");
        menu.clickMenuButton(player, ExtruderEntity.TOGGLE_BUTTON);
        helper.assertTrue(extruder.isRunning(), "Start did not start the extruder");
        helper.assertValueEqual(menu.getFacing(), Direction.UP, "the facing the menu reports");
        menu.clickMenuButton(player, ExtruderEntity.TOGGLE_BUTTON);
        helper.assertFalse(extruder.isRunning(), "Stop did not stop the extruder");

        helper.assertTrue(menu.stillValid(player), "the menu closed with the player beside the extruder");
        player.teleportTo(player.getX() + 20.0D, player.getY(), player.getZ());
        helper.assertFalse(menu.stillValid(player), "the menu stayed open with the player 20 blocks away");

        helper.succeed();
    }

    /**
     * An empty handed punch starts and stops it and does it no harm, as in GrimPack, but will not
     * start it with nothing to burn.
     */
    private static void extruderEmptyHandedPunchStartsAndStopsIt(GameTestHelper helper) {
        ExtruderEntity extruder = extruder(helper, new BlockPos(4, 2, 4), Direction.NORTH);
        ServerPlayer player = survivalPlayer(helper);
        stand(helper, player, new BlockPos(4, 1, 2));

        extruder.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
        helper.assertFalse(extruder.isRunning(), "a punch started an extruder with no fuel");
        extruder.getContainer().setItem(ExtruderEntity.FUEL_SLOT, new ItemStack(Items.COAL));

        for (int punch = 0; punch < 10; punch++) {
            extruder.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
            helper.assertValueEqual(extruder.isRunning(), punch % 2 == 0, "running after punch " + (punch + 1));
        }
        helper.assertTrue(extruder.isAlive() && extruder.getDamage() == 0.0F, "empty handed punches damaged the extruder");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        extruder.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
        helper.assertTrue(extruder.getDamage() > 0.0F, "a punch holding a stick did no damage");
        helper.assertFalse(extruder.isRunning(), "a punch holding a stick started the extruder");

        helper.succeed();
    }

    /**
     * In creative, one empty handed punch starts or stops it like anyone's, and a second straight
     * after breaks it, spilling what it holds but not dropping the extruder.
     */
    private static void extruderCreativeDoublePunchBreaksIt(GameTestHelper helper) {
        final BlockPos pos = new BlockPos(4, 2, 4);
        ExtruderEntity extruder = extruder(helper, pos, Direction.UP);
        extruder.getContainer().setItem(ExtruderEntity.FUEL_SLOT, new ItemStack(Items.COAL));
        extruder.getContainer().setItem(ExtruderEntity.FIRST_MINED_SLOT, new ItemStack(Items.COBBLESTONE, 10));
        ServerPlayer player = survivalPlayer(helper);
        player.setGameMode(GameType.CREATIVE);
        stand(helper, player, new BlockPos(4, 1, 2));
        Runnable punch = () -> extruder.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);

        punch.run();
        helper.assertTrue(extruder.isAlive() && extruder.isRunning(), "one creative punch did not just start the extruder");

        helper.startSequence()
                .thenIdle(15)
                .thenExecute(() -> {
                    punch.run();
                    helper.assertTrue(extruder.isAlive() && !extruder.isRunning(), "a creative punch well after the last did not just stop it");
                    punch.run();
                    helper.assertFalse(extruder.isAlive(), "two creative punches in a row did not break it");
                    helper.assertItemEntityPresent(Items.COBBLESTONE);
                    helper.assertItemEntityNotPresent(TechItems.extruder(ExtruderType.IRON));
                })
                .thenSucceed();
    }

    /**
     * The screen's Mine estimate matches a real run: one coal through a row of stone mines exactly
     * as many blocks as it says, the last one on whatever fuel is left, and then the extruder stops
     * itself.
     */
    private static void extruderEstimatesHowFarItsFuelLasts(GameTestHelper helper) {
        final BlockPos start = new BlockPos(0, 2, 4);
        for (int x = 1; x <= 8; x++) {
            helper.setBlock(start.east(x), Blocks.STONE);
        }
        ItemStack coal = new ItemStack(Items.COAL);
        int expected = ExtruderEntity.blocksFor(0, ExtruderEntity.fuelValue(helper.getLevel(), coal), 1, ExtruderType.IRON.getSpeed(),
                ExtruderType.IRON.blockCost(TechCommonMod.COMMON_CONFIG.extruderFuelPerMinedBlock.get()), false);
        helper.assertTrue(expected > 0 && expected < 8, "one coal should mine part of the row, but the estimate is " + expected);
        helper.assertValueEqual(ExtruderEntity.blocksFor(0, 0, 0, 1.0F, 0, false), 0, "blocks for no fuel");

        ExtruderEntity miner = extruder(helper, start, Direction.EAST);
        miner.getContainer().setItem(ExtruderEntity.FUEL_SLOT, coal);
        miner.setRunning(true);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertFalse(miner.isRunning(), "the extruder kept running with no fuel left"))
                .thenExecute(() -> {
                    int mined = 0;
                    for (int x = 1; x <= 8; x++) {
                        mined += helper.getBlockState(start.east(x)).isAir() ? 1 : 0;
                    }
                    helper.assertValueEqual(mined, expected, "blocks one coal mined against the screen's estimate");
                })
                .thenSucceed();
    }

    /**
     * The tools an extruder is made from decide what it mines: a wooden one stops at iron ore that an
     * iron one mines. Its tier decides how many slots its screen has, and each level spends less fuel on
     * each block, netherite half. The Assorted Tools materials
     * have no tools here, so they cannot be made or mine at all.
     */
    private static void extruderMaterialDecidesWhatItMinesAndHolds(GameTestHelper helper) {
        helper.assertTrue(ExtruderType.WOOD.toolFor(Blocks.IRON_ORE.defaultBlockState()).isEmpty(), "wooden tools can take iron ore's drops");
        helper.assertTrue(ExtruderType.STONE.toolFor(Blocks.IRON_ORE.defaultBlockState()).is(Items.STONE_PICKAXE), "a stone extruder does not mine iron ore with its pickaxe");
        helper.assertTrue(ExtruderType.WOOD.toolFor(Blocks.DIRT.defaultBlockState()).is(Items.WOODEN_SHOVEL), "a wooden extruder does not dig dirt with its shovel");
        helper.assertTrue(ExtruderType.WOOD.toolFor(Blocks.GLASS.defaultBlockState()).is(Items.WOODEN_PICKAXE), "a block needing no tool is not mined with the pickaxe");
        helper.assertFalse(ExtruderType.DIAMOND.isUncraftable(), "the diamond extruder has no tools");
        helper.assertTrue(ExtruderType.STEEL.isUncraftable() && ExtruderType.STEEL.toolFor(Blocks.DIRT.defaultBlockState()).isEmpty(), "the steel extruder has tools without Assorted Tools");

        final BlockPos woodStart = new BlockPos(1, 2, 4);
        final BlockPos ironStart = new BlockPos(1, 2, 1);
        helper.setBlock(woodStart.east(), Blocks.IRON_ORE);
        helper.setBlock(ironStart.east(), Blocks.IRON_ORE);
        ExtruderEntity wooden = extruder(helper, woodStart, Direction.EAST, ExtruderType.WOOD);
        ExtruderEntity iron = extruder(helper, ironStart, Direction.EAST, ExtruderType.IRON);
        for (ExtruderEntity extruder : List.of(wooden, iron)) {
            extruder.getContainer().setItem(ExtruderEntity.FUEL_SLOT, new ItemStack(Items.COAL));
            extruder.setRunning(true);
        }

        ServerPlayer player = survivalPlayer(helper);
        stand(helper, player, new BlockPos(4, 1, 6));
        helper.assertValueEqual(wooden.createMenu(1, player.getInventory(), player).slots.size(), 1 + 3 + 9 + 36, "slots in a wooden extruder's menu");
        helper.assertValueEqual(iron.createMenu(1, player.getInventory(), player).slots.size(), 1 + 6 + 18 + 36, "slots in an iron extruder's menu");
        ExtruderEntity diamond = extruder(helper, new BlockPos(6, 2, 7), Direction.UP, ExtruderType.DIAMOND);
        helper.assertValueEqual(diamond.createMenu(1, player.getInventory(), player).slots.size(), 1 + 9 + 27 + 36, "slots in a diamond extruder's menu");
        diamond.setExtruderType(ExtruderType.NETHERITE);
        helper.assertValueEqual(diamond.createMenu(1, player.getInventory(), player).slots.size(), 1 + 9 + 27 + 36, "slots in a netherite extruder's menu, the same as diamond");
        helper.assertValueEqual(ExtruderType.NETHERITE.blockCost(400), 200, "a netherite extruder's cost for a 400 fuel block");
        helper.assertValueEqual(ExtruderType.DIAMOND.blockCost(400), 250, "a diamond extruder's cost for a 400 fuel block");
        helper.assertValueEqual(ExtruderType.WOOD.blockCost(400), 400, "a wooden extruder's cost for a 400 fuel block");
        helper.assertValueEqual(ExtruderType.GOLD.getLevel(), 0, "the gold extruder's level");
        helper.assertValueEqual(ExtruderType.STEEL.getLevel(), 3, "the steel extruder's level");
        helper.assertValueEqual(ExtruderType.NETHERITE.getLevel(), 4, "the netherite extruder's level");
        helper.assertValueEqual(ExtruderType.WOOD.getSpeed(), 0.5625F, "the wooden extruder's speed");
        helper.assertValueEqual(ExtruderType.NETHERITE.getSpeed(), 1.3125F, "the netherite extruder's speed");

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertBlockPresent(Blocks.AIR, ironStart.east()))
                .thenIdle(10)
                .thenExecute(() -> {
                    helper.assertBlockPresent(Blocks.IRON_ORE, woodStart.east());
                    helper.assertFalse(wooden.isRunning(), "the wooden extruder kept running at iron ore it cannot mine");
                    helper.assertTrue(iron.getContainer().getItem(ExtruderEntity.FIRST_MINED_SLOT).is(Items.RAW_IRON), "the iron extruder did not mine raw iron");
                })
                .thenSucceed();
    }

    /**
     * A stone extruder is its pickaxe over its axe, a level 0 extruder and its shovel, either way
     * round; a level 1 extruder in the middle is the wrong level, and a wooden one needs its piston
     * and dispenser.
     */
    private static void extruderRecipesClimbALevelAtATime(GameTestHelper helper) {
        ItemStack none = ItemStack.EMPTY;
        ItemStack pickaxe = new ItemStack(Items.STONE_PICKAXE);
        ItemStack shovel = new ItemStack(Items.STONE_SHOVEL);
        ItemStack axe = new ItemStack(Items.STONE_AXE);
        ItemStack wooden = new ItemStack(TechItems.extruder(ExtruderType.WOOD));

        helper.assertTrue(craft(helper, 3, 2, none, pickaxe, none, axe, wooden, shovel).is(TechItems.extruder(ExtruderType.STONE)), "axe, wooden extruder, shovel did not make a stone extruder");
        helper.assertTrue(craft(helper, 3, 2, none, pickaxe, none, shovel, new ItemStack(TechItems.extruder(ExtruderType.GOLD)), axe).is(TechItems.extruder(ExtruderType.STONE)), "shovel, gold extruder, axe did not make a stone extruder");
        helper.assertTrue(craft(helper, 3, 2, none, pickaxe, none, axe, new ItemStack(TechItems.extruder(ExtruderType.COPPER)), shovel).isEmpty(), "a level 1 extruder made a stone extruder");
        helper.assertTrue(craft(helper, 3, 3, none, new ItemStack(Items.WOODEN_PICKAXE), none, new ItemStack(Items.WOODEN_AXE), new ItemStack(Items.PISTON), new ItemStack(Items.WOODEN_SHOVEL), none, new ItemStack(Items.DISPENSER), none)
                .is(TechItems.extruder(ExtruderType.WOOD)), "wooden tools round a piston over a dispenser did not make a wooden extruder");

        helper.succeed();
    }

    private static ItemStack craft(GameTestHelper helper, int width, int height, ItemStack... grid) {
        CraftingInput input = CraftingInput.of(width, height, List.of(grid));
        return helper.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel()).map(recipe -> recipe.value().assemble(input)).orElse(ItemStack.EMPTY);
    }

    /** Punched enough while sneaking, it drops as an item along with everything it was holding. */
    private static void extruderBreaksIntoItselfAndItsContents(GameTestHelper helper) {
        ExtruderEntity extruder = extruder(helper, new BlockPos(4, 2, 4), Direction.NORTH);
        extruder.getContainer().setItem(ExtruderEntity.FIRST_MINED_SLOT, new ItemStack(Items.COBBLESTONE, 10));
        ServerPlayer player = survivalPlayer(helper);
        stand(helper, player, new BlockPos(4, 1, 2));
        player.setShiftKeyDown(true);

        for (int hit = 0; hit < 10 && extruder.isAlive(); hit++) {
            extruder.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
        }

        helper.assertFalse(extruder.isAlive(), "the extruder survived ten punches");
        helper.assertItemEntityPresent(TechItems.extruder(ExtruderType.IRON), new BlockPos(4, 2, 4), 2.0D);
        helper.assertItemEntityPresent(Items.COBBLESTONE, new BlockPos(4, 2, 4), 2.0D);

        helper.succeed();
    }
}
