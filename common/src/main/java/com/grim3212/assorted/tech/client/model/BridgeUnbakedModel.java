package com.grim3212.assorted.tech.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.grim3212.assorted.lib.client.model.loaders.IModelSpecification;
import com.grim3212.assorted.lib.client.model.loaders.IModelSpecificationLoader;
import com.grim3212.assorted.lib.client.model.loaders.context.IModelBakingContext;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class BridgeUnbakedModel implements IModelSpecification<BridgeUnbakedModel> {

    public static final ResourceLocation LOADER_NAME = new ResourceLocation(Constants.MOD_ID, "bridge");

    private BlockModel unbakedBridge;

    private BridgeUnbakedModel(BlockModel unbakedBridge) {
        this.unbakedBridge = unbakedBridge;
    }

    @Override
    public BakedModel bake(IModelBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation) {
        this.unbakedBridge.resolveParents(baker::getModel);
        return new BridgeBakedModel(context, unbakedBridge, baker, spriteGetter, modelState, modelLocation);
    }

    public static final class Loader implements IModelSpecificationLoader<BridgeUnbakedModel> {
        public static final Loader INSTANCE = new Loader();

        public BridgeUnbakedModel read(JsonDeserializationContext deserializationContext, JsonObject jsonObject) {
            return new BridgeUnbakedModel(deserializationContext.deserialize(jsonObject.getAsJsonObject("bridge"), BlockModel.class));
        }
    }
}
