package com.grim3212.assorted.tech.client.model;

import com.google.common.collect.ImmutableMap;
import com.grim3212.assorted.lib.client.model.RetexturableBlockModel;
import com.grim3212.assorted.lib.client.model.loaders.context.IModelBakingContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class BridgeBakedModel extends BridgeBaseBakedModel {
    public BridgeBakedModel(IModelBakingContext context, BlockModel unbakedBridge, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation name) {
        super(context, unbakedBridge, bakery, spriteGetter, transform, name);
    }

    @Override
    protected BakedModel generateModel(ImmutableMap<String, String> texture) {
        RetexturableBlockModel toBake = RetexturableBlockModel.from(this.unbakedBridge, this.name);
        return toBake.retexture(texture).bake(this.bakery, toBake, this.spriteGetter, this.transform, this.name, true);
    }
}
