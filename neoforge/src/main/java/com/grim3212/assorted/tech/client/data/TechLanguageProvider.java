package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.lib.data.LibLanguageProvider;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.TechTags;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.data.TechItemTagProvider;
import net.minecraft.data.PackOutput;

/**
 * Generates the en_us.json of this mod. A block, item or entity whose name is its id in title case needs
 * no line here (see {@link LibLanguageProvider}); these are the names that read differently, and
 * every key that is not a name.
 */
public class TechLanguageProvider extends LibLanguageProvider {

    /** A blank line between paragraphs; the manual splits its text the way the font does. */
    private static final String BREAK = "\n\n";

    public TechLanguageProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    @Override
    protected void addNames() {
        this.add("itemGroup.assortedtech", "Assorted Tech");

        this.add("death.attack.assortedtech.spike", "%1$s was pierced by spikes");
        this.add("death.attack.assortedtech.spike.player", "%1$s walked into a spike whilst trying to escape %2$s");
        this.add("death.attack.assortedtech.laser", "%1$s played with lasers");
        this.add("death.attack.assortedtech.laser.player", "%1$s got zapped by a laser whilst trying to escape %2$s");

        this.add("assortedtech.subtitle.spike_deploy", "Spike deployed");
        this.add("assortedtech.subtitle.spike_close", "Spike closed");
        this.add("assortedtech.subtitle.alarm", "Alarm going off");

        this.add("tooltip.spike.damage", "Damage: %s");
        this.add("tooltip.sensor.detects.wood", "Detects all entities");
        this.add("tooltip.sensor.detects.stone", "Detects only living mobs");
        this.add("tooltip.sensor.detects.iron", "Detects players");
        this.add("tooltip.sensor.detects.mossy_cobblestone", "Detects only monsters");
        this.add("tooltip.sensor.detects.prismarine", "Detects only water mobs");
        this.add("tooltip.sensor.detects.gold", "Detects only item entities");
        this.add("tooltip.sensor.detects.emerald", "Detects only villager mobs");
        this.add("tooltip.sensor.detects.netherrack", "Detects only nether mobs");
        this.add("tooltip.sensor.detects.cobweb", "Detects only arthropods");
        this.add("tooltip.sensor.detects.end_stone", "Detects only end mobs");
        this.add("tooltip.sensor.detects.hay_bale", "Detects only tameable mobs");
        this.add("tooltip.sensor.detects.feather", "Detects only flying mobs");

        this.add("message.sensor.range", "Sensor range update to: %s");

        this.add("block.assortedtech.flip_flop_wall_torch", "Flip Flop Torch");
        this.add("block.assortedtech.glowstone_wall_torch", "Glowstone Torch");
        this.add("block.assortedtech.alarm", "Alarm Box");
        this.add("block.assortedtech.bridge", "Bridge Piece");
        this.add("block.assortedtech.bridge_control_gravity", "Gravity Lift Control");

        this.add("fan.screen", "Fan");
        this.add("fan.screen.ok", "Ok");
        this.add("fan.screen.cancel", "Cancel");
        this.add("fan.screen.mode", "Mode:");
        this.add("fan.screen.range", "Range:");
        this.add("fan.screen.mode.blow", "Blow");
        this.add("fan.screen.mode.suck", "Suck");
        this.add("fan.screen.mode.off", "Off");
        this.add("fan.screen.max", "Max");
        this.add("fan.screen.min", "Min");
        this.add("fan.screen.add_one", "+1");
        this.add("fan.screen.add_five", "+5");
        this.add("fan.screen.minus_one", "-1");
        this.add("fan.screen.minus_five", "-5");

        this.add("alarm.screen", "Alarm Box");
        this.add("alarm.screen.done", "Done");
        this.add("alarm.screen.description", "Press the button below to cycle through the different types of alarms available. When you've found the one you want, click the 'Done' button at the bottom. Supply a redstone pulse (on/off signal) to any side of the box to sound the alarm.");
        this.add("alarm.screen.name", "Alarm %s");
        this.add("alarm.screen.test", "Test");

        this.add("item.assortedtech.gps", "GPS");
        this.add("block.assortedtech.gps_sensor", "GPS Sensor");
        this.add("block.assortedtech.upgraded_gps_sensor", "Upgraded GPS Sensor");

        this.add("tooltip.gps.usage", "Right click a block to store the space in front of it");
        this.add("tooltip.gps.stored", "Stored: %s, %s, %s");
        this.add("message.gps.stored", "GPS stored %s, %s, %s");
        this.add("message.gps.cleared", "GPS position cleared");

        this.add("gps_sensor.screen.mode", "Detects: %s");
        this.add("gps_sensor.screen.mode.players", "Players");
        this.add("gps_sensor.screen.mode.mobs", "Mobs");
        this.add("gps_sensor.screen.mode.items", "Items");
        this.add("gps_sensor.screen.filter", "Filter");
        this.add("gps_sensor.screen.any_players", "Any player, or type a name");
        this.add("gps_sensor.screen.any_mobs", "Any mob, or an id like zombie");
        this.add("gps_sensor.screen.add_players", "Player name to add");
        this.add("gps_sensor.screen.add_mobs", "Mob id or #tag to add");
        this.add("gps_sensor.screen.add_items", "Item id or #tag");
        this.add("gps_sensor.screen.help.players", "Press + or Enter to add the name to the list. The sensor watches for anyone on it.");
        this.add("gps_sensor.screen.help.mobs", "Press + or Enter to add it to the list. The sensor watches for anything on it. Start with # for an entity tag, like #minecraft:undead.");
        this.add("gps_sensor.screen.help.items", "Press + or Enter to add it to the list, or click an item on the slot instead. The sensor watches for anything on the list. Start with # for an item tag, like #minecraft:logs.");
        this.add("gps_sensor.screen.entries", "Filter: %s/%s");
        this.add("gps_sensor.screen.entries.empty.players", "Nothing added, so any player counts.");
        this.add("gps_sensor.screen.entries.empty.mobs", "Nothing added, so any mob counts.");
        this.add("gps_sensor.screen.entries.empty.items", "Nothing added, so any item counts.");
        this.add("gps_sensor.screen.problem.tag", "Only an upgraded sensor takes #tags");
        this.add("gps_sensor.screen.problem.player_tag", "Players have no tags");
        this.add("gps_sensor.screen.problem.unknown_tag", "No such tag: %s");
        this.add("gps_sensor.screen.problem.unknown", "Nothing is called %s");
        this.add("gps_sensor.screen.problem.duplicate", "Already on the list");
        this.add("gps_sensor.screen.problem.full", "The list is full");
        this.add("gps_sensor.screen.any_item", "Any Item");
        this.add("gps_sensor.screen.item", "Item: %s");
        this.add("gps_sensor.screen.item_hint", "Click an item on the slot");
        this.add("gps_sensor.screen.item_change_hint", "Click another item to swap it, or click empty handed to clear it");
        this.add("gps_sensor.screen.item_add_hint", "Click an item on the slot to add it to the list");
        this.add("gps_sensor.screen.radius", "Radius: %s");
        this.add("gps_sensor.screen.show.on", "Show Watched Area: On");
        this.add("gps_sensor.screen.show.off", "Show Watched Area: Off");
        this.add("gps_sensor.screen.status.none", "No position.");
        this.add("gps_sensor.screen.status.other_dimension", "The GPS's position is in another dimension");
        this.add("gps_sensor.screen.gps_hint", "Put a GPS here to watch the position it stored");
        this.add("gps_sensor.screen.status.good", "Watching %s, %s, %s");
        this.add("gps_sensor.screen.status.blocked", "%s, %s, %s is solid, nothing can be there");
        this.add("gps_sensor.screen.status.out_of_range", "The position is more than %s blocks away");

        this.add("extruder.screen.fuel", "Fuel: %s");
        this.add("extruder.screen.mined", "Mined");
        this.add("tooltip.extruder.level", "Extruder Level %s");
        this.add("message.extruder.started", "Extruder started");
        this.add("message.extruder.no_fuel", "The extruder has no fuel");
        this.add("message.extruder.stopped", "Extruder stopped");
        this.add("extruder.screen.mine_blocks", "Mine: %s");
        this.add("extruder.screen.extrude_blocks", "Extrude: %s");
        this.add("extruder.screen.start", "Start");
        this.add("extruder.screen.stop", "Stop");
        this.add("extruder.screen.direction.down", "Down");
        this.add("extruder.screen.direction.up", "Up");
        this.add("extruder.screen.direction.north", "North");
        this.add("extruder.screen.direction.south", "South");
        this.add("extruder.screen.direction.west", "West");
        this.add("extruder.screen.direction.east", "East");

        this.add("tag.item.assortedtech.sensors", "Sensors");
        this.add("tag.item.assortedtech.spikes", "Spikes");
        for (int level = 0; level < TechTags.Items.EXTRUDER_LEVELS; level++) {
            this.add("tag.item.assortedtech.extruders.level_" + level, "Level " + level + " Extruders");
        }
        // The c: tool tags this mod fills; Assorted Tools names the same ones, and the rest.
        for (ExtruderType type : TechItemTagProvider.vanillaToolMaterials()) {
            for (String kind : new String[]{"pickaxes", "shovels", "axes"}) {
                this.add("tag.item.c." + kind + "." + type, titleCase(type.toString()) + " " + titleCase(kind));
            }
        }

        // Families whose names read differently from their ids.
        this.nameBlocks("(.+)_directional", m -> "Directional " + titleCase(m.group(1)));
        this.nameBlocks("(.+)_spike", m -> titleCase(m.group(1)) + " Spikes");
        this.nameItems("(.+)_extruder", m -> titleCase(m.group(1)) + " Extruder");
        this.nameBlocks("bridge_control_(laser|accel|death|trick)", m -> titleCase(m.group(1)) + " Bridge Control");

        this.addManual();
    }

