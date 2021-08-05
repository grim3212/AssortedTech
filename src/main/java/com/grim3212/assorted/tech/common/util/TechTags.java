package com.grim3212.assorted.tech.common.util;

import com.grim3212.assorted.tech.AssortedTech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags.IOptionalNamedTag;

public class TechTags {

	public static class Blocks {
		public static final IOptionalNamedTag<Block> SPIKES = techTag("spikes");
		public static final IOptionalNamedTag<Block> SENSORS = techTag("sensors");

		public static IOptionalNamedTag<Block> techTag(String name) {
			return BlockTags.createOptional(new ResourceLocation(AssortedTech.MODID, name));
		}
	}

	public static class Items {
		public static final IOptionalNamedTag<Item> SENSORS = forgeTag("sensors");
		public static final IOptionalNamedTag<Item> SPIKES = forgeTag("spikes");

		public static final IOptionalNamedTag<Item> INGOTS_TIN = forgeTag("ingots/tin");
		public static final IOptionalNamedTag<Item> INGOTS_COPPER = forgeTag("ingots/copper");
		public static final IOptionalNamedTag<Item> INGOTS_SILVER = forgeTag("ingots/silver");
		public static final IOptionalNamedTag<Item> INGOTS_ALUMINUM = forgeTag("ingots/aluminum");
		public static final IOptionalNamedTag<Item> INGOTS_NICKEL = forgeTag("ingots/nickel");
		public static final IOptionalNamedTag<Item> INGOTS_PLATINUM = forgeTag("ingots/platinum");
		public static final IOptionalNamedTag<Item> INGOTS_LEAD = forgeTag("ingots/lead");
		public static final IOptionalNamedTag<Item> INGOTS_BRONZE = forgeTag("ingots/bronze");
		public static final IOptionalNamedTag<Item> INGOTS_ELECTRUM = forgeTag("ingots/electrum");
		public static final IOptionalNamedTag<Item> INGOTS_INVAR = forgeTag("ingots/invar");
		public static final IOptionalNamedTag<Item> INGOTS_STEEL = forgeTag("ingots/steel");
		public static final IOptionalNamedTag<Item> GEMS_RUBY = forgeTag("gems/ruby");
		public static final IOptionalNamedTag<Item> GEMS_AMETHYST = forgeTag("gems/amethyst");
		public static final IOptionalNamedTag<Item> GEMS_SAPPHIRE = forgeTag("gems/sapphire");
		public static final IOptionalNamedTag<Item> GEMS_TOPAZ = forgeTag("gems/topaz");
		public static final IOptionalNamedTag<Item> GEMS_PERIDOT = forgeTag("gems/peridot");

		public static IOptionalNamedTag<Item> forgeTag(String name) {
			return ItemTags.createOptional(new ResourceLocation("forge", name));
		}

		public static IOptionalNamedTag<Item> techTag(String name) {
			return ItemTags.createOptional(new ResourceLocation(AssortedTech.MODID, name));
		}
	}

	public static class Entities {

		public static final IOptionalNamedTag<EntityType<?>> SENSORS_NETHER = techTag("sensors/nether");
		public static final IOptionalNamedTag<EntityType<?>> SENSORS_END = techTag("sensors/end");
		public static final IOptionalNamedTag<EntityType<?>> SENSORS_PETS = techTag("sensors/pets");
		public static final IOptionalNamedTag<EntityType<?>> SENSORS_WATER = techTag("sensors/water");
		public static final IOptionalNamedTag<EntityType<?>> SENSORS_FLYING = techTag("sensors/flying");
		public static final IOptionalNamedTag<EntityType<?>> SENSORS_ARTHROPODS = techTag("sensors/arthropods");

		public static IOptionalNamedTag<EntityType<?>> techTag(String name) {
			return EntityTypeTags.createOptional(new ResourceLocation(AssortedTech.MODID, name));
		}
	}
}
