package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.item.TechItems;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
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
		withExistingParent(name(TechItems.FLIP_FLOP_TORCH.get()), "item/generated").texture("layer0", prefix("block/flip_flop_torch_off"));
		withExistingParent(name(TechItems.GLOWSTONE_TORCH.get()), "item/generated").texture("layer0", prefix("block/glowstone_torch_off"));
	}

	private ItemModelBuilder generatedItem(String name) {
		return withExistingParent(name, "item/generated").texture("layer0", prefix("item/" + name));
	}

	private ItemModelBuilder generatedItem(Item i) {
		return generatedItem(name(i));
	}

	private static String name(Item i) {
		return Registry.ITEM.getKey(i).getPath();
	}

	private ResourceLocation prefix(String name) {
		return new ResourceLocation(AssortedTech.MODID, name);
	}
}
