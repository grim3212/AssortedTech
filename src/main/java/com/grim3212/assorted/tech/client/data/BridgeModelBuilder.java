package com.grim3212.assorted.tech.client.data;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BridgeModelBuilder extends ModelBuilder<BridgeModelBuilder> {

	private ResourceLocation loader;
	private ResourceLocation model;
	private ResourceLocation bridge;
	private Map<String, ResourceLocation> textures;

	protected BridgeModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
		super(outputLocation, existingFileHelper);
		this.textures = Maps.newHashMap();
	}

	public BridgeModelBuilder loader(ResourceLocation loader) {
		this.loader = loader;
		return this;
	}

	public BridgeModelBuilder objModel(ResourceLocation model) {
		this.model = model;
		return this;
	}

	public BridgeModelBuilder bridge(ResourceLocation bridge) {
		this.bridge = bridge;
		return this;
	}

	public BridgeModelBuilder addTexture(String name, ResourceLocation texture) {
		this.textures.put(name, texture);
		return this;
	}

	@Override
	public JsonObject toJson() {
		JsonObject ret = super.toJson();
		if (loader != null) {
			ret.addProperty("loader", loader.toString());
		}

		if (model != null) {
			ret.addProperty("model", model.toString());

			if (this.textures.size() > 0) {
				JsonObject textureObj = new JsonObject();

				textures.forEach((k, v) -> {
					textureObj.addProperty(k, v.toString());
				});

				ret.add("textures", textureObj);
			}
		}

		if (bridge != null) {
			JsonObject colorizerObj = new JsonObject();
			colorizerObj.addProperty("parent", bridge.toString());

			if (this.textures.size() > 0) {
				JsonObject textureObj = new JsonObject();

				textures.forEach((k, v) -> {
					textureObj.addProperty(k, v.toString());
				});

				colorizerObj.add("textures", textureObj);
			}

			ret.add("bridge", colorizerObj);
		}
		return ret;
	}
}