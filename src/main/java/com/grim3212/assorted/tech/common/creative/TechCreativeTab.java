package com.grim3212.assorted.tech.common.creative;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.util.SpikeType;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class TechCreativeTab {

	public static void registerTabs(final CreativeModeTabEvent.Register event) {
		event.registerCreativeModeTab(new ResourceLocation(AssortedTech.MODID, "tab"), builder -> builder.title(Component.translatable("itemGroup.assortedtech")).icon(() -> new ItemStack(TechBlocks.FLIP_FLOP_TORCH.get())).displayItems((enabledFlags, populator, hasPermissions) -> {

			if (TechConfig.COMMON.gravityEnabled.get()) {
				populator.accept(TechItems.GRAVITY_BOOTS.get());
				
				populator.accept(TechBlocks.ATTRACTOR.get());
				populator.accept(TechBlocks.ATTRACTOR_DIRECTIONAL.get());
				populator.accept(TechBlocks.REPULSOR.get());
				populator.accept(TechBlocks.REPULSOR_DIRECTIONAL.get());
				populator.accept(TechBlocks.GRAVITOR.get());
				populator.accept(TechBlocks.GRAVITOR_DIRECTIONAL.get());
			}

			if (TechConfig.COMMON.torchesEnabled.get()) {
				populator.accept(TechItems.FLIP_FLOP_TORCH.get());
				populator.accept(TechItems.GLOWSTONE_TORCH.get());
			}
			
			if (TechConfig.COMMON.fanEnabled.get()) {
				populator.accept(TechBlocks.FAN.get());
			}
			
			if (TechConfig.COMMON.alarmEnabled.get()) {
				populator.accept(TechBlocks.ALARM.get());
			}
			
			if (TechConfig.COMMON.bridgesEnabled.get()) {
				populator.accept(TechBlocks.BRIDGE_CONTROL_ACCEL.get());
				populator.accept(TechBlocks.BRIDGE_CONTROL_DEATH.get());
				populator.accept(TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
				populator.accept(TechBlocks.BRIDGE_CONTROL_LASER.get());
				populator.accept(TechBlocks.BRIDGE_CONTROL_TRICK.get());
			}

			if (TechConfig.COMMON.sensorsEnabled.get()) {
				TechBlocks.SENSORS.forEach((sensor) -> {
					populator.accept(sensor.get());
				});
			}
			
			if (TechConfig.COMMON.spikesEnabled.get()) {
				TechBlocks.SPIKES.forEach((spike) -> {
					if(canNotCraft(spike.get().getSpikeType())) {
						return;
					}
					
					populator.accept(spike.get());
				});
			}
		}));
	}

	private static boolean canNotCraft(SpikeType type) {
		return TechConfig.COMMON.hideUncraftableItems.get() && ForgeRegistries.ITEMS.tags().getTag(type.getMaterial()).size() <= 0;
	}
}
