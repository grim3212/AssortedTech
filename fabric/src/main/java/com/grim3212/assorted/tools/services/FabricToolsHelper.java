package com.grim3212.assorted.tools.services;

import com.grim3212.assorted.tech.sounds.common.item.MultiToolItem;
import com.grim3212.assorted.tech.sounds.services.IToolsHelper;
import com.grim3212.assorted.tools.common.item.FabricMultiToolItem;
import net.minecraft.world.item.Item;

public class FabricToolsHelper implements IToolsHelper {
    @Override
    public MultiToolItem getMultiToolItem(ItemTierConfig tier, Item.Properties builderIn) {
        return new FabricMultiToolItem(tier, builderIn);
    }
}
