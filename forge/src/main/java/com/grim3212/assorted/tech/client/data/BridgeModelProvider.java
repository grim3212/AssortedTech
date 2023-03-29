package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.Constants;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

public class BridgeModelProvider extends ModelProvider<BridgeModelBuilder> {

    final Map<ResourceLocation, BridgeModelBuilder> previousModels = new HashMap<>();

    public BridgeModelProvider(PackOutput output, ExistingFileHelper exHelper) {
        super(output, Constants.MOD_ID, "block", BridgeModelBuilder::new, exHelper);
    }

    @Override
    public String getName() {
        return "Bridge model provider";
    }

    @Override
    protected void registerModels() {
        super.generatedModels.putAll(previousModels);
    }

    public void previousModels() {
        previousModels.putAll(super.generatedModels);
    }
}