package com.grim3212.assorted.tech.common.block.blockentity;

import com.google.common.collect.Sets;
import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.TechBlocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TechBlockEntityTypes {

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, AssortedTech.MODID);

	public static final RegistryObject<BlockEntityType<SensorBlockEntity>> SENSOR = BLOCK_ENTITIES.register("sensor", () -> new BlockEntityType<>(SensorBlockEntity::new, Sets.newHashSet(TechBlocks.SENSORS.stream().map(b -> b.get()).toList()), null));
	public static final RegistryObject<BlockEntityType<FanBlockEntity>> FAN = BLOCK_ENTITIES.register("fan", () -> new BlockEntityType<>(FanBlockEntity::new, Sets.newHashSet(TechBlocks.FAN.get()), null));

}
