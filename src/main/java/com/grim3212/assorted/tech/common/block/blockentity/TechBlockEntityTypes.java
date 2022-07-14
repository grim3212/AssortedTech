package com.grim3212.assorted.tech.common.block.blockentity;

import com.google.common.collect.Sets;
import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.TechBlocks;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TechBlockEntityTypes {

	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AssortedTech.MODID);

	public static final RegistryObject<BlockEntityType<SensorBlockEntity>> SENSOR = BLOCK_ENTITIES.register("sensor", () -> new BlockEntityType<>(SensorBlockEntity::new, Sets.newHashSet(TechBlocks.SENSORS.stream().map(b -> b.get()).toList()), null));
	public static final RegistryObject<BlockEntityType<FanBlockEntity>> FAN = BLOCK_ENTITIES.register("fan", () -> new BlockEntityType<>(FanBlockEntity::new, Sets.newHashSet(TechBlocks.FAN.get()), null));
	public static final RegistryObject<BlockEntityType<AlarmBlockEntity>> ALARM = BLOCK_ENTITIES.register("alarm", () -> new BlockEntityType<>(AlarmBlockEntity::new, Sets.newHashSet(TechBlocks.FAN.get()), null));

	public static final RegistryObject<BlockEntityType<BridgeBlockEntity>> BRIDGE = BLOCK_ENTITIES.register("bridge", () -> new BlockEntityType<>(BridgeBlockEntity::new, Sets.newHashSet(TechBlocks.BRIDGE.get()), null));
	public static final RegistryObject<BlockEntityType<BridgeControlBlockEntity>> BRIDGE_CONTROL = BLOCK_ENTITIES.register("bridge_control", () -> new BlockEntityType<>(BridgeControlBlockEntity::new, Sets.newHashSet(TechBlocks.BRIDGE_CONTROL_LASER.get(), TechBlocks.BRIDGE_CONTROL_ACCEL.get(), TechBlocks.BRIDGE_CONTROL_TRICK.get(), TechBlocks.BRIDGE_CONTROL_DEATH.get(), TechBlocks.BRIDGE_CONTROL_GRAVITY.get()), null));
	public static final RegistryObject<BlockEntityType<GravityBlockEntity>> GRAVITY = BLOCK_ENTITIES.register("gravity", () -> new BlockEntityType<>(GravityBlockEntity::new, Sets.newHashSet(TechBlocks.ATTRACTOR.get(), TechBlocks.REPULSOR.get(), TechBlocks.GRAVITOR.get()), null));
	public static final RegistryObject<BlockEntityType<GravityDirectionalBlockEntity>> GRAVITY_DIRECTIONAL = BLOCK_ENTITIES.register("gravity_directional", () -> new BlockEntityType<>(GravityDirectionalBlockEntity::new, Sets.newHashSet(TechBlocks.ATTRACTOR_DIRECTIONAL.get(), TechBlocks.REPULSOR_DIRECTIONAL.get(), TechBlocks.GRAVITOR_DIRECTIONAL.get()), null));

}
