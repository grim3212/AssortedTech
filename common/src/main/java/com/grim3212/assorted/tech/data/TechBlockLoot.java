package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.data.LibBlockLootProvider;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TechBlockLoot extends LibBlockLootProvider {

    private final List<Block> blocks = new ArrayList<>();

    public TechBlockLoot() {
        super(() -> TechBlocks.BLOCKS.getEntries().stream().map(Supplier::get).collect(Collectors.toList()));
        this.blocks.add(TechBlocks.FLIP_FLOP_TORCH.get());
        this.blocks.add(TechBlocks.GLOWSTONE_TORCH.get());
        this.blocks.add(TechBlocks.FAN.get());
        this.blocks.add(TechBlocks.ALARM.get());
        this.blocks.add(TechBlocks.BRIDGE_CONTROL_ACCEL.get());
        this.blocks.add(TechBlocks.BRIDGE_CONTROL_DEATH.get());
        this.blocks.add(TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
        this.blocks.add(TechBlocks.BRIDGE_CONTROL_LASER.get());
        this.blocks.add(TechBlocks.BRIDGE_CONTROL_TRICK.get());

        this.blocks.add(TechBlocks.ATTRACTOR.get());
        this.blocks.add(TechBlocks.REPULSOR.get());
        this.blocks.add(TechBlocks.GRAVITOR.get());
        this.blocks.add(TechBlocks.ATTRACTOR_DIRECTIONAL.get());
        this.blocks.add(TechBlocks.REPULSOR_DIRECTIONAL.get());
        this.blocks.add(TechBlocks.GRAVITOR_DIRECTIONAL.get());

        TechBlocks.SPIKES.forEach((spike) -> this.blocks.add(spike.get()));
        TechBlocks.SENSORS.forEach((sensor) -> this.blocks.add(sensor.get()));
    }

    @Override
    public void generate() {
        this.dropOther(TechBlocks.FLIP_FLOP_WALL_TORCH.get(), TechBlocks.FLIP_FLOP_TORCH.get());
        this.dropOther(TechBlocks.GLOWSTONE_WALL_TORCH.get(), TechBlocks.GLOWSTONE_TORCH.get());
        for (Block b : this.blocks) {
            this.dropSelf(b);
        }
    }

}
