package com.grim3212.assorted.tech.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class RetexturableBlockModel extends BlockModel {
	private final Map<String, Material> replacements = new HashMap<>();

	public static RetexturableBlockModel from(BlockModel parent) {
		RetexturableBlockModel model = new RetexturableBlockModel(parent.getParentLocation(), parent.getElements(), parent.textureMap, parent.hasAmbientOcclusion, parent.getGuiLight(), parent.getTransforms(), parent.getOverrides());
		model.customData.copyFrom(parent.customData);
		return model;
	}

	public RetexturableBlockModel(ResourceLocation parentLocation, List<BlockElement> elements, Map<String, Either<Material, String>> textures, boolean ambientOcclusion, GuiLight guiLight, ItemTransforms cameraTransforms, List<ItemOverride> overrides) {
		super(parentLocation, elements, textures, ambientOcclusion, guiLight, cameraTransforms, overrides);
	}

	@Override
	public Material getMaterial(String nameIn) {
		if (this.replacements.containsKey(nameIn)) {
			return this.replacements.get(nameIn);
		}

		return super.getMaterial(nameIn);
	}

	public void replaceTexture(String name, Material texture) {
		this.replacements.put(name, texture);
	}

	public void replaceTexture(String name, ResourceLocation texture) {
		this.replacements.put(name, new Material(InventoryMenu.BLOCK_ATLAS, texture));
	}

	public RetexturableBlockModel retexture(ImmutableMap<String, String> textures) {
		textures.forEach((name, texture) -> replaceTexture(name, new ResourceLocation(texture)));
		return this;
	}
}
