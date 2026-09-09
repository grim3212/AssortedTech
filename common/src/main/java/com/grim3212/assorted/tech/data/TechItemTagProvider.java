package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.data.LibItemTagProvider;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.api.TechTags;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
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
    public void addCommonTags(Function<TagKey<Item>, TagAppender<Item>> appender, BiConsumer<TagKey<Block>, TagKey<Item>> copier) {
        // The intrinsic tag appender is gone; TagAppender only accepts ResourceKeys. Wrap it back
        // into something that takes items so the tag lists below stay readable.
        Function<TagKey<Item>, ItemTagger> tagger = (tag) -> new ItemTagger(appender.apply(tag));

        for (IRegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
            tagger.apply(TechTags.Items.SPIKES).add(b.get().asItem());
        }

        for (IRegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
            tagger.apply(TechTags.Items.SENSORS).add(b.get().asItem());
        }
    }

    private record ItemTagger(TagAppender<Item> appender) {

        ItemTagger add(Item... values) {
            for (Item value : values) {
                this.appender.add(BuiltInRegistries.ITEM.getResourceKey(value).orElseThrow());
            }

            return this;
        }

        ItemTagger addTag(TagKey<Item> tag) {
            this.appender.addTag(tag);
            return this;
        }
    }
}
