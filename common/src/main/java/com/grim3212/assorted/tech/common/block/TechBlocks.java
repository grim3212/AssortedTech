package com.grim3212.assorted.tech.common.block;

import com.google.common.collect.Lists;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.api.util.GravityType;
import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.api.util.SpikeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class TechBlocks {

    public static final RegistryProvider<Block> BLOCKS = RegistryProvider.create(Registries.BLOCK, Constants.MOD_ID);
    public static final RegistryProvider<Item> ITEMS = RegistryProvider.create(Registries.ITEM, Constants.MOD_ID);

    public static final IRegistryObject<FlipFlopTorchBlock> FLIP_FLOP_TORCH = registerNoItem("flip_flop_torch", () -> new FlipFlopTorchBlock(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
    public static final IRegistryObject<FlipFlopWallTorchBlock> FLIP_FLOP_WALL_TORCH = registerNoItem("flip_flop_wall_torch", () -> new FlipFlopWallTorchBlock(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
    public static final IRegistryObject<GlowstoneTorchBlock> GLOWSTONE_TORCH = registerNoItem("glowstone_torch", () -> new GlowstoneTorchBlock(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD)));
    public static final IRegistryObject<GlowstoneWallTorchBlock> GLOWSTONE_WALL_TORCH = registerNoItem("glowstone_wall_torch", () -> new GlowstoneWallTorchBlock(BlockBehaviour.Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD)));

    public static final IRegistryObject<FanBlock> FAN = register("fan", () -> new FanBlock(BlockBehaviour.Properties.of(Material.STONE).sound(SoundType.STONE).strength(1.5F, 10F)));
    public static final IRegistryObject<AlarmBlock> ALARM = register("alarm", () -> new AlarmBlock(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).lightLevel((b) -> 4).strength(2.0F, 1.0F).requiresCorrectToolForDrops()));

    public static final IRegistryObject<BridgeBlock> BRIDGE = register("bridge", () -> new BridgeBlock(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(-1.0F, 3600000.0F).dynamicShape().noOcclusion().noLootTable().isValidSpawn((s, g, p, e) -> false)));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_LASER = register("bridge_control_laser", () -> new BridgeControlBlock(BridgeType.LASER, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_ACCEL = register("bridge_control_accel", () -> new BridgeControlBlock(BridgeType.ACCEL, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_TRICK = register("bridge_control_trick", () -> new BridgeControlBlock(BridgeType.TRICK, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_DEATH = register("bridge_control_death", () -> new BridgeControlBlock(BridgeType.DEATH, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_GRAVITY = register("bridge_control_gravity", () -> new BridgeControlBlock(BridgeType.GRAVITY, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));

    public static final IRegistryObject<GravityBlock> ATTRACTOR = register("attractor", () -> new GravityBlock(GravityType.ATTRACT, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityBlock> REPULSOR = register("repulsor", () -> new GravityBlock(GravityType.REPULSE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityBlock> GRAVITOR = register("gravitor", () -> new GravityBlock(GravityType.GRAVITATE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));

    public static final IRegistryObject<GravityDirectionalBlock> ATTRACTOR_DIRECTIONAL = register("attractor_directional", () -> new GravityDirectionalBlock(GravityType.ATTRACT, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityDirectionalBlock> REPULSOR_DIRECTIONAL = register("repulsor_directional", () -> new GravityDirectionalBlock(GravityType.REPULSE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityDirectionalBlock> GRAVITOR_DIRECTIONAL = register("gravitor_directional", () -> new GravityDirectionalBlock(GravityType.GRAVITATE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));

    public static final List<IRegistryObject<SpikeBlock>> SPIKES = Lists.newArrayList();
    public static final List<IRegistryObject<SensorBlock>> SENSORS = Lists.newArrayList();

    static {
        Stream.of(SpikeType.values()).forEach((type) -> SPIKES.add(register(type.toString() + "_spike", () -> new SpikeBlock(Block.Properties.of(Material.METAL).sound(SoundType.METAL).noCollission().strength(1.5F, 10F), type))));
        Stream.of(SensorType.values()).forEach((type) -> SENSORS.add(register(type.toString() + "_sensor", () -> new SensorBlock(Block.Properties.of(type.getMaterial()).sound(type.getSoundType()).strength(1.0F, 10.0F), type))));
    }

    private static <T extends Block> IRegistryObject<T> register(String name, Supplier<? extends T> sup) {
        return register(name, sup, block -> item(block));
    }

    private static <T extends Block> IRegistryObject<T> register(String name, Supplier<? extends T> sup, Function<IRegistryObject<T>, Supplier<? extends Item>> itemCreator) {
        IRegistryObject<T> ret = registerNoItem(name, sup);
        ITEMS.register(name, itemCreator.apply(ret));
        return ret;
    }

    private static <T extends Block> IRegistryObject<T> registerNoItem(String name, Supplier<? extends T> sup) {
        return BLOCKS.register(name, sup);
    }

    private static Supplier<BlockItem> item(final IRegistryObject<? extends Block> block) {
        return () -> new BlockItem(block.get(), new Item.Properties());
    }

    private static ToIntFunction<BlockState> litBlockEmission(int litLevel) {
        return (state) -> {
            return state.getValue(BlockStateProperties.LIT) ? litLevel : 0;
        };
    }

    public static void init() {
    }
}
