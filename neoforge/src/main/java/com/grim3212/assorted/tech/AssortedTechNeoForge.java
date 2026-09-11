package com.grim3212.assorted.tech;

import com.grim3212.assorted.tech.client.data.TechLanguageProvider;
import com.grim3212.assorted.lib.data.ForgeBlockTagProvider;
import com.grim3212.assorted.lib.data.ForgeEntityTagProvider;
import com.grim3212.assorted.lib.data.ForgeItemTagProvider;
import com.grim3212.assorted.tech.client.data.TechBlockstateProvider;
import com.grim3212.assorted.tech.client.data.TechEquipmentAssetProvider;
import com.grim3212.assorted.tech.client.data.TechItemModelProvider;
import com.grim3212.assorted.tech.data.TechBlockLoot;
import com.grim3212.assorted.tech.data.TechBlockTagProvider;
import com.grim3212.assorted.tech.data.TechEntityTagProvider;
import com.grim3212.assorted.tech.data.TechItemTagProvider;
import com.grim3212.assorted.tech.data.TechRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mod(Constants.MOD_ID)
public class AssortedTechNeoForge {

    /**
     * {@code FMLJavaModLoadingContext} is gone; the mod event bus and the mod container are injected
     * into the {@code @Mod} constructor instead.
     */
    public AssortedTechNeoForge(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::gatherServerData);
        modBus.addListener(this::gatherClientData);

        TechCommonMod.init();
    }

    /**
     * {@code ExistingFileHelper} was removed from datagen, the event owns the provider list now
     * ({@code addProvider}), and the include flags are gone because the server and client halves are
     * separate events. Getting the split wrong is quiet: the wrong event runs and reports
     * "All providers took: 0 ms" with a successful build.
     */
    private void gatherServerData(final GatherDataEvent.Server event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Recipe providers are not data providers any more - the Runner owns the output.
        event.addProvider(new TechRecipes.Runner(packOutput, lookupProvider));
        ForgeBlockTagProvider blockTagProvider = event.addProvider(new ForgeBlockTagProvider(packOutput, lookupProvider, Constants.MOD_ID, new TechBlockTagProvider(packOutput, lookupProvider)));
        event.addProvider(new ForgeItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter(), Constants.MOD_ID, new TechItemTagProvider(packOutput, lookupProvider, blockTagProvider.contentsGetter())));
        event.addProvider(new ForgeEntityTagProvider(packOutput, lookupProvider, Constants.MOD_ID, new TechEntityTagProvider(packOutput, lookupProvider)));
        event.addProvider(new LootTableProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(TechBlockLoot::new, LootContextParamSets.BLOCK)), lookupProvider));
    }

    private void gatherClientData(final GatherDataEvent.Client event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();

        // BridgeModelProvider is gone. It only existed because Forge's ModelProvider was generic
        // over a builder type, so a second provider instance was the only way to get at a custom
        // loader builder. A custom loader is an ExtendedModelTemplateBuilder#customLoader call
        // inside whichever provider emits the model now, so the class had nothing left to do.
        event.addProvider(new TechBlockstateProvider(packOutput));
        event.addProvider(new TechItemModelProvider(packOutput));
        event.addProvider(new TechEquipmentAssetProvider(packOutput));
        event.addProvider(new TechLanguageProvider(packOutput));
    }
}
