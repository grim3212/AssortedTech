package com.grim3212.assorted.tech.common.util;

import com.grim3212.assorted.tech.AssortedTech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TechTags {

	public static class Blocks {
		public static final TagKey<Block> SPIKES = techTag("spikes");
		public static final TagKey<Block> SENSORS = techTag("sensors");

		public static TagKey<Block> techTag(String name) {
			return BlockTags.create(new ResourceLocation(AssortedTech.MODID, name));
		}
	}

	public static class Items {
		public static final TagKey<Item> SENSORS = forgeTag("sensors");
		public static final TagKey<Item> SPIKES = forgeTag("spikes");

		public static final TagKey<Item> INGOTS_TIN = forgeTag("ingots/tin");
		public static final TagKey<Item> INGOTS_COPPER = forgeTag("ingots/copper");
		public static final TagKey<Item> INGOTS_SILVER = forgeTag("ingots/silver");
		public static final TagKey<Item> INGOTS_ALUMINUM = forgeTag("ingots/aluminum");
		public static final TagKey<Item> INGOTS_NICKEL = forgeTag("ingots/nickel");
		public static final TagKey<Item> INGOTS_PLATINUM = forgeTag("ingots/platinum");
		public static final TagKey<Item> INGOTS_LEAD = forgeTag("ingots/lead");
		public static final TagKey<Item> INGOTS_BRONZE = forgeTag("ingots/bronze");
		public static final TagKey<Item> INGOTS_ELECTRUM = forgeTag("ingots/electrum");
		public static final TagKey<Item> INGOTS_INVAR = forgeTag("ingots/invar");
		public static final TagKey<Item> INGOTS_STEEL = forgeTag("ingots/steel");
		public static final TagKey<Item> GEMS_RUBY = forgeTag("gems/ruby");
		public static final TagKey<Item> GEMS_AMETHYST = forgeTag("gems/amethyst");
		public static final TagKey<Item> GEMS_SAPPHIRE = forgeTag("gems/sapphire");
		public static final TagKey<Item> GEMS_TOPAZ = forgeTag("gems/topaz");
		public static final TagKey<Item> GEMS_PERIDOT = forgeTag("gems/peridot");

		public static TagKey<Item> forgeTag(String name) {
			return ItemTags.create(new ResourceLocation("forge", name));
		}

		public static TagKey<Item> techTag(String name) {
			return ItemTags.create(new ResourceLocation(AssortedTech.MODID, name));
		}
	}

	public static class Entities {

		public static final TagKey<EntityType<?>> SENSORS_NETHER = techTag("sensors/nether");
		public static final TagKey<EntityType<?>> SENSORS_END = techTag("sensors/end");
		public static final TagKey<EntityType<?>> SENSORS_PETS = techTag("sensors/pets");
		public static final TagKey<EntityType<?>> SENSORS_WATER = techTag("sensors/water");
		public static final TagKey<EntityType<?>> SENSORS_FLYING = techTag("sensors/flying");
		public static final TagKey<EntityType<?>> SENSORS_ARTHROPODS = techTag("sensors/arthropods");

		public static TagKey<EntityType<?>> techTag(String name) {
			return EntityTypeTags.create(new ResourceLocation(AssortedTech.MODID, name).toString());
		}
	}
}
