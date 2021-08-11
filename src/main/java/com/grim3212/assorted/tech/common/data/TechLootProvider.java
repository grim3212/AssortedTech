package com.grim3212.assorted.tech.common.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grim3212.assorted.tech.common.block.TechBlocks;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.util.ResourceLocation;

public class TechLootProvider implements IDataProvider {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	private List<Block> blocks = Lists.newArrayList();

	public TechLootProvider(DataGenerator generator) {
		this.generator = generator;

		this.blocks.add(TechBlocks.FLIP_FLOP_TORCH.get());
		this.blocks.add(TechBlocks.GLOWSTONE_TORCH.get());
		this.blocks.add(TechBlocks.FAN.get());

		TechBlocks.SPIKES.forEach((spike) -> this.blocks.add(spike.get()));
		TechBlocks.SENSORS.forEach((sensor) -> this.blocks.add(sensor.get()));
	}

	@Override
	public void run(DirectoryCache cache) throws IOException {
		Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

		tables.put(TechBlocks.FLIP_FLOP_WALL_TORCH.getId(), genRegular(TechBlocks.FLIP_FLOP_TORCH.get()));
		tables.put(TechBlocks.GLOWSTONE_WALL_TORCH.getId(), genRegular(TechBlocks.GLOWSTONE_TORCH.get()));

		for (Block b : this.blocks) {
			tables.put(b.getRegistryName(), genRegular(b));
		}

		for (Map.Entry<ResourceLocation, LootTable.Builder> e : tables.entrySet()) {
			Path path = getPath(generator.getOutputFolder(), e.getKey());
			IDataProvider.save(GSON, cache, LootTableManager.serialize(e.getValue().setParamSet(LootParameterSets.BLOCK).build()), path);
		}
	}

	private static Path getPath(Path root, ResourceLocation id) {
		return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
	}

	private static LootTable.Builder genRegular(Block b) {
		LootEntry.Builder<?> entry = ItemLootEntry.lootTableItem(b);
		LootPool.Builder pool = LootPool.lootPool().name("main").setRolls(ConstantRange.exactly(1)).add(entry).when(SurvivesExplosion.survivesExplosion());
		return LootTable.lootTable().withPool(pool);
	}

	@Override
	public String getName() {
		return "Assorted Tech loot tables";
	}

}
