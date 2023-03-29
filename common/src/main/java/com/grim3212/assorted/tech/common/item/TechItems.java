package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;

import java.util.function.Supplier;

public class TechItems {

    public static final IRegistryObject<StandingAndWallBlockItem> FLIP_FLOP_TORCH = register("flip_flop_torch", () -> new StandingAndWallBlockItem(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final IRegistryObject<StandingAndWallBlockItem> GLOWSTONE_TORCH = register("glowstone_torch", () -> new StandingAndWallBlockItem(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));

    public static final IRegistryObject<GravityArmorItem> GRAVITY_BOOTS = register("gravity_boots", () -> new GravityArmorItem(EquipmentSlot.FEET, new Item.Properties()));

    private static <T extends Item> IRegistryObject<T> register(final String name, final Supplier<T> sup) {
        return TechBlocks.ITEMS.register(name, sup);
    }

    public static void init() {
    }
}
