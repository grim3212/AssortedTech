package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.data.LibItemTagProvider;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.api.TechTags;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TechItemTagProvider extends LibItemTagProvider {

    public TechItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookup, blockTags);
    }

    @Override
    public void addCommonTags(Function<TagKey<Item>, TagAppender<Item>> appender, BiConsumer<TagKey<Block>, TagKey<Item>> copier) {
        // The intrinsic tag appender is gone; TagAppender only accepts ResourceKeys. Wrap it back
        // into something that takes items so the tag lists below stay readable.
        Function<TagKey<Item>, ItemTagger> tagger = (tag) -> new ItemTagger(appender.apply(tag));

        for (IRegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
            tagger.apply(TechTags.Items.SPIKES).add(b.get().asItem());
        }

        for (IRegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
            tagger.apply(TechTags.Items.SENSORS).add(b.get().asItem());
        }

        // The tools each extruder is made from and mines with are the c:<kind>/<material> tags.
        // Vanilla's are filled here so they never need Assorted Tools; it, or any other mod, fills
        // the rest, and fills vanilla's too, which merges.
        for (ExtruderType type : ExtruderType.values()) {
            tagger.apply(TechTags.Items.extruders(type.getLevel())).add(TechItems.extruder(type));

            Item[] vanilla = VANILLA_TOOLS.get(type);
            if (vanilla != null) {
                tagger.apply(type.getPickaxes()).add(vanilla[0]);
                tagger.apply(type.getShovels()).add(vanilla[1]);
                tagger.apply(type.getAxes()).add(vanilla[2]);
            }
        }
    }

    /** The extruder materials whose tool tags this mod fills, for the language provider to name. */
    public static Set<ExtruderType> vanillaToolMaterials() {
        return VANILLA_TOOLS.keySet();
    }

    private static final Map<ExtruderType, Item[]> VANILLA_TOOLS = Map.of(
            ExtruderType.WOOD, new Item[]{Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_AXE},
            ExtruderType.STONE, new Item[]{Items.STONE_PICKAXE, Items.STONE_SHOVEL, Items.STONE_AXE},
            ExtruderType.COPPER, new Item[]{Items.COPPER_PICKAXE, Items.COPPER_SHOVEL, Items.COPPER_AXE},
            ExtruderType.IRON, new Item[]{Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE},
            ExtruderType.GOLD, new Item[]{Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE},
            ExtruderType.DIAMOND, new Item[]{Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE},
            ExtruderType.NETHERITE, new Item[]{Items.NETHERITE_PICKAXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_AXE});


    private record ItemTagger(TagAppender<Item> appender) {

        ItemTagger add(Item... values) {
            for (Item value : values) {
                this.appender.add(BuiltInRegistries.ITEM.getResourceKey(value).orElseThrow());
            }

            return this;
        }

        ItemTagger addTag(TagKey<Item> tag) {
            this.appender.addTag(tag);
            return this;
        }
    }
}
