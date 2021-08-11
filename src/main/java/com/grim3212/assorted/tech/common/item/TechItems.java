package com.grim3212.assorted.tech.common.item;

import java.util.function.Supplier;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.handler.EnabledCondition;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TechItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AssortedTech.MODID);

	public static final RegistryObject<EnabledStandingAndWallBlockItem> FLIP_FLOP_TORCH = register("flip_flop_torch", () -> new EnabledStandingAndWallBlockItem(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get(), EnabledCondition.TORCHES_CONDITION, new Item.Properties().tab(AssortedTech.ASSORTED_TECH_ITEM_GROUP)));
	public static final RegistryObject<EnabledStandingAndWallBlockItem> GLOWSTONE_TORCH = register("glowstone_torch", () -> new EnabledStandingAndWallBlockItem(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get(), EnabledCondition.TORCHES_CONDITION, new Item.Properties().tab(AssortedTech.ASSORTED_TECH_ITEM_GROUP)));

	private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> sup) {
		return ITEMS.register(name, sup);
	}
}
