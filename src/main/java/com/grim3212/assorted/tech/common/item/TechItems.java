package com.grim3212.assorted.tech.common.item;

import java.util.function.Supplier;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.TechBlocks;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TechItems {

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AssortedTech.MODID);

	public static final RegistryObject<StandingAndWallBlockItem> FLIP_FLOP_TORCH = register("flip_flop_torch", () -> new StandingAndWallBlockItem(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
	public static final RegistryObject<StandingAndWallBlockItem> GLOWSTONE_TORCH = register("glowstone_torch", () -> new StandingAndWallBlockItem(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));

	public static final RegistryObject<GravityArmorItem> GRAVITY_BOOTS = register("gravity_boots", () -> new GravityArmorItem(EquipmentSlot.FEET, new Item.Properties()));

	private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> sup) {
		return ITEMS.register(name, sup);
	}
}
