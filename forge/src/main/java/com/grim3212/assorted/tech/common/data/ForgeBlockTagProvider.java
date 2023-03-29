package com.grim3212.assorted.tech.common.data;

import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.data.TechBlockTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ForgeBlockTagProvider extends BlockTagsProvider {

    private final TechBlockTagProvider commonBlocks;

    public ForgeBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Constants.MOD_ID, existingFileHelper);
        this.commonBlocks = new TechBlockTagProvider(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.commonBlocks.addCommonTags(this::tag);
    }

    @Override
    public String getName() {
        return "Assorted Tools block tags";
    }
}