    /** The chapters in {@code assets/assortedtech/manual} name these keys. */
    private void addManual() {
        this.add("manual.assortedtech.title", "Assorted Tech");
        this.add("manual.assortedtech.description",
                "Laser bridges, gravity, sensors, spikes and additional redstone fun.");

        this.addBridgesChapter();
        this.addGravityChapter();
        this.addSensorsChapter();
        this.addSpikesChapter();
        this.addRedstoneChapter();
        this.addExtruderChapter();
    }

    private void addExtruderChapter() {
        this.add("manual.assortedtech.chapter.extruder", "Extruder");

        this.add("manual.assortedtech.chapter.extruder.extruder.title", "Extruder");
        this.add("manual.assortedtech.chapter.extruder.extruder",
                "Right click it to fuel it, choose its direction and start/stop it. You can also punch it with an empty hand to start or "
                        + "stop it." + BREAK
                        + "It will move in a straight line, mining whatever the material it is made out of can mine, and lays a block from its top slots behind itself at every step.");

        this.add("manual.assortedtech.chapter.extruder.materials.title", "Materials");
        this.add("manual.assortedtech.chapter.extruder.materials",
                "An extruder is made from a pickaxe, shovel and axe of one material, and mines only what "
                        + "those tools can mine. There is one for every material that Assorted Core supports." + BREAK
                        + "Upgraded extruders require an extruder of the previous level surrounded by its tools. "
                        + "Each level is faster, more efficient and has more inventory slots than the previous level.");
    }

