package com.grim3212.assorted.tech.client.model;

import com.google.common.collect.ImmutableMap;
import com.grim3212.assorted.lib.client.model.loaders.context.IModelBakingContext;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;

/**
 * The json bridge: the {@code "bridge"} object names a parent model, and each stored block state
 * re-bakes that parent with the stored block's texture in the {@code stored} slot, walking the
 * parent chain as vanilla inheritance does (overrides first). The item form is {@link
 * BridgeItemModel}.
 */
public class BridgeBakedModel extends BridgeBaseBakedModel {

    public BridgeBakedModel(IModelBakingContext context, BridgeUnbakedModel.Bridge unbakedBridge, ModelBaker bakery, ModelState transform, Identifier name) {
        super(context, unbakedBridge, bakery, transform, name);
    }

    // UnbakedGeometry#bake is deprecated by NeoForge in favour of a ContextMap aware overload declared
    // on its own UnbakedGeometryExtension, which does not exist in the vanilla jar this common module
    // compiles against - so the replacement is unreachable from here. Vanilla still declares this one.
    @SuppressWarnings("deprecation")
    @Override
    protected BlockStateModel generateModel(ImmutableMap<String, String> textures) {
        ResolvedModel parent = this.bakery.getModel(this.model.parent());

        TextureSlots.Data.Builder overrides = new TextureSlots.Data.Builder();
        this.model.textures().forEach((slot, texture) -> overrides.addTexture(slot, new Material(Identifier.parse(texture))));
        textures.forEach((slot, texture) -> overrides.addTexture(slot, new Material(Identifier.parse(texture))));

        TextureSlots.Resolver resolver = new TextureSlots.Resolver();
        resolver.addLast(overrides.build());
        for (ResolvedModel current = parent; current != null; current = current.parent()) {
            resolver.addLast(current.wrapped().textureSlots());
        }

        TextureSlots slots = resolver.resolve(this.debugName);

        QuadCollection quads = ResolvedModel.findTopGeometry(parent).bake(slots, this.bakery, this.transform, this.debugName);
        boolean ambientOcclusion = ResolvedModel.findTopAmbientOcclusion(parent);
        Material.Baked particle = this.bakery.materials().resolveSlot(slots, "particle", this.debugName);

        return new SingleVariant(new SimpleModelWrapper(quads, ambientOcclusion, particle));
    }
}
