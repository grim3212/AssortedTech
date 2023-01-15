package com.grim3212.assorted.tech;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.grim3212.assorted.tech.client.data.BridgeModelProvider;
import com.grim3212.assorted.tech.client.data.TechBlockstateProvider;
import com.grim3212.assorted.tech.client.data.TechItemModelProvider;
import com.grim3212.assorted.tech.client.particle.TechParticleTypes;
import com.grim3212.assorted.tech.client.proxy.ClientProxy;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.creative.TechCreativeTab;
import com.grim3212.assorted.tech.common.data.TechBlockTagProvider;
import com.grim3212.assorted.tech.common.data.TechEntityTagProvider;
import com.grim3212.assorted.tech.common.data.TechItemTagProvider;
import com.grim3212.assorted.tech.common.data.TechLootProvider;
import com.grim3212.assorted.tech.common.data.TechLootProvider.BlockTables;
import com.grim3212.assorted.tech.common.data.TechRecipes;
import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.network.PacketHandler;
import com.grim3212.assorted.tech.common.proxy.IProxy;
import com.grim3212.assorted.tech.common.util.TechSounds;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AssortedTech.MODID)
public class AssortedTech {
	public static final String MODID = "assortedtech";
	public static final String MODNAME = "Assorted Tech";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static IProxy proxy = new IProxy() {
	};

	public AssortedTech() {
		DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
		proxy.starting();

		final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		modBus.addListener(this::setup);
		modBus.addListener(this::gatherData);
		modBus.addListener(TechCreativeTab::registerTabs);

		TechBlocks.BLOCKS.register(modBus);
		TechItems.ITEMS.register(modBus);
		TechBlockEntityTypes.BLOCK_ENTITIES.register(modBus);
		TechSounds.SOUNDS.register(modBus);
		TechParticleTypes.PARTICLE_TYPES.register(modBus);

		ModLoadingContext.get().registerConfig(Type.CLIENT, TechConfig.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(Type.COMMON, TechConfig.COMMON_SPEC);

		CraftingHelper.register(EnabledCondition.Serializer.INSTANCE);
	}

	private void setup(final FMLCommonSetupEvent event) {
		PacketHandler.init();
	}

	private void gatherData(GatherDataEvent event) {
		DataGenerator datagenerator = event.getGenerator();
		PackOutput packOutput = datagenerator.getPackOutput();
		ExistingFileHelper fileHelper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		datagenerator.addProvider(event.includeServer(), new TechRecipes(packOutput));
		TechBlockTagProvider blockTagProvider = new TechBlockTagProvider(packOutput, lookupProvider, fileHelper);
		datagenerator.addProvider(event.includeServer(), blockTagProvider);
		datagenerator.addProvider(event.includeServer(), new TechItemTagProvider(packOutput, lookupProvider, blockTagProvider, fileHelper));
		datagenerator.addProvider(event.includeServer(), new TechEntityTagProvider(packOutput, lookupProvider, fileHelper));
		datagenerator.addProvider(event.includeServer(), new TechLootProvider(packOutput, Collections.emptySet(), List.of(new LootTableProvider.SubProviderEntry(BlockTables::new, LootContextParamSets.BLOCK))));

		BridgeModelProvider loadedModels = new BridgeModelProvider(packOutput, fileHelper);
		datagenerator.addProvider(event.includeClient(), new TechBlockstateProvider(packOutput, fileHelper, loadedModels));
		datagenerator.addProvider(event.includeClient(), loadedModels);
		datagenerator.addProvider(event.includeClient(), new TechItemModelProvider(packOutput, fileHelper));
	}
}
