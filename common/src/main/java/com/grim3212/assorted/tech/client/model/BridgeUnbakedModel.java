package com.grim3212.assorted.tech.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.grim3212.assorted.lib.client.model.loaders.IModelSpecification;
import com.grim3212.assorted.lib.client.model.loaders.IModelSpecificationLoader;
import com.grim3212.assorted.lib.client.model.loaders.context.IModelBakingContext;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.util.Map;

public class BridgeUnbakedModel implements IModelSpecification<BridgeUnbakedModel> {

    public static final Identifier LOADER_NAME = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "bridge");

    private final Bridge unbakedBridge;

    private BridgeUnbakedModel(Bridge unbakedBridge) {
        this.unbakedBridge = unbakedBridge;
    }

    @Override
    public BlockStateModel bake(IModelBakingContext context, ModelBaker baker, ModelState modelState, Identifier modelLocation) {
        return new BridgeBakedModel(context, this.unbakedBridge, baker, modelState, modelLocation);
    }

    // BridgeBakedModel resolves the shape with baker.getModel(parent), and nothing else pulls
    // tinted_cube in - it is the json parent of nothing and no blockstate names it - so without this
    // discovery never sees it and the bridges bake to the missing model.
    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {
        resolver.markDependency(this.unbakedBridge.parent());
    }

    /**
     * The {@code "bridge"} object of a bridge model json: the parent model the bridge takes its shape
     * from plus the texture slots that parent still needs filled in - in practice {@code stored},
     * which is what a stored block state overrides at render time and what the bridge falls back to
     * when it is holding nothing.
     * <p>
     * It used to be deserialized into a whole {@code BlockModel}. A 26.2 json model is a
     * {@code CuboidModel} record whose element and face deserializers are package private, so it
     * cannot be read from a foreign {@link JsonDeserializationContext}; the two fields the bridge
     * models actually use are read directly instead and the parent is resolved through the
     * {@link ModelBaker} at bake time like any other model reference. This mirrors AssortedDecor's
     * {@code ColorizerUnbakedModel.Colorizer}, which is the same json shape.
     */
    public record Bridge(Identifier parent, ImmutableMap<String, String> textures) {
    }

    public static final class Loader implements IModelSpecificationLoader<BridgeUnbakedModel> {
        public static final Loader INSTANCE = new Loader();

        @Override
        public BridgeUnbakedModel read(JsonDeserializationContext deserializationContext, JsonObject jsonObject) {
            if (!jsonObject.has("bridge"))
                throw new JsonParseException("Bridge Loader requires a 'bridge' key holding the model to retexture.");

            JsonObject bridge = GsonHelper.getAsJsonObject(jsonObject, "bridge");
            Identifier parent = Identifier.parse(GsonHelper.getAsString(bridge, "parent"));

            ImmutableMap.Builder<String, String> textures = ImmutableMap.builder();
            if (bridge.has("textures")) {
                for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(bridge, "textures").entrySet()) {
                    textures.put(entry.getKey(), entry.getValue().getAsString());
                }
            }

            return new BridgeUnbakedModel(new Bridge(parent, textures.build()));
        }
    }
}
