package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.api.TechTags;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.NotImplementedException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TechBlockTagProvider extends VanillaBlockTagsProvider {

    public TechBlockTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookup) {
        super(packOutput, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        throw new NotImplementedException();
    }

    @Override
    protected IntrinsicTagAppender<Block> tag(TagKey<Block> tag) {
        throw new NotImplementedException();
    }

    public void addCommonTags(Function<TagKey<Block>, IntrinsicTagAppender<Block>> tagger) {
        tagger.apply(BlockTags.MINEABLE_WITH_PICKAXE).add(TechBlocks.FAN.get(), TechBlocks.ALARM.get());

        for (IRegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
            tagger.apply(TechTags.Blocks.SPIKES).add(b.get());
            tagger.apply(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get());
        }

        for (IRegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
            tagger.apply(TechTags.Blocks.SENSORS).add(b.get());
            tagger.apply(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get());
        }

        tagger.apply(TechTags.Blocks.LASER_BREAKABLES).add(Blocks.WATER, Blocks.LAVA, Blocks.ICE, Blocks.SUGAR_CANE, Blocks.SNOW, Blocks.POWDER_SNOW, Blocks.WHEAT, Blocks.POTATOES, Blocks.CARROTS, Blocks.BEETROOTS);

        tagger.apply(BlockTags.MINEABLE_WITH_PICKAXE).add(TechBlocks.ATTRACTOR.get(), TechBlocks.GRAVITOR.get(), TechBlocks.REPULSOR.get(), TechBlocks.ATTRACTOR_DIRECTIONAL.get(), TechBlocks.REPULSOR_DIRECTIONAL.get(), TechBlocks.GRAVITOR_DIRECTIONAL.get());
        tagger.apply(BlockTags.MINEABLE_WITH_PICKAXE).add(TechBlocks.BRIDGE_CONTROL_ACCEL.get(), TechBlocks.BRIDGE_CONTROL_DEATH.get(), TechBlocks.BRIDGE_CONTROL_GRAVITY.get(), TechBlocks.BRIDGE_CONTROL_LASER.get(), TechBlocks.BRIDGE_CONTROL_TRICK.get());
    }
}
