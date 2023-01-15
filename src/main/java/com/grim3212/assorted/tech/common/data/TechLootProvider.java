package com.grim3212.assorted.tech.common.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.grim3212.assorted.tech.common.block.TechBlocks;

import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class TechLootProvider extends LootTableProvider {

	public TechLootProvider(PackOutput output, Set<ResourceLocation> requiredTables, List<LootTableProvider.SubProviderEntry> subProviders) {
		super(output, requiredTables, subProviders);
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
		map.forEach((location, lootTable) -> {
			LootTables.validate(validationtracker, location, lootTable);
		});
	}

	public static class BlockTables extends VanillaBlockLoot {

		private final List<Block> blocks = new ArrayList<>();

		public BlockTables() {
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
		protected Iterable<Block> getKnownBlocks() {
			return TechBlocks.BLOCKS.getEntries().stream().map(Supplier::get).collect(Collectors.toList());
		}

	}

}
