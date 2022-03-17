package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.item.TechItems;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class TechItemModelProvider extends ItemModelProvider {

	public TechItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, AssortedTech.MODID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Assorted Tech item models";
	}

	@Override
	protected void registerModels() {
		withExistingParent(TechItems.FLIP_FLOP_TORCH.getId().getPath(), "item/generated").texture("layer0", prefix("block/flip_flop_torch_off"));
		withExistingParent(TechItems.GLOWSTONE_TORCH.getId().getPath(), "item/generated").texture("layer0", prefix("block/glowstone_torch_off"));
	}

	private ResourceLocation prefix(String name) {
		return new ResourceLocation(AssortedTech.MODID, name);
	}
}
