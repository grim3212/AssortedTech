package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Stream;

/**
 * Forge's {@code ItemModelProvider} / {@code ItemModelBuilder} / {@code ExistingFileHelper} are gone,
 * and so is the idea that an item model is one json. An item points at a data driven
 * {@code ItemModel} in {@code assets/assortedtech/items/} - a {@code minecraft:model} /
 * {@code select} / {@code condition} tree - which names the {@code assets/assortedtech/models/}
 * geometry to draw. {@link ModelProvider} writes both halves.
 * <p>
 * This provider owns exactly the three items whose model is not derived from a block model, which is
 * the same set the 1.20.1 {@code TechItemModelProvider} covered. Two of them are {@link BlockItem}s
 * (the torches are {@code StandingAndWallBlockItem}s registered separately from their blocks), so
 * {@link TechBlockstateProvider} filters them back out of its own {@link #getKnownItems()} - a
 * {@link ModelProvider} refuses a duplicate item model definition, and its
 * {@code finalizeAndValidate} would otherwise point a {@code BlockItem} with no model of its own at
 * the block model, which for a torch is the 3D block rather than the flat inventory sprite.
 * <p>
 * {@code generateFlatItem(item, template)} is not usable here: it derives the texture from the item's
 * id, and both torch textures live under {@code block/}. Each model therefore goes through
 * {@link ModelTemplate#create(Identifier, TextureMapping, java.util.function.BiConsumer)} followed by
 * {@code itemModelOutput.accept(...)}, the same shape {@code ToolsItemModelProvider} uses.
 * {@code item/generated} is {@link ModelTemplates#FLAT_ITEM}, which resolves to the same vanilla
 * parent.
 */
public class TechItemModelProvider extends ModelProvider {

    public TechItemModelProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    /**
     * The items this provider claims. Read by {@link TechBlockstateProvider} so the two never write
     * the same file.
     */
    private static List<Item> owned() {
        return List.of(TechItems.FLIP_FLOP_TORCH.get(), TechItems.GLOWSTONE_TORCH.get(), TechItems.GRAVITY_BOOTS.get());
    }

    public static boolean owns(Item item) {
        return owned().contains(item);
    }

    @Override
    public String getName() {
        return "Assorted Tech item models";
    }

    /**
     * This provider registers no blocks at all; {@link TechBlockstateProvider} owns every one.
     */
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.empty();
    }

    /**
     * Filtered out of the registry rather than built from {@code Item#builtInRegistryHolder()}, which
     * is deprecated, and symmetrical with the filter {@link TechBlockstateProvider} applies.
     */
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return super.getKnownItems().filter(holder -> owns(holder.value()));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        flatItem(itemModels, TechItems.FLIP_FLOP_TORCH.get(), "block/flip_flop_torch_off");
        flatItem(itemModels, TechItems.GLOWSTONE_TORCH.get(), "block/glowstone_torch_off");
        flatItem(itemModels, TechItems.GRAVITY_BOOTS.get(), "item/gravity_boots");
    }

    private void flatItem(ItemModelGenerators itemModels, Item item, String texture) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(modelId(name(item)), TextureMapping.layer0(prefixed(texture)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private static String name(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static Identifier modelId(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "item/" + path);
    }

    private static Material prefixed(String texture) {
        return new Material(Identifier.fromNamespaceAndPath(Constants.MOD_ID, texture));
    }
}
