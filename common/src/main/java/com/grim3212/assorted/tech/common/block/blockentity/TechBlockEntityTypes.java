package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TechBlockEntityTypes {

    public static final RegistryProvider<BlockEntityType<?>> BLOCK_ENTITIES = RegistryProvider.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static final IRegistryObject<BlockEntityType<SensorBlockEntity>> SENSOR = BLOCK_ENTITIES.register("sensor", () -> Services.REGISTRY_UTIL.createBlockEntityType(SensorBlockEntity::new, TechBlocks.SENSORS.stream().map(x -> x.get()).toArray(Block[]::new)));
    public static final IRegistryObject<BlockEntityType<FanBlockEntity>> FAN = BLOCK_ENTITIES.register("fan", () -> Services.REGISTRY_UTIL.createBlockEntityType(FanBlockEntity::new, TechBlocks.FAN.get()));
    public static final IRegistryObject<BlockEntityType<AlarmBlockEntity>> ALARM = BLOCK_ENTITIES.register("alarm", () -> Services.REGISTRY_UTIL.createBlockEntityType(AlarmBlockEntity::new, TechBlocks.FAN.get()));

    public static final IRegistryObject<BlockEntityType<BridgeBlockEntity>> BRIDGE = BLOCK_ENTITIES.register("bridge", () -> Services.REGISTRY_UTIL.createBlockEntityType(BridgeBlockEntity::new, TechBlocks.BRIDGE.get()));
    public static final IRegistryObject<BlockEntityType<BridgeControlBlockEntity>> BRIDGE_CONTROL = BLOCK_ENTITIES.register("bridge_control", () -> Services.REGISTRY_UTIL.createBlockEntityType(BridgeControlBlockEntity::new, TechBlocks.BRIDGE_CONTROL_LASER.get(), TechBlocks.BRIDGE_CONTROL_ACCEL.get(), TechBlocks.BRIDGE_CONTROL_TRICK.get(), TechBlocks.BRIDGE_CONTROL_DEATH.get(), TechBlocks.BRIDGE_CONTROL_GRAVITY.get()));
    public static final IRegistryObject<BlockEntityType<GravityBlockEntity>> GRAVITY = BLOCK_ENTITIES.register("gravity", () -> Services.REGISTRY_UTIL.createBlockEntityType(GravityBlockEntity::new, TechBlocks.ATTRACTOR.get(), TechBlocks.REPULSOR.get(), TechBlocks.GRAVITOR.get()));
    public static final IRegistryObject<BlockEntityType<GravityDirectionalBlockEntity>> GRAVITY_DIRECTIONAL = BLOCK_ENTITIES.register("gravity_directional", () -> Services.REGISTRY_UTIL.createBlockEntityType(GravityDirectionalBlockEntity::new, TechBlocks.ATTRACTOR_DIRECTIONAL.get(), TechBlocks.REPULSOR_DIRECTIONAL.get(), TechBlocks.GRAVITOR_DIRECTIONAL.get()));

    public static void init() {
    }
}
