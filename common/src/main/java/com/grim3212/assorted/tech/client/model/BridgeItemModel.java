package com.grim3212.assorted.tech.client.model;

import com.grim3212.assorted.lib.client.model.data.IBlockModelData;
import com.grim3212.assorted.lib.client.model.data.IModelDataBuilder;
import com.grim3212.assorted.lib.client.model.item.DataAwareItemModel;
import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.properties.TechModelProperties;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * The bridge item, drawn with the block it has absorbed rather than its empty fallback.
 * <p>
 * 1.20.1 did this with a {@code BridgeItemOverrideList} on the baked model. {@code ItemOverrides} is
 * gone; this registers the {@code assortedtech:bridge} item model type, which reads the stack's
 * {@code stored_state} and draws through AssortedLib's {@link DataAwareItemModel} - the same shape
 * AssortedDecor's colorizer items use.
 */
public final class BridgeItemModel {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "bridge");

    private BridgeItemModel() {
    }

    private static BlockState storedState(ItemStack stack) {
        if (!NBTHelper.hasTag(stack, "stored_state")) {
            return Blocks.AIR.defaultBlockState();
        }

        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK, NBTHelper.getTag(stack, "stored_state"));
    }

    private static IBlockModelData modelData(ItemStack stack) {
        return IModelDataBuilder.create().withInitial(TechModelProperties.BLOCK_STATE, storedState(stack)).build();
    }

    /**
     * @param model The bridge <em>block</em> model - the json carrying the {@code assortedtech:bridge}
     *              loader.
     * @param tints {@code assortedtech:bridge} colours a stored block that is itself tinted.
     */
    public record Unbaked(Identifier model, List<ItemTintSource> tints) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            return DataAwareItemModel.bake(context, this.model, this.tints, BridgeItemModel::modelData, BridgeItemModel::storedState);
        }
    }
}
