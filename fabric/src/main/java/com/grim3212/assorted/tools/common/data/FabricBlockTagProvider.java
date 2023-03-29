package com.grim3212.assorted.tools.common.data;

import com.grim3212.assorted.tech.sounds.data.ToolsCommonTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VanillaBlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class FabricBlockTagProvider extends VanillaBlockTagsProvider {

    private final ToolsCommonTagProvider.BlockTagProvider commonBlocks;

    public FabricBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
        this.commonBlocks = new ToolsCommonTagProvider.BlockTagProvider(output, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        this.commonBlocks.addCommonTags(this::tag);
    }
}
