package com.grim3212.assorted.tech;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.grim3212.assorted.tech.client.data.TechBlockstateProvider;
import com.grim3212.assorted.tech.client.data.TechItemModelProvider;
import com.grim3212.assorted.tech.client.proxy.ClientProxy;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.data.TechBlockTagProvider;
import com.grim3212.assorted.tech.common.data.TechEntityTagProvider;
import com.grim3212.assorted.tech.common.data.TechItemTagProvider;
import com.grim3212.assorted.tech.common.data.TechLootProvider;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.proxy.IProxy;
import com.grim3212.assorted.tech.common.util.TechSounds;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod(AssortedTech.MODID)
public class AssortedTech {
	public static final String MODID = "assortedtech";
	public static final String MODNAME = "Assorted Tech";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static IProxy proxy = new IProxy() {
	};

	public static final CreativeModeTab ASSORTED_TECH_ITEM_GROUP = (new CreativeModeTab(AssortedTech.MODID) {
		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack makeIcon() {
			return new ItemStack(TechBlocks.FLIP_FLOP_TORCH.get());
		}
	});

	public AssortedTech() {
		DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> proxy = new ClientProxy());
		proxy.starting();

		final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		modBus.addListener(this::gatherData);

		TechBlocks.BLOCKS.register(modBus);
		TechItems.ITEMS.register(modBus);
		TechBlockEntityTypes.BLOCK_ENTITIES.register(modBus);
		TechSounds.SOUNDS.register(modBus);

		ModLoadingContext.get().registerConfig(Type.COMMON, TechConfig.COMMON_SPEC);
	}

	private void gatherData(GatherDataEvent event) {
		DataGenerator datagenerator = event.getGenerator();
		ExistingFileHelper fileHelper = event.getExistingFileHelper();

		if (event.includeServer()) {
			TechBlockTagProvider blockTagProvider = new TechBlockTagProvider(datagenerator, fileHelper);
			datagenerator.addProvider(blockTagProvider);
			datagenerator.addProvider(new TechItemTagProvider(datagenerator, blockTagProvider, fileHelper));
			datagenerator.addProvider(new TechEntityTagProvider(datagenerator, fileHelper));
			datagenerator.addProvider(new TechLootProvider(datagenerator));
		}

		if (event.includeClient()) {
			datagenerator.addProvider(new TechBlockstateProvider(datagenerator, fileHelper));
			datagenerator.addProvider(new TechItemModelProvider(datagenerator, fileHelper));
		}
	}
}
