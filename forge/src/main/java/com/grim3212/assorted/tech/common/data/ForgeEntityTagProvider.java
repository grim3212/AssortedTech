package com.grim3212.assorted.tech.common.data;

import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.data.TechEntityTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ForgeEntityTagProvider extends EntityTypeTagsProvider {

    private final TechEntityTagProvider commonEntityTags;

    public ForgeEntityTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFileHelper) {
        super(output, lookup, Constants.MOD_ID, existingFileHelper);
        this.commonEntityTags = new TechEntityTagProvider(output, lookup);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.commonEntityTags.addCommonTags(this::tag);
    }

    @Override
    public String getName() {
        return "Assorted Tools entity type tags";
    }
}
