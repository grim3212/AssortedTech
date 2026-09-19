package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.lib.data.LibManualProvider;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.entity.TechEntities;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.crafting.TechConditions;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * This mod's section of the instruction manual. The spike and sensor families are read from the
 * same lists the blocks are registered from, so a new material lands on its page on its own.
 */
public class TechManualProvider extends LibManualProvider {

    public TechManualProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    @Override
    protected void addChapters() {
        this.section(100, TechBlocks.BRIDGE_CONTROL_LASER.get());

        this.addBridges();
        this.addGravity();
        this.addSensors();
        this.addSpikes();
        this.addRedstone();
        this.addExtruder();
    }

    private void addExtruder() {
        Item[] all = TechItems.EXTRUDERS.values().stream().map(IRegistryObject::get).toArray(Item[]::new);

        ChapterBuilder extruder = this.chapter("extruder").whenPartEnabled(TechConditions.Parts.EXTRUDER);
        extruder.recipes("extruder", all).every(50).opens(all).opens(TechEntities.EXTRUDER.get());
        extruder.text("materials");
    }

    private void addBridges() {
        ChapterBuilder bridges = this.chapter("bridges").whenPartEnabled(TechConditions.Parts.BRIDGES);

        // The beam itself is placed by the control, so it reads as the plain laser bridge.
        bridges.recipes("laser", TechBlocks.BRIDGE_CONTROL_LASER.get())
                .opens(TechBlocks.BRIDGE_CONTROL_LASER.get(), TechBlocks.BRIDGE.get());
        bridges.recipes("trick", TechBlocks.BRIDGE_CONTROL_TRICK.get()).opens(TechBlocks.BRIDGE_CONTROL_TRICK.get());
        bridges.recipes("accel", TechBlocks.BRIDGE_CONTROL_ACCEL.get()).opens(TechBlocks.BRIDGE_CONTROL_ACCEL.get());
        bridges.recipes("death", TechBlocks.BRIDGE_CONTROL_DEATH.get()).opens(TechBlocks.BRIDGE_CONTROL_DEATH.get());
        bridges.recipes("gravity", TechBlocks.BRIDGE_CONTROL_GRAVITY.get()).opens(TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
    }

    private void addGravity() {
        ChapterBuilder gravity = this.chapter("gravity");

        gravity.recipes("attractors", TechBlocks.ATTRACTOR.get(), TechBlocks.ATTRACTOR_DIRECTIONAL.get()).whenPartEnabled(TechConditions.Parts.GRAVITY).every(60)
                .opens(TechBlocks.ATTRACTOR.get(), TechBlocks.ATTRACTOR_DIRECTIONAL.get());
        gravity.recipes("repulsors", TechBlocks.REPULSOR.get(), TechBlocks.REPULSOR_DIRECTIONAL.get()).whenPartEnabled(TechConditions.Parts.GRAVITY).every(60)
                .opens(TechBlocks.REPULSOR.get(), TechBlocks.REPULSOR_DIRECTIONAL.get());
        gravity.recipes("gravitors", TechBlocks.GRAVITOR.get(), TechBlocks.GRAVITOR_DIRECTIONAL.get()).whenPartEnabled(TechConditions.Parts.GRAVITY).every(60)
                .opens(TechBlocks.GRAVITOR.get(), TechBlocks.GRAVITOR_DIRECTIONAL.get());
        gravity.recipes("boots", TechItems.GRAVITY_BOOTS.get()).opens(TechItems.GRAVITY_BOOTS.get());
    }

    private void addSensors() {
        Block[] all = blocks(TechBlocks.SENSORS);

        ChapterBuilder sensors = this.chapter("sensors").whenPartEnabled(TechConditions.Parts.SENSORS);
        sensors.recipes("sensors", all).every(60).opens(all);
        sensors.text("triggers");
        sensors.recipes("gps", TechItems.GPS.get()).whenPartEnabled(TechConditions.Parts.GPS).opens(TechItems.GPS.get());
        sensors.recipes("gps_sensor", TechBlocks.GPS_SENSOR.get()).whenPartEnabled(TechConditions.Parts.GPS).opens(TechBlocks.GPS_SENSOR.get());
        sensors.recipes("upgraded_gps_sensor", TechBlocks.UPGRADED_GPS_SENSOR.get()).whenPartEnabled(TechConditions.Parts.GPS).opens(TechBlocks.UPGRADED_GPS_SENSOR.get());
    }

    private void addSpikes() {
        Block[] all = blocks(TechBlocks.SPIKES);

        ChapterBuilder spikes = this.chapter("spikes").whenPartEnabled(TechConditions.Parts.SPIKES);
        spikes.recipes("spikes", all).every(50).opens(all);
        spikes.text("materials");
    }

    private void addRedstone() {
        ChapterBuilder redstone = this.chapter("redstone");

        redstone.recipes("fan", TechBlocks.FAN.get()).whenPartEnabled(TechConditions.Parts.FAN).opens(TechBlocks.FAN.get());
        redstone.recipes("alarm", TechBlocks.ALARM.get()).whenPartEnabled(TechConditions.Parts.ALARM).opens(TechBlocks.ALARM.get());
        redstone.recipes("glowstone_torch", TechBlocks.GLOWSTONE_TORCH.get()).whenPartEnabled(TechConditions.Parts.TORCHES)
                .opens(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get());
        redstone.recipes("flip_flop_torch", TechBlocks.FLIP_FLOP_TORCH.get()).whenPartEnabled(TechConditions.Parts.TORCHES)
                .opens(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get());
    }

    private static Block[] blocks(List<? extends IRegistryObject<? extends Block>> registered) {
        return registered.stream().map(IRegistryObject::get).toArray(Block[]::new);
    }
}
