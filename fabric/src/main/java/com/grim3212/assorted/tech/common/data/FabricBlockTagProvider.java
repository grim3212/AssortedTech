package com.grim3212.assorted.tech.common.data;

import com.grim3212.assorted.tech.data.TechBlockTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VanillaBlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class FabricBlockTagProvider extends VanillaBlockTagsProvider {

    private final TechBlockTagProvider commonBlocks;

    public FabricBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
        this.commonBlocks = new TechBlockTagProvider(output, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        this.commonBlocks.addCommonTags(this::tag);
    }
}
