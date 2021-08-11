package com.grim3212.assorted.tech.common.block.blockentity;

import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.TechBlocks;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TechBlockEntityTypes {

	public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, AssortedTech.MODID);

	public static final RegistryObject<TileEntityType<SensorBlockEntity>> SENSOR = BLOCK_ENTITIES.register("sensor", () -> new TileEntityType<>(SensorBlockEntity::new, Sets.newHashSet(TechBlocks.SENSORS.stream().map(b -> b.get()).collect(Collectors.toList())), null));
	public static final RegistryObject<TileEntityType<FanBlockEntity>> FAN = BLOCK_ENTITIES.register("fan", () -> new TileEntityType<>(FanBlockEntity::new, Sets.newHashSet(TechBlocks.FAN.get()), null));

}
