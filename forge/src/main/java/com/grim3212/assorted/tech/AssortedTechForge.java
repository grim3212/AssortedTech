package com.grim3212.assorted.tech;

import com.grim3212.assorted.lib.data.ForgeBlockTagProvider;
import com.grim3212.assorted.lib.data.ForgeEntityTagProvider;
import com.grim3212.assorted.lib.data.ForgeItemTagProvider;
import com.grim3212.assorted.tech.client.data.BridgeModelProvider;
import com.grim3212.assorted.tech.client.data.TechBlockstateProvider;
import com.grim3212.assorted.tech.client.data.TechItemModelProvider;
import com.grim3212.assorted.tech.data.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod(Constants.MOD_ID)
public class AssortedTechForge {

    public AssortedTechForge() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::gatherData);

        TechCommonMod.init();
    }


    private void gatherData(GatherDataEvent event) {
        DataGenerator datagenerator = event.getGenerator();
        PackOutput packOutput = datagenerator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        datagenerator.addProvider(event.includeServer(), new TechRecipes(packOutput));
        ForgeBlockTagProvider blockTagProvider = new ForgeBlockTagProvider(packOutput, lookupProvider, fileHelper, Constants.MOD_ID, new TechBlockTagProvider(packOutput, lookupProvider));
        datagenerator.addProvider(event.includeServer(), blockTagProvider);
        datagenerator.addProvider(event.includeServer(), new ForgeItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), fileHelper, Constants.MOD_ID, new TechItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter())));
        datagenerator.addProvider(event.includeServer(), new ForgeEntityTagProvider(packOutput, lookupProvider, fileHelper, Constants.MOD_ID, new TechEntityTagProvider(packOutput, lookupProvider)));
        datagenerator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(TechBlockLoot::new, LootContextParamSets.BLOCK))));

        BridgeModelProvider loadedModels = new BridgeModelProvider(packOutput, fileHelper);
        datagenerator.addProvider(event.includeClient(), new TechBlockstateProvider(packOutput, fileHelper, loadedModels));
        datagenerator.addProvider(event.includeClient(), loadedModels);
        datagenerator.addProvider(event.includeClient(), new TechItemModelProvider(packOutput, fileHelper));
    }
}