    private void addBridgesChapter() {
        this.add("manual.assortedtech.chapter.bridges", "Laser Bridges");

        this.add("manual.assortedtech.chapter.bridges.laser.title", "Laser Bridge");
        this.add("manual.assortedtech.chapter.bridges.laser",
                "A bridge control shoots a beam when it is given redstone, and the beam is solid ground. It "
                        + "cannot be broken while it is lit, and it stops at the first block in the way." + BREAK
                        + "Right click the control with a block to give the beam that block's texture, and shift "
                        + "right click with an empty hand to put it back.");

        this.add("manual.assortedtech.chapter.bridges.trick.title", "Trick Bridge");
        this.add("manual.assortedtech.chapter.bridges.trick",
                "A trick bridge is a laser bridge you fall straight through. It looks solid but definiitely is not.");

        this.add("manual.assortedtech.chapter.bridges.accel.title", "Acceleration Bridge");
        this.add("manual.assortedtech.chapter.bridges.accel",
                "An acceleration bridge carries you along faster than walking. Useful for long crossings.");

        this.add("manual.assortedtech.chapter.bridges.death.title", "Death Bridge");
        this.add("manual.assortedtech.chapter.bridges.death",
                "A death bridge looks like a trick bridge and hurts anything that touches the beam.");

        this.add("manual.assortedtech.chapter.bridges.gravity.title", "Gravity Lift");
        this.add("manual.assortedtech.chapter.bridges.gravity",
                "A gravity lift is a beam you step into rather than onto. The lift will push whatever is inside it the way it points.");
    }

    private void addGravityChapter() {
        this.add("manual.assortedtech.chapter.gravity", "Gravity");

        this.add("manual.assortedtech.chapter.gravity.attractors.title", "Attractors");
        this.add("manual.assortedtech.chapter.gravity.attractors",
                "Powered, an attractor pulls everything within towards it." + BREAK
                        + "The directional version only pulls on the face the cone points out of.");

        this.add("manual.assortedtech.chapter.gravity.repulsors.title", "Repulsors");
        this.add("manual.assortedtech.chapter.gravity.repulsors",
                "A repulsor is an attractor run backwards. When powered, it pushes everything in range away." + BREAK
                        + "The directional version pushes only out of the face it points.");

        this.add("manual.assortedtech.chapter.gravity.gravitors.title", "Gravitors");
        this.add("manual.assortedtech.chapter.gravity.gravitors",
                "A gravitor lifts whatever is near it straight up while it is powered, and lets go the moment "
                        + "it stops. Whatever was in the air then falls the whole way." + BREAK
                        + "The directional version only lifts on the face it points out of.");

        this.add("manual.assortedtech.chapter.gravity.boots.title", "Gravity Boots");
        this.add("manual.assortedtech.chapter.gravity.boots",
                "Wearing gravity boots, none of the blocks in this chapter move you at all.");
    }

