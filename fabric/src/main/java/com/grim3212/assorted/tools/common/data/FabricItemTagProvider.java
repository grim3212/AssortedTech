package com.grim3212.assorted.tools.common.data;

import com.grim3212.assorted.tech.sounds.data.ToolsCommonTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class FabricItemTagProvider extends VanillaItemTagsProvider {

    private final ToolsCommonTagProvider.ItemTagProvider commonItems;

    public FabricItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, TagsProvider<Block> blockTagsProvider) {
        super(output, lookup, blockTagsProvider);
        this.commonItems = new ToolsCommonTagProvider.ItemTagProvider(output, lookup, blockTagsProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        this.commonItems.addCommonTags(this::tag, (pair) -> this.copy(pair.getA(), pair.getB()));
    }
}
