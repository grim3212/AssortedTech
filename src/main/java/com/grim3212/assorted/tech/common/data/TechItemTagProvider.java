package com.grim3212.assorted.tech.common.data;

import java.util.concurrent.CompletableFuture;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.util.TechTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class TechItemTagProvider extends ItemTagsProvider {

	public TechItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, TagsProvider<Block> blockTags, ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, blockTags, AssortedTech.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider provider) {
		for (RegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
			this.tag(TechTags.Items.SPIKES).add(b.get().asItem());
		}

		for (RegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
			this.tag(TechTags.Items.SENSORS).add(b.get().asItem());
		}

		this.tag(TechTags.Items.FLUID_CONTAINERS).add(Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET);
	}

	@Override
	public String getName() {
		return "Assorted Tech item tags";
	}
}
