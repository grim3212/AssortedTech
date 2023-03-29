package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.annotations.LoaderImplement;
import com.grim3212.assorted.lib.mixin.data.AccessorBlockLootSubProvider;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TechBlockLoot extends VanillaBlockLoot {

    private final List<Block> blocks = new ArrayList<>();

    public TechBlockLoot() {
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
    protected void generate() {
        this.dropOther(TechBlocks.FLIP_FLOP_WALL_TORCH.get(), TechBlocks.FLIP_FLOP_TORCH.get());
        this.dropOther(TechBlocks.GLOWSTONE_WALL_TORCH.get(), TechBlocks.GLOWSTONE_TORCH.get());
        for (Block b : this.blocks) {
            this.dropSelf(b);
        }
    }

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        this.generate();
        Set<ResourceLocation> set = new HashSet<>();
        AccessorBlockLootSubProvider provider = ((AccessorBlockLootSubProvider) this);

        for (Block block : getKnownBlocks()) {
            if (block.isEnabled(provider.assortedlib_getEnabledFeatures())) {
                ResourceLocation resourcelocation = block.getLootTable();
                if (resourcelocation != BuiltInLootTables.EMPTY && set.add(resourcelocation)) {
                    LootTable.Builder loottable$builder = provider.assortedlib_getMap().remove(resourcelocation);
                    if (loottable$builder == null) {
                        throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", resourcelocation, BuiltInRegistries.BLOCK.getKey(block)));
                    }

                    biConsumer.accept(resourcelocation, loottable$builder);
                }
            }
        }

        if (!provider.assortedlib_getMap().isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + provider.assortedlib_getMap().keySet());
        }
    }

    @LoaderImplement(loader = LoaderImplement.Loader.FORGE, value = "BlockLootSubProvider")
    protected Iterable<Block> getKnownBlocks() {
        return TechBlocks.BLOCKS.getEntries().stream().map(Supplier::get).collect(Collectors.toList());
    }

}
