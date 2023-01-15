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
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class TechBlockTagProvider extends BlockTagsProvider {

	public TechBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, AssortedTech.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider provider) {
		this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TechBlocks.FAN.get(), TechBlocks.ALARM.get());

		for (RegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
			this.tag(TechTags.Blocks.SPIKES).add(b.get());
			this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get());
		}

		for (RegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
			this.tag(TechTags.Blocks.SENSORS).add(b.get());
			this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get());
		}

		this.tag(TechTags.Blocks.LASER_BREAKABLES).add(Blocks.WATER, Blocks.LAVA, Blocks.ICE, Blocks.SUGAR_CANE, Blocks.SNOW, Blocks.POWDER_SNOW, Blocks.WHEAT, Blocks.POTATOES, Blocks.CARROTS, Blocks.BEETROOTS);

		this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TechBlocks.ATTRACTOR.get(), TechBlocks.GRAVITOR.get(), TechBlocks.REPULSOR.get(), TechBlocks.ATTRACTOR_DIRECTIONAL.get(), TechBlocks.REPULSOR_DIRECTIONAL.get(), TechBlocks.GRAVITOR_DIRECTIONAL.get());
		this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(TechBlocks.BRIDGE_CONTROL_ACCEL.get(), TechBlocks.BRIDGE_CONTROL_DEATH.get(), TechBlocks.BRIDGE_CONTROL_GRAVITY.get(), TechBlocks.BRIDGE_CONTROL_LASER.get(), TechBlocks.BRIDGE_CONTROL_TRICK.get());
	}

	@Override
	public String getName() {
		return "Assorted Tech block tags";
	}
}
