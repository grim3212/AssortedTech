package com.grim3212.assorted.tech.common.block;

import com.google.common.collect.Lists;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.api.util.GravityType;
import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.api.util.SpikeType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class TechBlocks {

    public static final RegistryProvider<Block> BLOCKS = RegistryProvider.create(Registries.BLOCK, Constants.MOD_ID);
    public static final RegistryProvider<Item> ITEMS = RegistryProvider.create(Registries.ITEM, Constants.MOD_ID);

    public static final IRegistryObject<FlipFlopTorchBlock> FLIP_FLOP_TORCH = registerNoItem("flip_flop_torch", props -> new FlipFlopTorchBlock(props.pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, getter, pos) -> false).noCollision().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
    public static final IRegistryObject<FlipFlopWallTorchBlock> FLIP_FLOP_WALL_TORCH = registerNoItem("flip_flop_wall_torch", props -> new FlipFlopWallTorchBlock(props.pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, getter, pos) -> false).noCollision().instabreak().lightLevel(litBlockEmission(7)).sound(SoundType.WOOD)));
    public static final IRegistryObject<GlowstoneTorchBlock> GLOWSTONE_TORCH = registerNoItem("glowstone_torch", props -> new GlowstoneTorchBlock(props.pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, getter, pos) -> false).noCollision().instabreak().sound(SoundType.WOOD)));
    public static final IRegistryObject<GlowstoneWallTorchBlock> GLOWSTONE_WALL_TORCH = registerNoItem("glowstone_wall_torch", props -> new GlowstoneWallTorchBlock(props.pushReaction(PushReaction.DESTROY).isRedstoneConductor((state, getter, pos) -> false).noCollision().instabreak().sound(SoundType.WOOD)));

    public static final IRegistryObject<FanBlock> FAN = register("fan", props -> new FanBlock(props.mapColor(MapColor.COLOR_BROWN).instrument(NoteBlockInstrument.BASEDRUM).sound(SoundType.STONE).strength(1.5F, 10F)));
    public static final IRegistryObject<AlarmBlock> ALARM = register("alarm", props -> new AlarmBlock(props.mapColor(MapColor.METAL).sound(SoundType.METAL).lightLevel((b) -> 4).strength(2.0F, 1.0F).requiresCorrectToolForDrops()));

    public static final IRegistryObject<BridgeBlock> BRIDGE = register("bridge", props -> new BridgeBlock(props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(-1.0F, 3600000.0F).dynamicShape().noOcclusion().noLootTable().isValidSpawn((s, g, p, e) -> false)));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_LASER = register("bridge_control_laser", props -> new BridgeControlBlock(BridgeType.LASER, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_ACCEL = register("bridge_control_accel", props -> new BridgeControlBlock(BridgeType.ACCEL, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_TRICK = register("bridge_control_trick", props -> new BridgeControlBlock(BridgeType.TRICK, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_DEATH = register("bridge_control_death", props -> new BridgeControlBlock(BridgeType.DEATH, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<BridgeControlBlock> BRIDGE_CONTROL_GRAVITY = register("bridge_control_gravity", props -> new BridgeControlBlock(BridgeType.GRAVITY, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(1.0F, 1.0F).requiresCorrectToolForDrops()));

    public static final IRegistryObject<GravityBlock> ATTRACTOR = register("attractor", props -> new GravityBlock(GravityType.ATTRACT, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityBlock> REPULSOR = register("repulsor", props -> new GravityBlock(GravityType.REPULSE, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityBlock> GRAVITOR = register("gravitor", props -> new GravityBlock(GravityType.GRAVITATE, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));

    public static final IRegistryObject<GravityDirectionalBlock> ATTRACTOR_DIRECTIONAL = register("attractor_directional", props -> new GravityDirectionalBlock(GravityType.ATTRACT, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityDirectionalBlock> REPULSOR_DIRECTIONAL = register("repulsor_directional", props -> new GravityDirectionalBlock(GravityType.REPULSE, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));
    public static final IRegistryObject<GravityDirectionalBlock> GRAVITOR_DIRECTIONAL = register("gravitor_directional", props -> new GravityDirectionalBlock(GravityType.GRAVITATE, props.mapColor(MapColor.METAL).sound(SoundType.METAL).strength(0.3F, 10.0F).requiresCorrectToolForDrops()));

    public static final List<IRegistryObject<SpikeBlock>> SPIKES = Lists.newArrayList();
    public static final List<IRegistryObject<SensorBlock>> SENSORS = Lists.newArrayList();

    static {
        Stream.of(SpikeType.values()).forEach((type) -> SPIKES.add(register(type.toString() + "_spike",
                props -> new SpikeBlock(props.mapColor(MapColor.METAL).sound(SoundType.METAL).noCollision().strength(1.5F, 10F), type),
                itemProps -> {
                    if (type == SpikeType.NETHERITE) {
                        itemProps.fireResistant();
                    }
                },
                Component.translatable("tooltip.spike.damage", Component.translatable(String.valueOf(type.getDamage())).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY))));
        Stream.of(SensorType.values()).forEach((type) -> SENSORS.add(register(type.toString() + "_sensor",
                props -> new SensorBlock(props.mapColor(type.getMapColor()).sound(type.getSoundType()).strength(1.0F, 10.0F), type),
                itemProps -> {
                },
                Component.translatable("tooltip.sensor.detects." + type.name().toLowerCase()).withStyle(ChatFormatting.GRAY))));
    }

    private static <T extends Block> IRegistryObject<T> register(String name, Function<BlockBehaviour.Properties, ? extends T> factory) {
        return register(name, factory, itemProps -> {
        }, null);
    }

    private static <T extends Block> IRegistryObject<T> register(String name, Function<BlockBehaviour.Properties, ? extends T> factory, Consumer<Item.Properties> itemProperties, Component tooltip) {
        IRegistryObject<T> ret = registerNoItem(name, factory);
        final ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        ITEMS.register(name, () -> {
            Item.Properties props = new Item.Properties().useBlockDescriptionPrefix().setId(key);
            itemProperties.accept(props);
            return tooltip == null ? new BlockItem(ret.get(), props) : new TooltipBlockItem(ret.get(), props, tooltip);
        });
        return ret;
    }

    private static <T extends Block> IRegistryObject<T> registerNoItem(String name, Function<BlockBehaviour.Properties, ? extends T> factory) {
        // Since 1.21.2 every block has to know its own id before it is constructed, so the
        // properties are built here where the registration name is known.
        final ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        return BLOCKS.register(name, () -> factory.apply(BlockBehaviour.Properties.of().setId(key)));
    }

    private static ToIntFunction<BlockState> litBlockEmission(int litLevel) {
        return (state) -> {
            return state.getValue(BlockStateProperties.LIT) ? litLevel : 0;
        };
    }

    /**
     * Carries the spikes' damage line and the sensors' "detects X" line, which a block cannot add.
     * Both are fixed per block, so the component is built once at registration.
     */
    private static class TooltipBlockItem extends BlockItem {

        private final Component tooltip;

        private TooltipBlockItem(Block block, Properties props, Component tooltip) {
            super(block, props);
            this.tooltip = tooltip;
        }

        /**
         * {@code Item.appendHoverText} is marked deprecated in 26.x - tooltips are meant to come
         * from data components implementing {@code TooltipProvider} - but it is still the only per
         * item hook, and vanilla's own items still override it.
         */
        @SuppressWarnings("deprecation")
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, adder, flag);
            adder.accept(this.tooltip);
        }
    }

    public static void init() {
    }
}