    private void addSensorsChapter() {
        this.add("manual.assortedtech.chapter.sensors", "Sensors");

        this.add("manual.assortedtech.chapter.sensors.sensors.title", "Sensors");
        this.add("manual.assortedtech.chapter.sensors.sensors",
                "A sensor watches the space in front of it and gives out redstone while something it cares "
                        + "about is there. A light on the face shows when it has seen one." + BREAK
                        + "Which things it cares about is decided by what it is made of, so the recipe is the "
                        + "setting. The next page lists them.");

        this.add("manual.assortedtech.chapter.sensors.gps.title", "GPS");
        this.add("manual.assortedtech.chapter.sensors.gps",
                "Right click a block with a GPS to store the space in front of it. Sneak and use it in the air to forget it.");

        this.add("manual.assortedtech.chapter.sensors.gps_sensor.title", "GPS Sensor");
        this.add("manual.assortedtech.chapter.sensors.gps_sensor",
                "A GPS sensor watches the position a GPS stored rather than the space in front of it, so it "
                        + "can sit behind a wall or under the floor. Right click it and put the GPS in the slot at "
                        + "the top right." + BREAK
                        + "You can open the GUI and choose between players, mobs and items, and "
                        + "narrow it to one player, one kind of mob or one item.");

        this.add("manual.assortedtech.chapter.sensors.upgraded_gps_sensor.title", "Upgraded GPS Sensor");
        this.add("manual.assortedtech.chapter.sensors.upgraded_gps_sensor",
                "The upgraded GPS sensor can be farther away and supports an adjusteable range." + BREAK
                        + "The upgraded GPS sensor supports a list of 6 entries each as well as supporting entity and item tags now." + BREAK 
                        + "Start an entry with # to use a tag, like #minecraft:undead for "
                        + "every undead mob or #minecraft:logs for every log.");

        this.add("manual.assortedtech.chapter.sensors.triggers.title", "What Each One Watches");
        this.add("manual.assortedtech.chapter.sensors.triggers",
                "Wood sees everything, including dropped items and arrows. Stone sees anything alive. Iron "
                        + "sees only players." + BREAK
                        + "Mossy cobblestone sees hostile mobs, cobweb sees spiders and their kin, netherrack "
                        + "sees what belongs in the Nether, and end stone sees what belongs in the End." + BREAK
                        + "Gold sees dropped items, emerald sees villagers and wandering traders, prismarine "
                        + "sees what swims, feather sees what flies, and a hay bale sees tamed pets.");
    }

    private void addSpikesChapter() {
        this.add("manual.assortedtech.chapter.spikes", "Spikes");

        this.add("manual.assortedtech.chapter.spikes.spikes.title", "Spikes");
        this.add("manual.assortedtech.chapter.spikes.spikes",
                "A spike goes on any face of a block, floor, wall or ceiling, and hurts whatever touches it when powered." + BREAK
                        + "An unpowered spike is flush against the ground and does nothing.");

        this.add("manual.assortedtech.chapter.spikes.materials.title", "Materials");
        this.add("manual.assortedtech.chapter.spikes.materials",
                "What a spike is made of decides how hard it hits. We support many Vanilla materials and every material Assorted Core adds.");
    }

    private void addRedstoneChapter() {
        this.add("manual.assortedtech.chapter.redstone", "Redstone Parts");

        this.add("manual.assortedtech.chapter.redstone.fan.title", "Fan");
        this.add("manual.assortedtech.chapter.redstone.fan",
                "A fan blows entities away from itself or pulls them towards it. Right click it to set the "
                        + "range and which mode is set." + BREAK
                        + "Redstone turns a fan off rather than on, so it runs by default and a lever stops it.");

        this.add("manual.assortedtech.chapter.redstone.alarm.title", "Alarm");
        this.add("manual.assortedtech.chapter.redstone.alarm",
                "An alarm sounds while it has power, and goes on any face of a block. Right click it to pick "
                        + "which of the sounds it makes." + BREAK
                        + "Wired to a sensor it becomes a doorbell, or something considerably less polite.");

        this.add("manual.assortedtech.chapter.redstone.glowstone_torch.title", "Glowstone Torch");
        this.add("manual.assortedtech.chapter.redstone.glowstone_torch",
                "A glowstone torch lights up when it is powered and goes dark when it is not, the way a "
                        + "redstone lamp does, but you can still walk through it like any torch.");

        this.add("manual.assortedtech.chapter.redstone.flip_flop_torch.title", "Flip Flop Torch");
        this.add("manual.assortedtech.chapter.redstone.flip_flop_torch",
                "A flip flop torch changes state each time it is powered and stays there. One button turns it "
                        + "on, the same button turns it off, which saves building the latch yourself.");
    }
}
