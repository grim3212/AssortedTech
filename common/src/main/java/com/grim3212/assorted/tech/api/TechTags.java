package com.grim3212.assorted.tech.api;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TechTags {

    public static class Blocks {

        public static final TagKey<Block> SPIKES = techTag("spikes");
        public static final TagKey<Block> SENSORS = techTag("sensors");

        public static final TagKey<Block> LASER_BREAKABLES = techTag("laser_breakables");

        private static TagKey<Block> techTag(String name) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(Constants.MOD_ID, name));
        }

        private static TagKey<Block> commonTag(String name) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(Services.PLATFORM.getCommonTagPrefix(), name));
        }
    }

    public static class Items {

        public static final TagKey<Item> SENSORS = techTag("sensors");
        public static final TagKey<Item> SPIKES = techTag("spikes");
        public static final TagKey<Item> INGOTS_TIN = commonTag("ingots/tin");
        public static final TagKey<Item> INGOTS_SILVER = commonTag("ingots/silver");
        public static final TagKey<Item> INGOTS_ALUMINUM = commonTag("ingots/aluminum");
        public static final TagKey<Item> INGOTS_NICKEL = commonTag("ingots/nickel");
        public static final TagKey<Item> INGOTS_PLATINUM = commonTag("ingots/platinum");
        public static final TagKey<Item> INGOTS_LEAD = commonTag("ingots/lead");
        public static final TagKey<Item> INGOTS_BRONZE = commonTag("ingots/bronze");
        public static final TagKey<Item> INGOTS_ELECTRUM = commonTag("ingots/electrum");
        public static final TagKey<Item> INGOTS_INVAR = commonTag("ingots/invar");
        public static final TagKey<Item> INGOTS_STEEL = commonTag("ingots/steel");
        public static final TagKey<Item> GEMS_RUBY = commonTag("gems/ruby");
        public static final TagKey<Item> GEMS_SAPPHIRE = commonTag("gems/sapphire");
        public static final TagKey<Item> GEMS_TOPAZ = commonTag("gems/topaz");
        public static final TagKey<Item> GEMS_PERIDOT = commonTag("gems/peridot");

        private static TagKey<Item> techTag(String name) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(Constants.MOD_ID, name));
        }

        private static TagKey<Item> commonTag(String name) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(Services.PLATFORM.getCommonTagPrefix(), name));
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
            return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Constants.MOD_ID, name));
        }
    }
}
