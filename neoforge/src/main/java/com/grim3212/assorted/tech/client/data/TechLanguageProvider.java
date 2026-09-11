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
    }
}
