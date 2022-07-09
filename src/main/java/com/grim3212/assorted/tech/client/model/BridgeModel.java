package com.grim3212.assorted.tech.client.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

public class BridgeModel implements IUnbakedGeometry<BridgeModel> {
	private BlockModel unbakedBridge;

	private BridgeModel(BlockModel unbakedBridge) {
		this.unbakedBridge = unbakedBridge;
	}

	@Nonnull
	@Override
	public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		Set<Material> ret = new HashSet<>();
		ret.addAll(this.unbakedBridge.getMaterials(modelGetter, missingTextureErrors));
		return ret;
	}

	@Nullable
	@Override
	public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation name) {
		BakedModel bakedBridge = unbakedBridge.bake(bakery, spriteGetter, transform, name);
		return new BridgeBakedModel(bakedBridge, unbakedBridge, owner, spriteGetter.apply(owner.getMaterial("particle")), bakery, spriteGetter, transform, overrides, name);
	}

	public enum Loader implements IGeometryLoader<BridgeModel> {
		INSTANCE;

		@Nonnull
		@Override
		public BridgeModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
			BlockModel bridge = deserializationContext.deserialize(jsonObject.getAsJsonObject("bridge"), BlockModel.class);
			return new BridgeModel(bridge);
		}
	}
}
