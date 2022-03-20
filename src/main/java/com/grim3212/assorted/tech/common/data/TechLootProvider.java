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

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class TechLootProvider implements DataProvider {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	private List<Block> blocks = Lists.newArrayList();

	public TechLootProvider(DataGenerator generator) {
		this.generator = generator;

		this.blocks.add(TechBlocks.FLIP_FLOP_TORCH.get());
		this.blocks.add(TechBlocks.GLOWSTONE_TORCH.get());
		this.blocks.add(TechBlocks.FAN.get());
		this.blocks.add(TechBlocks.ALARM.get());
		this.blocks.add(TechBlocks.BRIDGE_CONTROL_ACCEL.get());
		this.blocks.add(TechBlocks.BRIDGE_CONTROL_DEATH.get());
		this.blocks.add(TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
		this.blocks.add(TechBlocks.BRIDGE_CONTROL_LASER.get());
		this.blocks.add(TechBlocks.BRIDGE_CONTROL_TRICK.get());

		TechBlocks.SPIKES.forEach((spike) -> this.blocks.add(spike.get()));
		TechBlocks.SENSORS.forEach((sensor) -> this.blocks.add(sensor.get()));
	}

	@Override
	public void run(HashCache cache) throws IOException {
		Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

		tables.put(TechBlocks.FLIP_FLOP_WALL_TORCH.getId(), genRegular(TechBlocks.FLIP_FLOP_TORCH.get()));
		tables.put(TechBlocks.GLOWSTONE_WALL_TORCH.getId(), genRegular(TechBlocks.GLOWSTONE_TORCH.get()));

		for (Block b : this.blocks) {
			tables.put(b.getRegistryName(), genRegular(b));
		}

		for (Map.Entry<ResourceLocation, LootTable.Builder> e : tables.entrySet()) {
			Path path = getPath(generator.getOutputFolder(), e.getKey());
			DataProvider.save(GSON, cache, LootTables.serialize(e.getValue().setParamSet(LootContextParamSets.BLOCK).build()), path);
		}
	}

	private static Path getPath(Path root, ResourceLocation id) {
		return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
	}

	private static LootTable.Builder genRegular(Block b) {
		LootPoolEntryContainer.Builder<?> entry = LootItem.lootTableItem(b);
		LootPool.Builder pool = LootPool.lootPool().name("main").setRolls(ConstantValue.exactly(1)).add(entry).when(ExplosionCondition.survivesExplosion());
		return LootTable.lootTable().withPool(pool);
	}

	@Override
	public String getName() {
		return "Assorted Tech loot tables";
	}

}
