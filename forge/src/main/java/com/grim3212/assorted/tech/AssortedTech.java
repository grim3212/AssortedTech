package com.grim3212.assorted.tech;

import com.grim3212.assorted.tech.client.TechClient;
import com.grim3212.assorted.tech.client.data.BridgeModelProvider;
import com.grim3212.assorted.tech.client.data.TechBlockstateProvider;
import com.grim3212.assorted.tech.client.data.TechItemModelProvider;
import com.grim3212.assorted.tech.common.data.ForgeBlockTagProvider;
import com.grim3212.assorted.tech.common.data.ForgeEntityTagProvider;
import com.grim3212.assorted.tech.common.data.ForgeItemTagProvider;
import com.grim3212.assorted.tech.common.data.TechRecipes;
import com.grim3212.assorted.tech.data.TechBlockLoot;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod(Constants.MOD_ID)
public class AssortedTech {

    public AssortedTech() {
        // Initialize client side
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> TechClient::init);

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
        ForgeBlockTagProvider blockTagProvider = new ForgeBlockTagProvider(packOutput, lookupProvider, fileHelper);
        datagenerator.addProvider(event.includeServer(), blockTagProvider);
        datagenerator.addProvider(event.includeServer(), new ForgeItemTagProvider(packOutput, lookupProvider, blockTagProvider, fileHelper));
        datagenerator.addProvider(event.includeServer(), new ForgeEntityTagProvider(packOutput, lookupProvider, fileHelper));
        datagenerator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(TechBlockLoot::new, LootContextParamSets.BLOCK))));

        BridgeModelProvider loadedModels = new BridgeModelProvider(packOutput, fileHelper);
        datagenerator.addProvider(event.includeClient(), new TechBlockstateProvider(packOutput, fileHelper, loadedModels));
        datagenerator.addProvider(event.includeClient(), loadedModels);
        datagenerator.addProvider(event.includeClient(), new TechItemModelProvider(packOutput, fileHelper));
    }
}
