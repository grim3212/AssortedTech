package com.grim3212.assorted.tech.client.model;

import com.google.common.collect.ImmutableMap;
import com.grim3212.assorted.lib.client.model.RetexturableBlockModel;
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
 * The json flavour of the bridge: the {@code "bridge"} object names a parent model, and every stored
 * block state re-bakes that parent with the stored block's texture in the {@code stored} slot.
 * <p>
 * There is no mutable json model object to copy and retexture any more - {@code BlockModel} is a
 * {@code CuboidModel} record whose textures are a {@link TextureSlots.Data}, and its element and face
 * deserializers are package private, so an inline geometry object could not be re-read here either.
 * The parent chain is walked directly instead, which is what vanilla model inheritance does: the
 * overrides go in child-first, the parent's own slots after them, and
 * {@link ResolvedModel#findTopGeometry} supplies the geometry to bake against the resolved slots.
 * This is the same route AssortedDecor's {@code ColorizerBakedModel} takes.
 * <p>
 * AssortedLib's {@link RetexturableBlockModel} is deliberately <em>not</em> used, and this class was
 * its last remaining caller. Two things stop it: it is an
 * {@link net.minecraft.client.resources.model.UnbakedModel} with no bake entry point of its own, and
 * a {@link ModelBaker} can only turn an {@link Identifier} into a {@link ResolvedModel} - there is no
 * way to resolve an ad-hoc {@code UnbakedModel} instance - so a retextured copy could never be baked;
 * and its {@code retexture} builds slot names verbatim, so this mod's 1.20.1 {@code "#stored"} key
 * would have produced a slot {@code TextureSlots#getMaterial} can never find (it strips the leading
 * {@code #} before looking a slot up). See the report entry in REVIEW-BEHAVIOUR-CHANGES.md.
 * <p>
 * The item form is {@link BridgeItemModel}, which reaches this model's cache through AssortedLib's
 * {@code DataAwareItemModel}. The {@code BridgeItemOverrideList} 1.20.1 hung off this class went with
 * {@code ItemOverrides}.
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
