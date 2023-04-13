package com.grim3212.assorted.tech.common.handlers;

import com.grim3212.assorted.lib.core.creative.CreativeTabItems;
import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TechCreativeItems {

    private static List<ItemStack> getCreativeItems() {
        CreativeTabItems items = new CreativeTabItems();

        if (TechCommonMod.COMMON_CONFIG.gravityEnabled.get()) {
            items.add(TechItems.GRAVITY_BOOTS.get());

            items.add(TechBlocks.ATTRACTOR.get());
            items.add(TechBlocks.ATTRACTOR_DIRECTIONAL.get());
            items.add(TechBlocks.REPULSOR.get());
            items.add(TechBlocks.REPULSOR_DIRECTIONAL.get());
            items.add(TechBlocks.GRAVITOR.get());
            items.add(TechBlocks.GRAVITOR_DIRECTIONAL.get());
        }

        if (TechCommonMod.COMMON_CONFIG.torchesEnabled.get()) {
            items.add(TechItems.FLIP_FLOP_TORCH.get());
            items.add(TechItems.GLOWSTONE_TORCH.get());
        }

        if (TechCommonMod.COMMON_CONFIG.fanEnabled.get()) {
            items.add(TechBlocks.FAN.get());
        }

        if (TechCommonMod.COMMON_CONFIG.alarmEnabled.get()) {
            items.add(TechBlocks.ALARM.get());
        }

        if (TechCommonMod.COMMON_CONFIG.bridgesEnabled.get()) {
            items.add(TechBlocks.BRIDGE_CONTROL_ACCEL.get());
            items.add(TechBlocks.BRIDGE_CONTROL_DEATH.get());
            items.add(TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
            items.add(TechBlocks.BRIDGE_CONTROL_LASER.get());
            items.add(TechBlocks.BRIDGE_CONTROL_TRICK.get());
        }

        if (TechCommonMod.COMMON_CONFIG.sensorsEnabled.get()) {
            TechBlocks.SENSORS.forEach((sensor) -> {
                items.add(sensor.get());
            });
        }

        if (TechCommonMod.COMMON_CONFIG.spikesEnabled.get()) {
            TechBlocks.SPIKES.forEach((spike) -> {
                if (canNotCraft(spike.get().getSpikeType())) {
                    return;
                }

                items.add(spike.get());
            });
        }

        return items.getItems();
    }

    private static boolean canNotCraft(SpikeType type) {
        return TechCommonMod.COMMON_CONFIG.hideUncraftableItems.get() && BuiltInRegistries.ITEM.getTag(type.getMaterial()).isPresent() && BuiltInRegistries.ITEM.getTag(type.getMaterial()).get().stream().count() < 1;
    }

    public static void init() {
        Services.PLATFORM.registerCreativeTab(new ResourceLocation(Constants.MOD_ID, "tab"), Component.translatable("itemGroup." + Constants.MOD_ID), () -> new ItemStack(TechBlocks.FLIP_FLOP_TORCH.get()), TechCreativeItems::getCreativeItems);
    }
}
