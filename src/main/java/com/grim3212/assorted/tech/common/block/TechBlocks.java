package com.grim3212.assorted.tech.common.block;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.item.EnabledBlockItem;
import com.grim3212.assorted.tech.common.item.TechItems;
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
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TechBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AssortedTech.MODID);
	public static final DeferredRegister<Item> ITEMS = TechItems.ITEMS;

	public static final RegistryObject<FlipFlopTorchBlock> FLIP_FLOP_TORCH = registerNoItem("flip_flop_torch", () -> new FlipFlopTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
	public static final RegistryObject<FlipFlopWallTorchBlock> FLIP_FLOP_WALL_TORCH = registerNoItem("flip_flop_wall_torch", () -> new FlipFlopWallTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
	public static final RegistryObject<GlowstoneTorchBlock> GLOWSTONE_TORCH = registerNoItem("glowstone_torch", () -> new GlowstoneTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD)));
	public static final RegistryObject<GlowstoneWallTorchBlock> GLOWSTONE_WALL_TORCH = registerNoItem("glowstone_wall_torch", () -> new GlowstoneWallTorchBlock(Properties.of(Material.DECORATION).noCollission().instabreak().sound(SoundType.WOOD)));

	public static final List<RegistryObject<SpikeBlock>> SPIKES = Lists.newArrayList();
	public static final List<RegistryObject<SensorBlock>> SENSORS = Lists.newArrayList();

	static {
		Stream.of(SpikeType.values()).forEach((type) -> SPIKES.add(register(type.toString() + "_spike", EnabledCondition.SPIKES_CONDITION, () -> new SpikeBlock(Properties.of(Material.METAL).sound(SoundType.METAL).noCollission().strength(1.5F, 10F), type))));
		Stream.of(SensorType.values()).forEach((type) -> SENSORS.add(register(type.toString() + "_sensor", EnabledCondition.SENSORS_CONDITION, () -> new SensorBlock(Properties.of(type.getMaterial()).sound(type.getSoundType()).noCollission().strength(1.0F, 10.0F), type))));
	}

	private static <T extends Block> RegistryObject<T> register(String name, String enabledCondition, Supplier<? extends T> sup) {
		return register(name, sup, block -> item(block, enabledCondition));
	}

	private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup, Function<RegistryObject<T>, Supplier<? extends Item>> itemCreator) {
		RegistryObject<T> ret = registerNoItem(name, sup);
		ITEMS.register(name, itemCreator.apply(ret));
		return ret;
	}

	private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<? extends T> sup) {
		return BLOCKS.register(name, sup);
	}

	private static Supplier<BlockItem> item(final RegistryObject<? extends Block> block, final String enabledCondition) {
		return () -> new EnabledBlockItem(block.get(), new Item.Properties().tab(AssortedTech.ASSORTED_TECH_ITEM_GROUP), enabledCondition);
	}

	private static ToIntFunction<BlockState> litBlockEmission(int litLevel) {
		return (state) -> {
			return state.getValue(BlockStateProperties.LIT) ? litLevel : 0;
		};
	}
}
