package com.grim3212.assorted.tech.common.inventory;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

public class TechMenuTypes {
    public static final RegistryProvider<MenuType<?>> MENU_TYPES = RegistryProvider.create(Registries.MENU, Constants.MOD_ID);

    public static final IRegistryObject<MenuType<ExtruderMenu>> EXTRUDER = MENU_TYPES.register("extruder", () -> Services.PLATFORM.createMenuType(ExtruderMenu::new, ExtruderType.STREAM_CODEC));
    public static final IRegistryObject<MenuType<GpsSensorMenu>> GPS_SENSOR = MENU_TYPES.register("gps_sensor", () -> Services.PLATFORM.createMenuType(GpsSensorMenu::new, BlockPos.STREAM_CODEC));

    public static void init() {
    }
}
