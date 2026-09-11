package com.grim3212.assorted.tech.client.color;

import com.grim3212.assorted.lib.platform.ClientServices;
import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.tech.Constants;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Tints a held bridge with the colour of the block state it carries, taken from the stored block's
 * own {@link BlockTintSource} at layer 0. {@code items/bridge.json} names it as
 * {@code "tints": [{"type": "assortedtech:bridge"}]}.
 */
public record BridgeItemTintSource() implements ItemTintSource {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "bridge");
    public static final MapCodec<BridgeItemTintSource> MAP_CODEC = MapCodec.unit(new BridgeItemTintSource());

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (NBTHelper.hasTag(itemStack, "stored_state")) {
            // Registry implements HolderLookup.RegistryLookup itself now, so there is no asLookup() view.
            BlockState stored = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, NBTHelper.getTag(itemStack, "stored_state"));
            BlockTintSource source = ClientServices.CLIENT.getBlockColors().getTintSource(stored, 0);
            if (source != null) {
                return source.color(stored);
            }
        }
        // Colours carry an alpha channel now, so an opaque white is -1 rather than 0xFFFFFF.
        return -1;
    }

    @Override
    public MapCodec<BridgeItemTintSource> type() {
        return MAP_CODEC;
    }
}
