package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.lib.data.LibManualProvider;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Stream;

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
        this.section(10, TechBlocks.BRIDGE_CONTROL_LASER.get());

        this.addBridges();
        this.addGravity();
        this.addSensors();
        this.addSpikes();
        this.addRedstone();
    }

    private void addBridges() {
        ChapterBuilder bridges = this.chapter("bridges");

        // The beam itself is placed by the control, so it reads as the plain laser bridge.
        bridges.recipes("laser", "bridge_control_laser")
                .opens(TechBlocks.BRIDGE_CONTROL_LASER.get(), TechBlocks.BRIDGE.get());
        bridges.recipes("trick", "bridge_control_trick").opens(TechBlocks.BRIDGE_CONTROL_TRICK.get());
        bridges.recipes("accel", "bridge_control_accel").opens(TechBlocks.BRIDGE_CONTROL_ACCEL.get());
        bridges.recipes("death", "bridge_control_death").opens(TechBlocks.BRIDGE_CONTROL_DEATH.get());
        bridges.recipes("gravity", "bridge_control_gravity").opens(TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
    }

    private void addGravity() {
        ChapterBuilder gravity = this.chapter("gravity");

        gravity.recipes("attractors", "attractor", "attractor_directional").every(60)
                .opens(TechBlocks.ATTRACTOR.get(), TechBlocks.ATTRACTOR_DIRECTIONAL.get());
        gravity.recipes("repulsors", "repulsor", "repulsor_directional").every(60)
                .opens(TechBlocks.REPULSOR.get(), TechBlocks.REPULSOR_DIRECTIONAL.get());
        gravity.recipes("gravitors", "gravitor", "gravitor_directional").every(60)
                .opens(TechBlocks.GRAVITOR.get(), TechBlocks.GRAVITOR_DIRECTIONAL.get());
        gravity.recipes("boots", "gravity_boots").opens(TechItems.GRAVITY_BOOTS.get());
    }

    private void addSensors() {
        String[] recipes = Stream.of(SensorType.values()).map(type -> type + "_sensor").toArray(String[]::new);

        ChapterBuilder sensors = this.chapter("sensors");
        sensors.recipes("sensors", recipes).every(60).opens(blocks(TechBlocks.SENSORS));
        sensors.text("triggers");
    }

    private void addSpikes() {
        String[] recipes = Stream.of(SpikeType.values()).map(type -> type + "_spike").toArray(String[]::new);

        ChapterBuilder spikes = this.chapter("spikes");
        spikes.recipes("spikes", recipes).every(50).opens(blocks(TechBlocks.SPIKES));
        spikes.text("materials");
    }

    private void addRedstone() {
        ChapterBuilder redstone = this.chapter("redstone");

        redstone.recipes("fan", "fan").opens(TechBlocks.FAN.get());
        redstone.recipes("alarm", "alarm").opens(TechBlocks.ALARM.get());
        redstone.recipes("glowstone_torch", "glowstone_torch")
                .opens(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get());
        redstone.recipes("flip_flop_torch", "flip_flop_torch")
                .opens(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get());
    }

    private static Block[] blocks(List<? extends IRegistryObject<? extends Block>> registered) {
        return registered.stream().map(IRegistryObject::get).toArray(Block[]::new);
    }
}
