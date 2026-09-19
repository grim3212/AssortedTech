package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.lib.core.inventory.MenuData;
import com.grim3212.assorted.tech.api.util.GpsSensorMode;
import com.grim3212.assorted.tech.common.block.GpsSensorBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorFilter;
import com.grim3212.assorted.tech.common.item.GpsTarget;
import com.grim3212.assorted.tech.common.item.TechDataComponents;
import com.grim3212.assorted.tech.common.inventory.GpsSensorMenu;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.network.GpsSensorFilterPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.lib.test.TestSupport.*;

/**
 * The GPS and the GPS sensors: storing a position, handing it to a sensor, and what the sensor
 * then sees there.
 */
final class GpsSensorTests {

    private GpsSensorTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("gps_stores_the_space_in_front_of_a_face", GpsSensorTests::gpsStoresTheSpaceInFrontOfAFace);
        out.accept("gps_sensor_watches_the_gps_in_its_slot", GpsSensorTests::gpsSensorWatchesTheGpsInItsSlot);
        out.accept("gps_sensor_detects_at_its_position", GpsSensorTests::gpsSensorDetectsAtItsPosition);
        out.accept("upgraded_gps_sensor_signal_counts_what_it_sees", GpsSensorTests::upgradedGpsSensorSignalCountsWhatItSees);
        out.accept("gps_sensor_filters_what_it_matches", GpsSensorTests::gpsSensorFiltersWhatItMatches);
        out.accept("upgraded_gps_sensor_takes_lists_and_tags", GpsSensorTests::upgradedGpsSensorTakesListsAndTags);
        out.accept("gps_sensor_settings_are_bounded", GpsSensorTests::gpsSensorSettingsAreBounded);
        out.accept("gps_sensor_menu_takes_a_ghost_item", GpsSensorTests::gpsSensorMenuTakesAGhostItem);
    }

    private static void gpsStoresTheSpaceInFrontOfAFace(GameTestHelper helper) {
        final BlockPos ground = new BlockPos(4, 1, 4);
        helper.setBlock(ground, Blocks.STONE);
        ServerPlayer player = survivalPlayer(helper, new ItemStack(TechItems.GPS.get()));
        stand(helper, player, new BlockPos(4, 1, 2));

        useOnTopOf(helper, player, ground);
        GpsTarget stored = player.getMainHandItem().get(TechDataComponents.GPS_TARGET.get());
        helper.assertTrue(stored != null, "the GPS stored nothing");
        helper.assertValueEqual(stored.target(), GlobalPos.of(helper.getLevel().dimension(), helper.absolutePos(ground.above())), "the stored position");

        player.setShiftKeyDown(true);
        player.getMainHandItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertFalse(player.getMainHandItem().has(TechDataComponents.GPS_TARGET.get()), "sneak using the GPS did not clear it");

        helper.succeed();
    }

    /**
     * The sensor watches whatever the GPS in its slot stored, so long as it is here and in range; a
     * GPS shift clicks in and out of the slot, and comes back out when the sensor is broken.
     */
    private static void gpsSensorWatchesTheGpsInItsSlot(GameTestHelper helper) {
        final BlockPos sensorPos = new BlockPos(1, 1, 1);
        final BlockPos watched = new BlockPos(4, 1, 4);
        helper.setBlock(sensorPos, TechBlocks.GPS_SENSOR.get());
        GpsSensorBlockEntity sensor = helper.getBlockEntity(sensorPos, GpsSensorBlockEntity.class);
        Container slot = sensor.getGpsContainer();
        ResourceKey<Level> here = helper.getLevel().dimension();

        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.NONE, "a sensor with no GPS");
        slot.setItem(0, new ItemStack(TechItems.GPS.get()));
        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.NONE, "a sensor holding a GPS with nothing stored");
        slot.setItem(0, gps(here, helper.absolutePos(sensorPos).offset(sensor.getRange() + 1, 0, 0)));
        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.OUT_OF_RANGE, "a GPS position out of range");
        slot.setItem(0, gps(Level.NETHER, helper.absolutePos(watched)));
        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.OTHER_DIMENSION, "a GPS position in the nether");
        slot.setItem(0, gps(here, helper.absolutePos(watched)));
        helper.assertValueEqual(sensor.getTarget(), helper.absolutePos(watched), "the position the sensor watches");
        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.GOOD, "an empty position in range");
        helper.setBlock(watched, Blocks.STONE);
        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.BLOCKED, "a position filled with stone");
        helper.setBlock(watched, Blocks.AIR);

        // The GPS slot takes only a GPS, one at a time, and shift clicks both ways.
        slot.setItem(0, ItemStack.EMPTY);
        ServerPlayer player = survivalPlayer(helper, gps(here, helper.absolutePos(watched)));
        stand(helper, player, new BlockPos(2, 1, 3));
        GpsSensorMenu menu = openMenu(player, sensor);
        helper.assertFalse(menu.getSlot(GpsSensorMenu.GPS_SLOT).mayPlace(new ItemStack(Items.COMPASS)), "the GPS slot took a compass");
        int handSlot = menu.slots.size() - 9;
        menu.quickMoveStack(player, handSlot);
        helper.assertTrue(player.getMainHandItem().isEmpty() && slot.getItem(0).is(TechItems.GPS.get()), "shift clicking the held GPS did not move it into the slot");
        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.GOOD, "the sensor after shift clicking its GPS in");
        menu.quickMoveStack(player, GpsSensorMenu.GPS_SLOT);
        helper.assertTrue(slot.getItem(0).isEmpty() && countInInventory(player, TechItems.GPS.get()) == 1, "shift clicking the GPS slot did not give the GPS back");
        helper.assertValueEqual(sensor.targetStatus(), GpsSensorBlockEntity.TargetStatus.NONE, "the sensor with its GPS taken out");

        slot.setItem(0, gps(here, helper.absolutePos(watched)));
        helper.destroyBlock(sensorPos);
        helper.assertItemEntityPresent(TechItems.GPS.get(), sensorPos, 2.0D);

        helper.succeed();
    }

    /** Detection happens at the GPS position, not in front of the sensor, and a filter narrows it. */
    private static void gpsSensorDetectsAtItsPosition(GameTestHelper helper) {
        final BlockPos sensorPos = new BlockPos(1, 1, 1);
        final BlockPos watched = new BlockPos(4, 1, 4);
        helper.setBlock(sensorPos, TechBlocks.GPS_SENSOR.get());
        GpsSensorBlockEntity sensor = helper.getBlockEntity(sensorPos, GpsSensorBlockEntity.class);
        sensor.getGpsContainer().setItem(0, gps(helper.getLevel().dimension(), helper.absolutePos(watched)));
        sensor.configure(GpsSensorMode.MOBS, "", ItemStack.EMPTY, 0, false);

        helper.startSequence()
                .thenExecute(() -> {
                    helper.assertBlockProperty(sensorPos, GpsSensorBlock.ACTIVE, false);
                    // In front of the sensor, where a plain sensor would look, is ignored.
                    helper.spawnWithNoFreeWill(EntityTypes.PIG, sensorPos.north().east());
                })
                .thenIdle(5)
                .thenExecute(() -> helper.assertBlockProperty(sensorPos, GpsSensorBlock.ACTIVE, false))
                .thenExecute(() -> helper.spawnWithNoFreeWill(EntityTypes.PIG, watched))
                .thenWaitUntil(() -> {
                    helper.assertBlockProperty(sensorPos, GpsSensorBlock.ACTIVE, true);
                    helper.assertRedstoneSignal(sensorPos, Direction.NORTH, signal -> signal == 15, () -> Component.literal("a detecting GPS sensor did not emit 15"));
                })
                .thenExecute(() -> sensor.configure(GpsSensorMode.MOBS, "minecraft:cow", ItemStack.EMPTY, 0, false))
                .thenWaitUntil(() -> helper.assertBlockProperty(sensorPos, GpsSensorBlock.ACTIVE, false))
                .thenSucceed();
    }

    /** The upgraded sensor's signal counts what it sees, items one by one, and stops at 15. */
    private static void upgradedGpsSensorSignalCountsWhatItSees(GameTestHelper helper) {
        final BlockPos sensorPos = new BlockPos(1, 1, 1);
        final BlockPos watched = new BlockPos(4, 1, 4);
        helper.setBlock(sensorPos, TechBlocks.UPGRADED_GPS_SENSOR.get());
        GpsSensorBlockEntity sensor = helper.getBlockEntity(sensorPos, GpsSensorBlockEntity.class);
        sensor.getGpsContainer().setItem(0, gps(helper.getLevel().dimension(), helper.absolutePos(watched)));
        sensor.configure(GpsSensorMode.MOBS, "", ItemStack.EMPTY, 1, false);
        for (int i = 0; i < 3; i++) {
            helper.spawnWithNoFreeWill(EntityTypes.PIG, watched);
        }

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertRedstoneSignal(sensorPos, Direction.NORTH, signal -> signal == 3, () -> Component.literal("three pigs did not give a signal of 3, but " + sensor.getSignal())))
                .thenExecute(() -> {
                    sensor.configure(GpsSensorMode.ITEMS, "", ItemStack.EMPTY, 1, false);
                    helper.spawnItem(Items.APPLE, watched).getItem().setCount(20);
                })
                .thenWaitUntil(() -> helper.assertRedstoneSignal(sensorPos, Direction.NORTH, signal -> signal == 15, () -> Component.literal("20 dropped apples did not give a signal of 15, but " + sensor.getSignal())))
                .thenSucceed();
    }

    private static void gpsSensorFiltersWhatItMatches(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TechBlocks.GPS_SENSOR.get());
        GpsSensorBlockEntity sensor = helper.getBlockEntity(new BlockPos(1, 1, 1), GpsSensorBlockEntity.class);
        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 4));
        Cow cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(6, 1, 4));
        ServerPlayer player = survivalPlayer(helper);
        ItemEntity apple = helper.spawnItem(Items.APPLE, new BlockPos(2, 1, 6));

        sensor.configure(GpsSensorMode.MOBS, "", ItemStack.EMPTY, 0, false);
        helper.assertTrue(sensor.matches(pig) && sensor.matches(cow) && !sensor.matches(player) && !sensor.matches(apple), "an unfiltered mob sensor sees every mob and nothing else");
        sensor.configure(GpsSensorMode.MOBS, "minecraft:pig", ItemStack.EMPTY, 0, false);
        helper.assertTrue(sensor.matches(pig) && !sensor.matches(cow), "a pig filter sees only pigs");

        sensor.configure(GpsSensorMode.PLAYERS, "", ItemStack.EMPTY, 0, false);
        helper.assertTrue(sensor.matches(player) && !sensor.matches(pig), "an unfiltered player sensor sees players only");
        sensor.configure(GpsSensorMode.PLAYERS, player.getGameProfile().name().toUpperCase(), ItemStack.EMPTY, 0, false);
        helper.assertTrue(sensor.matches(player), "a player filter is not case sensitive");
        sensor.configure(GpsSensorMode.PLAYERS, "someone-else", ItemStack.EMPTY, 0, false);
        helper.assertFalse(sensor.matches(player), "a player filter saw someone else");

        sensor.configure(GpsSensorMode.ITEMS, "", new ItemStack(Items.APPLE, 12), 0, false);
        helper.assertTrue(sensor.matches(apple) && !sensor.matches(pig), "an apple filter sees dropped apples");
        helper.assertValueEqual(sensor.getFilterItem().getCount(), 1, "the filter item is kept as a single");
        sensor.configure(GpsSensorMode.ITEMS, "", new ItemStack(Items.BREAD), 0, false);
        helper.assertFalse(sensor.matches(apple), "a bread filter saw an apple");

        helper.succeed();
    }

    /**
     * The upgraded sensor filters by a list of entries, where a # entry is an entity or item tag.
     * Entries are added and removed through the menu, which refuses one that could never match, and
     * each mode keeps its own list. In item mode the slot adds to the list rather than holding an
     * item. A plain sensor given a tag sees nothing.
     */
    private static void upgradedGpsSensorTakesListsAndTags(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), TechBlocks.GPS_SENSOR.get());
        helper.setBlock(new BlockPos(3, 1, 1), TechBlocks.UPGRADED_GPS_SENSOR.get());
        GpsSensorBlockEntity plain = helper.getBlockEntity(new BlockPos(1, 1, 1), GpsSensorBlockEntity.class);
        GpsSensorBlockEntity upgraded = helper.getBlockEntity(new BlockPos(3, 1, 1), GpsSensorBlockEntity.class);
        Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(4, 1, 4));
        Cow cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(6, 1, 4));
        Zombie zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(2, 1, 4));
        ServerPlayer player = survivalPlayer(helper);
        stand(helper, player, new BlockPos(3, 1, 3));
        ItemEntity log = helper.spawnItem(Items.OAK_LOG, new BlockPos(2, 1, 6));
        ItemEntity apple = helper.spawnItem(Items.APPLE, new BlockPos(4, 1, 6));
        GpsSensorMenu menu = openMenu(player, upgraded);

        upgraded.configure(GpsSensorMode.MOBS, "", ItemStack.EMPTY, 0, false);
        GpsSensorFilterPacket.handle(new GpsSensorFilterPacket("pig", true), player);
        GpsSensorFilterPacket.handle(new GpsSensorFilterPacket("minecraft:cow", true), player);
        GpsSensorFilterPacket.handle(new GpsSensorFilterPacket("PIG", true), player);
        GpsSensorFilterPacket.handle(new GpsSensorFilterPacket("minecraft:no_such_mob", true), player);
        helper.assertValueEqual(upgraded.getEntries(), List.of("minecraft:pig", "minecraft:cow"), "the entries after adding a duplicate and an unknown mob");
        helper.assertTrue(upgraded.matches(pig) && upgraded.matches(cow) && !upgraded.matches(zombie), "a pig and cow list sees both and nothing else");

        menu.clickMenuButton(player, GpsSensorMenu.REMOVE_ENTRY_BUTTON);
        helper.assertValueEqual(upgraded.getEntries(), List.of("minecraft:cow"), "the entries after removing the first");
        helper.assertFalse(menu.clickMenuButton(player, GpsSensorMenu.REMOVE_ENTRY_BUTTON + 5), "removing an entry that is not there");

        upgraded.addEntry("#minecraft:undead");
        helper.assertTrue(upgraded.matches(zombie) && upgraded.matches(cow) && !upgraded.matches(pig), "an undead tag and a cow see both");
        helper.assertFalse(upgraded.addEntry("#minecraft:no_such_tag"), "an unknown tag was added");

        upgraded.configure(GpsSensorMode.PLAYERS, "", ItemStack.EMPTY, 0, false);
        helper.assertTrue(upgraded.getEntries().isEmpty(), "the players list started with the mobs list's entries");
        upgraded.addEntry("someone-else");
        helper.assertFalse(upgraded.matches(player), "a list without the player saw them");
        upgraded.addEntry(player.getGameProfile().name());
        helper.assertTrue(upgraded.matches(player), "a list of names missed a player on it");

        upgraded.configure(GpsSensorMode.ITEMS, "", ItemStack.EMPTY, 0, false);
        upgraded.addEntry("#minecraft:logs");
        helper.assertTrue(upgraded.matches(log) && !upgraded.matches(apple), "a logs tag sees the log only");
        menu.setCarried(new ItemStack(Items.APPLE, 3));
        menu.clicked(GpsSensorMenu.FILTER_SLOT, 0, ContainerInput.PICKUP, player);
        helper.assertValueEqual(upgraded.getEntries(), List.of("#minecraft:logs", "minecraft:apple"), "the item entries after clicking apples onto the slot");
        helper.assertTrue(upgraded.getFilterItem().isEmpty() && !menu.getSlot(GpsSensorMenu.FILTER_SLOT).hasItem(), "the upgraded sensor's slot kept the apple");
        helper.assertTrue(upgraded.matches(log) && upgraded.matches(apple), "a tag and a clicked item see either one");
        menu.setCarried(ItemStack.EMPTY);

        menu.clickMenuButton(player, GpsSensorMenu.MODE_BUTTON);
        menu.clickMenuButton(player, GpsSensorMenu.MODE_BUTTON);
        helper.assertValueEqual(upgraded.getMode(), GpsSensorMode.MOBS, "the mode after cycling round from items");
        helper.assertValueEqual(upgraded.getEntries(), List.of("minecraft:cow", "#minecraft:undead"), "the mobs list after switching away and back");

        GpsSensorMenu plainMenu = openMenu(player, plain);
        plain.configure(GpsSensorMode.PLAYERS, "someone", ItemStack.EMPTY, 0, false);
        plainMenu.clickMenuButton(player, GpsSensorMenu.MODE_BUTTON);
        plainMenu.setFilterText("minecraft:pig");
        plainMenu.clickMenuButton(player, GpsSensorMenu.MODE_BUTTON);
        plainMenu.clickMenuButton(player, GpsSensorMenu.MODE_BUTTON);
        helper.assertValueEqual(plain.getFilter(), "someone", "the plain sensor's player filter after switching away and back");
        helper.assertValueEqual(plain.getFilter(GpsSensorMode.MOBS), "minecraft:pig", "the plain sensor's mob filter, kept while in another mode");

        helper.assertFalse(plain.addEntry("pig"), "a plain sensor took a list entry");
        plain.configure(GpsSensorMode.MOBS, "#minecraft:undead", ItemStack.EMPTY, 0, false);
        helper.assertFalse(plain.matches(zombie), "a plain sensor used a tag");

        helper.assertTrue(GpsSensorFilter.problem(GpsSensorMode.MOBS, "#minecraft:undead", true) == null, "a good tag was flagged");
        helper.assertTrue(GpsSensorFilter.problem(GpsSensorMode.MOBS, "#minecraft:undead", false) != null, "a tag on a plain sensor was not flagged");
        helper.assertTrue(GpsSensorFilter.problem(GpsSensorMode.PLAYERS, "#minecraft:undead", true) != null, "a player tag was not flagged");

        helper.succeed();
    }

    /**
     * The radius only reaches past its block on the upgraded sensor, and typed filter text is
     * refused once the player is out of the menu's reach.
     */
    private static void gpsSensorSettingsAreBounded(GameTestHelper helper) {
        final BlockPos plainPos = new BlockPos(1, 1, 1);
        final BlockPos upgradedPos = new BlockPos(3, 1, 1);
        helper.setBlock(plainPos, TechBlocks.GPS_SENSOR.get());
        helper.setBlock(upgradedPos, TechBlocks.UPGRADED_GPS_SENSOR.get());
        GpsSensorBlockEntity plain = helper.getBlockEntity(plainPos, GpsSensorBlockEntity.class);
        GpsSensorBlockEntity upgraded = helper.getBlockEntity(upgradedPos, GpsSensorBlockEntity.class);

        plain.configure(GpsSensorMode.MOBS, "", ItemStack.EMPTY, 3, false);
        helper.assertValueEqual(plain.getRadius(), 0, "a plain GPS sensor's radius");
        upgraded.configure(GpsSensorMode.MOBS, "", ItemStack.EMPTY, 100, false);
        helper.assertValueEqual(upgraded.getRadius(), upgraded.getMaxRadius(), "an upgraded GPS sensor's radius, clamped");
        helper.assertTrue(upgraded.getRange() > plain.getRange(), "the upgraded sensor does not reach further");

        ServerPlayer player = survivalPlayer(helper);
        stand(helper, player, new BlockPos(2, 1, 3));
        openMenu(player, plain);
        GpsSensorFilterPacket.handle(new GpsSensorFilterPacket("someone", false), player);
        helper.assertValueEqual(plain.getFilter(), "someone", "the filter typed by a nearby player");

        player.teleportTo(player.getX() + 40.0D, player.getY(), player.getZ());
        GpsSensorFilterPacket.handle(new GpsSensorFilterPacket("someone-else", false), player);
        helper.assertValueEqual(plain.getFilter(), "someone", "the filter after text from a player 40 blocks away");

        helper.succeed();
    }

    /**
     * The item filter is a ghost slot: clicking it with a stack, or shift clicking one in the
     * inventory, copies a single into the sensor and moves nothing; an empty click clears it.
     */
    private static void gpsSensorMenuTakesAGhostItem(GameTestHelper helper) {
        final BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, TechBlocks.GPS_SENSOR.get());
        GpsSensorBlockEntity sensor = helper.getBlockEntity(pos, GpsSensorBlockEntity.class);
        ServerPlayer player = survivalPlayer(helper, new ItemStack(Items.EMERALD, 5));
        stand(helper, player, new BlockPos(2, 1, 3));
        GpsSensorMenu menu = openMenu(player, sensor);

        // The client's menu is built from the position alone.
        RegistryFriendlyByteBuf buf = RegistryFriendlyByteBuf.decorator(helper.getLevel().registryAccess()).apply(Unpooled.buffer());
        MenuData.write(menu.getType(), sensor.getMenuData(player), buf);
        AbstractContainerMenu client = MenuData.read(menu.getType(), 1, player.getInventory(), buf);
        helper.assertTrue(client instanceof GpsSensorMenu clientMenu && clientMenu.getSensor() == sensor, "the client menu did not find the sensor");
        helper.assertValueEqual(client.slots.size(), menu.slots.size(), "client menu slots");

        menu.clickMenuButton(player, GpsSensorMenu.MODE_BUTTON);
        menu.clickMenuButton(player, GpsSensorMenu.MODE_BUTTON);
        helper.assertValueEqual(sensor.getMode(), GpsSensorMode.ITEMS, "the mode after two mode clicks");

        menu.setCarried(new ItemStack(Items.APPLE, 7));
        menu.clicked(GpsSensorMenu.FILTER_SLOT, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(sensor.getFilterItem().is(Items.APPLE) && sensor.getFilterItem().getCount() == 1, "clicking the slot with apples did not make them the filter");
        helper.assertValueEqual(menu.getCarried().getCount(), 7, "the carried apples after setting the filter");

        // The player's hotbar is the last nine slots, and the emeralds sit in its first.
        int handSlot = menu.slots.size() - 9;
        menu.quickMoveStack(player, handSlot);
        helper.assertTrue(sensor.getFilterItem().is(Items.EMERALD), "shift clicking the held emeralds did not make them the filter");
        helper.assertValueEqual(player.getMainHandItem().getCount(), 5, "the held emeralds after shift clicking them");

        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(GpsSensorMenu.FILTER_SLOT, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(sensor.getFilterItem().isEmpty(), "an empty handed click did not clear the filter");

        helper.succeed();
    }

    /**
     * Built straight from the provider: the test player's connection cannot take NeoForge's
     * open screen payload, as in Storage's menu tests.
     */
    private static GpsSensorMenu openMenu(ServerPlayer player, GpsSensorBlockEntity sensor) {
        GpsSensorMenu menu = (GpsSensorMenu) sensor.createMenu(1, player.getInventory(), player);
        player.containerMenu = menu;
        return menu;
    }

    private static ItemStack gps(ResourceKey<Level> dimension, BlockPos pos) {
        ItemStack stack = new ItemStack(TechItems.GPS.get());
        stack.set(TechDataComponents.GPS_TARGET.get(), new GpsTarget(GlobalPos.of(dimension, pos)));
        return stack;
    }
}
