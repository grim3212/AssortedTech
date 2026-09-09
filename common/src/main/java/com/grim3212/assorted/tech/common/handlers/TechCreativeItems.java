package com.grim3212.assorted.tech.common.handlers;

import com.grim3212.assorted.lib.core.creative.CreativeTabItems;
import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TechCreativeItems {

    public static final RegistryProvider<CreativeModeTab> CREATIVE_TABS = RegistryProvider.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "tab"));

    // CreativeModeTab.Output is protected in 26.2 vanilla, so a display items generator cannot be
    // written against the plain game jar. The tab is registered empty and filled through the
    // library's modifyCreativeTab hook instead, which both loaders already implement on top of
    // their own creative tab events.
    // CreativeModeTab.builder(Row, int) is deprecated by NeoForge's patches only; the vanilla jar
    // this module compiles against has no other builder. See PORTING-26.2.md.
    @SuppressWarnings("deprecation")
    public static final IRegistryObject CREATIVE_TAB = CREATIVE_TABS.register("tab", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.translatable("itemGroup." + Constants.MOD_ID))
            .icon(() -> new ItemStack(TechBlocks.FLIP_FLOP_TORCH.get()))
            .build());

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

    // Registry#getTag is gone; a registry is its own HolderLookup now, so a tag resolves through
    // Registry#get(TagKey) to an Optional<HolderSet.Named>. The original "present but empty" test is
    // kept exactly, so a tag no pack defines at all still leaves the spike visible.
    private static boolean canNotCraft(SpikeType type) {
        return TechCommonMod.COMMON_CONFIG.hideUncraftableItems.get() && BuiltInRegistries.ITEM.get(type.getMaterial()).map(holders -> holders.size() < 1).orElse(false);
    }

    public static void init() {
        Services.PLATFORM.modifyCreativeTab(CREATIVE_TAB_KEY, TechCreativeItems::getCreativeItems);
    }
}
