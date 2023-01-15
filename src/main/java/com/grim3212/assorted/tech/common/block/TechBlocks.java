package com.grim3212.assorted.tech.common.block;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.util.BridgeType;
import com.grim3212.assorted.tech.common.util.GravityType;
import com.grim3212.assorted.tech.common.util.SensorType;
import com.grim3212.assorted.tech.common.util.SpikeType;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TechBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AssortedTech.MODID);
	public static final DeferredRegister<Item> ITEMS = TechItems.ITEMS;

	public static final RegistryObject<FlipFlopTorchBlock> FLIP_FLOP_TORCH = registerNoItem("flip_flop_torch", () -> new FlipFlopTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
	public static final RegistryObject<FlipFlopWallTorchBlock> FLIP_FLOP_WALL_TORCH = registerNoItem("flip_flop_wall_torch", () -> new FlipFlopWallTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
	public static final RegistryObject<GlowstoneTorchBlock> GLOWSTONE_TORCH = registerNoItem("glowstone_torch", () -> new GlowstoneTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD)));
	public static final RegistryObject<GlowstoneWallTorchBlock> GLOWSTONE_WALL_TORCH = registerNoItem("glowstone_wall_torch", () -> new GlowstoneWallTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD)));

	public static final RegistryObject<FanBlock> FAN = register("fan", () -> new FanBlock(Properties.of(Material.STONE).sound(SoundType.STONE).strength(1.5F, 10F)));
	public static final RegistryObject<AlarmBlock> ALARM = register("alarm", () -> new AlarmBlock(Properties.of(Material.METAL).sound(SoundType.METAL).lightLevel((b) -> 4).strength(2.0F, 1.0F).requiresCorrectToolForDrops()));

	public static final RegistryObject<BridgeBlock> BRIDGE = register("bridge", () -> new BridgeBlock(Properties.of(Material.METAL).sound(SoundType.METAL).strength(-1.0F, 3600000.0F).dynamicShape().noOcclusion().noLootTable().isValidSpawn((s, g, p, e) -> false)));
	public static final RegistryObject<BridgeControlBlock> BRIDGE_CONTROL_LASER = register("bridge_control_laser", () -> new BridgeControlBlock(BridgeType.LASER, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<BridgeControlBlock> BRIDGE_CONTROL_ACCEL = register("bridge_control_accel", () -> new BridgeControlBlock(BridgeType.ACCEL, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<BridgeControlBlock> BRIDGE_CONTROL_TRICK = register("bridge_control_trick", () -> new BridgeControlBlock(BridgeType.TRICK, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<BridgeControlBlock> BRIDGE_CONTROL_DEATH = register("bridge_control_death", () -> new BridgeControlBlock(BridgeType.DEATH, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<BridgeControlBlock> BRIDGE_CONTROL_GRAVITY = register("bridge_control_gravity", () -> new BridgeControlBlock(BridgeType.GRAVITY, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));

	public static final RegistryObject<GravityBlock> ATTRACTOR = register("attractor", () -> new GravityBlock(GravityType.ATTRACT, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<GravityBlock> REPULSOR = register("repulsor", () -> new GravityBlock(GravityType.REPULSE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<GravityBlock> GRAVITOR = register("gravitor", () -> new GravityBlock(GravityType.GRAVITATE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));

	public static final RegistryObject<GravityDirectionalBlock> ATTRACTOR_DIRECTIONAL = register("attractor_directional", () -> new GravityDirectionalBlock(GravityType.ATTRACT, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<GravityDirectionalBlock> REPULSOR_DIRECTIONAL = register("repulsor_directional", () -> new GravityDirectionalBlock(GravityType.REPULSE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
	public static final RegistryObject<GravityDirectionalBlock> GRAVITOR_DIRECTIONAL = register("gravitor_directional", () -> new GravityDirectionalBlock(GravityType.GRAVITATE, Block.Properties.of(Material.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));

	public static final List<RegistryObject<SpikeBlock>> SPIKES = Lists.newArrayList();
	public static final List<RegistryObject<SensorBlock>> SENSORS = Lists.newArrayList();

	static {
		Stream.of(SpikeType.values()).forEach((type) -> SPIKES.add(register(type.toString() + "_spike", () -> new SpikeBlock(Properties.of(Material.METAL).sound(SoundType.METAL).noCollission().strength(1.5F, 10F), type))));
		Stream.of(SensorType.values()).forEach((type) -> SENSORS.add(register(type.toString() + "_sensor", () -> new SensorBlock(Properties.of(type.getMaterial()).sound(type.getSoundType()).strength(1.0F, 10.0F), type))));
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
		return register(name, sup, block -> item(block));
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup, Function<RegistryObject<T>, Supplier<? extends Item>> itemCreator) {
		RegistryObject<T> ret = registerNoItem(name, sup);
		ITEMS.register(name, itemCreator.apply(ret));
		return ret;
	}

	private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<? extends T> sup) {
		return BLOCKS.register(name, sup);
	}

	private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block) {
		return () -> new BlockItem(block.get(), new Item.Properties());
	}

	private static ToIntFunction<BlockState> litBlockEmission(int litLevel) {
		return (state) -> {
			return state.getValue(BlockStateProperties.LIT) ? litLevel : 0;
		};
	}
}
