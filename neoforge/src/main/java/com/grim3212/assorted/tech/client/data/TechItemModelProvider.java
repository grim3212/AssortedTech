package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.client.render.ExtruderRenderer;
import com.grim3212.assorted.tech.client.render.ExtruderSpecialRenderer;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Item models for the items not drawn from a block model, including the two torch block items,
 * whose inventory model is a flat sprite from {@code block/}. That is why {@code generateFlatItem},
 * which takes the texture from the item id, is not used.
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
        List<Item> owned = new ArrayList<>(List.of(TechItems.FLIP_FLOP_TORCH.get(), TechItems.GLOWSTONE_TORCH.get(), TechItems.GRAVITY_BOOTS.get(), TechItems.GPS.get()));
        TechItems.EXTRUDERS.values().forEach(extruder -> owned.add(extruder.get()));
        return owned;
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
        TechItems.EXTRUDERS.forEach((type, extruder) -> extruder(itemModels, extruder.get(), type));
        flatItem(itemModels, TechItems.GPS.get(), "item/gps");
    }

    /**
     * The extruder itself in 3D, drawn by {@link ExtruderSpecialRenderer}. The base model only holds
     * {@code block/block}'s display settings and the particle for when it breaks, which has to be an
     * atlas sprite rather than the entity texture: the body's back, cut from it, the same for all.
     */
    private void extruder(ItemModelGenerators itemModels, Item item, ExtruderType type) {
        TextureMapping particle = new TextureMapping().put(TextureSlot.PARTICLE, prefixed("item/extruder_particle"));
        Identifier base = EXTRUDER_BASE.create(modelId(name(item)), particle, itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.specialModel(base, new ExtruderSpecialRenderer.Unbaked(ExtruderRenderer.texture(type))));
    }

    private void flatItem(ItemModelGenerators itemModels, Item item, String texture) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(modelId(name(item)), TextureMapping.layer0(prefixed(texture)), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private static String name(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    private static final ModelTemplate EXTRUDER_BASE = ExtendedModelTemplateBuilder.builder()
            .parent(Identifier.withDefaultNamespace("block/block"))
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .build();

    private static Identifier modelId(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "item/" + path);
    }

    private static Material prefixed(String texture) {
        return new Material(Identifier.fromNamespaceAndPath(Constants.MOD_ID, texture));
    }
}
