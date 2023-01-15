package com.grim3212.assorted.tech.client.model;

import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

public class BridgeBakedModel extends BridgeBaseBakedModel {

	private final UnbakedModel unbakedModel;

	public BridgeBakedModel(BakedModel bakedColorizer, UnbakedModel unbakedModel, IGeometryBakingContext owner, TextureAtlasSprite baseSprite, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation name) {
		super(bakedColorizer, owner, baseSprite, bakery, spriteGetter, transform, overrides, name);
		this.unbakedModel = unbakedModel;
	}

	@Override
	protected BakedModel generateModel(ImmutableMap<String, String> texture) {
		RetexturableBlockModel toBake = RetexturableBlockModel.from((BlockModel) this.unbakedModel);
		return toBake.retexture(texture).bake(this.bakery, toBake, this.spriteGetter, this.transform, this.name, true);
	}

}
