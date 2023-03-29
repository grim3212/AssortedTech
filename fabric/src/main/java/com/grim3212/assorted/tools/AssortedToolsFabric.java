package com.grim3212.assorted.tools;

import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.sounds.ToolsCommonMod;
import com.grim3212.assorted.tools.common.item.FabricBetterBucketFluidHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.item.Item;

public class AssortedToolsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ToolsCommonMod.init();

        FluidStorage.ITEM.registerForItems((i, c) -> new FabricBetterBucketFluidHandler(c), TechItems.buckets().toArray(new Item[0]));
    }
}
