package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.data.LibItemTagProvider;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.api.TechTags;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TechItemTagProvider extends LibItemTagProvider {

    public TechItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookup, blockTags);
    }

    @Override
    public void addCommonTags(Function<TagKey<Item>, IntrinsicTagAppender<Item>> tagger, BiConsumer<TagKey<Block>, TagKey<Item>> copier) {
        for (IRegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
            tagger.apply(TechTags.Items.SPIKES).add(b.get().asItem());
        }

        for (IRegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
            tagger.apply(TechTags.Items.SENSORS).add(b.get().asItem());
        }
    }
}
