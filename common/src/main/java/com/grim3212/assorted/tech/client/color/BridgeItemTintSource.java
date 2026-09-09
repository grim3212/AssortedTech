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
 * Tints a held bridge block with the colour of the block state it is carrying, which is what the old
 * {@code IClientHelper#registerItemColor} lambda in {@code TechClient} did.
 * <p>
 * {@code ItemColor} and {@code ItemColors} were deleted in 26.2: an item's tints are a list of
 * {@link ItemTintSource} entries in its item model json, and code only registers the {@link MapCodec}
 * that reads a custom source type, keyed by id.
 * <p>
 * TODO(26.2): nothing reaches this source yet. {@code assets/assortedtech/items/bridge.json} has to
 *  carry {@code "tints": [{"type": "assortedtech:bridge"}]} for it to be consulted, which is a datagen
 *  change this class cannot make. Until then a held bridge block is untinted.
 * <p>
 * Behaviour note: the 1.20.1 lambda answered {@code ItemColors#getColor} for a stack of the stored
 * block, and returned opaque white for any tint index other than the model's. There is no
 * {@code ItemColors} to ask any more and the tint layer is decided by this source's position in the
 * json list rather than by an index argument, so the colour is taken from the stored block's own
 * {@link BlockTintSource} at layer 0 instead - the same answer for every vanilla block item, whose
 * item colours were block-colour delegates.
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
