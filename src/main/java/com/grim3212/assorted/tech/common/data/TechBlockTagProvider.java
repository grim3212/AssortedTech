package com.grim3212.assorted.tech.common.data;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.util.TechTags;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fmllegacy.RegistryObject;

public class TechBlockTagProvider extends BlockTagsProvider {

	public TechBlockTagProvider(DataGenerator generatorIn, ExistingFileHelper existingFileHelper) {
		super(generatorIn, AssortedTech.MODID, existingFileHelper);
	}

	@Override
	protected void addTags() {
		this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TechBlocks.FAN.get());

		for (RegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
			this.tag(TechTags.Blocks.SPIKES).add(b.get());
			this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get());
		}

		for (RegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
			this.tag(TechTags.Blocks.SENSORS).add(b.get());
			this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get());
		}

	}

	@Override
	public String getName() {
		return "Assorted Tech block tags";
	}
}
