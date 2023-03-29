package com.grim3212.assorted.tech.common.data;

import com.grim3212.assorted.tech.data.TechEntityTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;

import java.util.concurrent.CompletableFuture;

public class FabricEntityTagProvider extends EntityTypeTagsProvider {

    private final TechEntityTagProvider commonEntities;

    public FabricEntityTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
        this.commonEntities = new TechEntityTagProvider(output, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookup) {
        this.commonEntities.addCommonTags(this::tag);
    }
}
