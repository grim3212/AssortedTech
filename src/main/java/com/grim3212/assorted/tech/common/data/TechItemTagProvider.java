package com.grim3212.assorted.tech.common.data;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.util.TechTags;

import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

public class TechItemTagProvider extends ItemTagsProvider {

	public TechItemTagProvider(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, ExistingFileHelper existingFileHelper) {
		super(dataGenerator, blockTagProvider, AssortedTech.MODID, existingFileHelper);
	}

	@Override
	protected void addTags() {
		for (RegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
			this.tag(TechTags.Items.SPIKES).add(b.get().asItem());
		}

		for (RegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
			this.tag(TechTags.Items.SENSORS).add(b.get().asItem());
		}
	}

	@Override
	public String getName() {
		return "Assorted Tech item tags";
	}
}
