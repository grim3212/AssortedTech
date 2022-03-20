package com.grim3212.assorted.tech.client.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.grim3212.assorted.tech.AssortedTech;
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
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

public class BridgeModel implements IModelGeometry<BridgeModel> {
	private BlockModel unbakedBridge;

	private BridgeModel(BlockModel unbakedBridge) {
		this.unbakedBridge = unbakedBridge;
	}

	@Nonnull
	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		Set<Material> ret = new HashSet<>();
		ret.addAll(this.unbakedBridge.getMaterials(modelGetter, missingTextureErrors));
		return ret;
	}

	@Nullable
	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation name) {
		BakedModel bakedBridge = unbakedBridge.bake(bakery, spriteGetter, transform, name);
		return new BridgeBakedModel(bakedBridge, unbakedBridge, owner, spriteGetter.apply(owner.resolveTexture("particle")), bakery, spriteGetter, transform, overrides, name);
	}

	public enum Loader implements IModelLoader<BridgeModel> {
		INSTANCE;

		public static final ResourceLocation LOCATION = new ResourceLocation(AssortedTech.MODID, "models/bridge");

		@Override
		public void onResourceManagerReload(@Nonnull ResourceManager resourceManager) {
		}

		@Nonnull
		@Override
		public BridgeModel read(JsonDeserializationContext ctx, JsonObject model) {
			BlockModel bridge = ctx.deserialize(model.getAsJsonObject("bridge"), BlockModel.class);
			return new BridgeModel(bridge);
		}
	}
}
