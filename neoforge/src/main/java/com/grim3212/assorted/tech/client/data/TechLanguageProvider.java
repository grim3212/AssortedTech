package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.lib.data.LibLanguageProvider;
import com.grim3212.assorted.tech.Constants;
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

        this.add("tag.item.assortedtech.sensors", "Sensors");
        this.add("tag.item.assortedtech.spikes", "Spikes");

        // Families whose names read differently from their ids.
        this.nameBlocks("(.+)_directional", m -> "Directional " + titleCase(m.group(1)));
        this.nameBlocks("(.+)_spike", m -> titleCase(m.group(1)) + " Spikes");
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
