package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.lib.core.item.ItemDescription;
import com.grim3212.assorted.lib.core.item.LibDataComponents;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class TechItems {

    public static final IRegistryObject<StandingAndWallBlockItem> FLIP_FLOP_TORCH = register("flip_flop_torch", props -> new StandingAndWallBlockItem(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get(), Direction.DOWN, props.useBlockDescriptionPrefix()));
    public static final IRegistryObject<StandingAndWallBlockItem> GLOWSTONE_TORCH = register("glowstone_torch", props -> new StandingAndWallBlockItem(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get(), Direction.DOWN, props.useBlockDescriptionPrefix()));

    public static final IRegistryObject<GravityArmorItem> GRAVITY_BOOTS = register("gravity_boots", GravityArmorItem::new);

    /** One per material, as {@code <material>_extruder}, in {@link ExtruderType} order. */
    public static final Map<ExtruderType, IRegistryObject<ExtruderItem>> EXTRUDERS = new EnumMap<>(ExtruderType.class);
    public static final IRegistryObject<GpsItem> GPS = register("gps", props -> new GpsItem(props.stacksTo(1).component(LibDataComponents.DESCRIPTION.get(), new ItemDescription(Component.translatable("tooltip.gps.usage").withStyle(ChatFormatting.GRAY)))));

    /**
     * Since 1.21.2 every item has to know its own id before it is constructed, so the properties are
     * built here where the registration name is known. These still register into
     * {@link TechBlocks#ITEMS}, which is the mod's single item registry provider.
     */
    private static <T extends Item> IRegistryObject<T> register(final String name, final Function<Item.Properties, ? extends T> factory) {
        final ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        return TechBlocks.ITEMS.register(name, () -> factory.apply(new Item.Properties().setId(key)));
    }

    static {
        for (ExtruderType type : ExtruderType.values()) {
            // Its level shown as Assorted Storage shows a storage level.
            ItemDescription level = new ItemDescription(Component.translatable("tooltip.extruder.level", Component.literal(String.valueOf(type.getLevel())).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
            EXTRUDERS.put(type, register(type + "_extruder", props -> {
                props.stacksTo(1).component(LibDataComponents.DESCRIPTION.get(), level);
                return new ExtruderItem(type == ExtruderType.NETHERITE ? props.fireResistant() : props, type);
            }));
        }
    }

    public static ExtruderItem extruder(ExtruderType type) {
        return EXTRUDERS.get(type).get();
    }

    public static void init() {
    }
}
