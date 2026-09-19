package com.grim3212.assorted.tech.api.util;

import com.grim3212.assorted.lib.util.LibCommonTags;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.IntFunction;

/**
 * What an extruder is made of, one per spike material. It is crafted from a pickaxe, shovel and axe
 * of its material, which also decide what it can mine. Its level, the harvest level of those tools,
 * sets how many blocks it lays from and mines into, how fast it drives and how little fuel each
 * block costs, and every
 * level above the first is crafted from an extruder of the level below.
 */
public enum ExtruderType implements StringRepresentable {
    // Vanilla tools
    WOOD("wood", Tier.WOOD),
    STONE("stone", Tier.STONE),
    COPPER("copper", Tier.STONE),
    IRON("iron", Tier.IRON),
    GOLD("gold", Tier.WOOD),
    DIAMOND("diamond", Tier.DIAMOND),
    NETHERITE("netherite", Tier.NETHERITE),

    // Assorted Tools tools, tiered by their harvest level
    AMETHYST("amethyst", Tier.IRON),
    EMERALD("emerald", Tier.DIAMOND),
    TIN("tin", Tier.STONE),
    SILVER("silver", Tier.DIAMOND),
    ALUMINUM("aluminum", Tier.STONE),
    NICKEL("nickel", Tier.IRON),
    PLATINUM("platinum", Tier.DIAMOND),
    LEAD("lead", Tier.IRON),
    BRONZE("bronze", Tier.IRON),
    ELECTRUM("electrum", Tier.DIAMOND),
    INVAR("invar", Tier.IRON),
    STEEL("steel", Tier.DIAMOND),
    RUBY("ruby", Tier.IRON),
    SAPPHIRE("sapphire", Tier.IRON),
    TOPAZ("topaz", Tier.IRON),
    PERIDOT("peridot", Tier.IRON);

    public static final StringRepresentable.EnumCodec<ExtruderType> CODEC = StringRepresentable.fromEnum(ExtruderType::values);
    private static final IntFunction<ExtruderType> BY_ID = ByIdMap.continuous(ExtruderType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ExtruderType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ExtruderType::ordinal);

    private final String name;
    private final Tier tier;
    private final TagKey<Item> pickaxes;
    private final TagKey<Item> shovels;
    private final TagKey<Item> axes;

    ExtruderType(String name, Tier tier) {
        this.name = name;
        this.tier = tier;
        this.pickaxes = toolTag("pickaxes", name);
        this.shovels = toolTag("shovels", name);
        this.axes = toolTag("axes", name);
    }

    private static TagKey<Item> toolTag(String kind, String material) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LibCommonTags.COMMON_NAMESPACE, kind + "/" + material));
    }

    public static ExtruderType byId(int id) {
        return BY_ID.apply(id);
    }

    public TagKey<Item> getPickaxes() {
        return this.pickaxes;
    }

    public TagKey<Item> getShovels() {
        return this.shovels;
    }

    public TagKey<Item> getAxes() {
        return this.axes;
    }

    /** Whether one of its tool tags is empty, as the Assorted Tools ones are without Assorted Tools. */
    public boolean isUncraftable() {
        return this.getTools().stream().anyMatch(ItemStack::isEmpty);
    }

    /** How many slots it lays blocks from, in the row under its fuel. */
    public int getExtrudeSlots() {
        return this.tier.extrudeSlots;
    }

    /** From 0 for wood and gold up to 4 for netherite, as {@link Tier} orders them. */
    public int getLevel() {
        return this.tier.ordinal();
    }

    /**
     * The fuel it spends on a block it mines or lays, given the configured cost: all of it at level
     * 0, an eighth less each level, down to half at netherite. The fuel it spends travelling is the
     * same for every level.
     */
    public int blockCost(int configured) {
        return Math.round(configured * (1.0F - 0.125F * this.getLevel()));
    }

    /**
     * How fast it drives, against the configured move speed: three quarters at level 0 and a quarter
     * more each level, all taken down by a quarter, so from 0.5625 up to 1.3125 at netherite.
     */
    public float getSpeed() {
        return (0.75F + 0.25F * this.getLevel()) * 0.75F;
    }

    /** How many slots it mines into, in whole rows of nine. */
    public int getMinedSlots() {
        return this.tier.minedRows * 9;
    }

    /**
     * One of each of its tools, the first in each tag; empty where a tag has none, as with an Assorted
     * Tools material when Assorted Tools is not installed. Looked up each time, since tags reload.
     */
    public List<ItemStack> getTools() {
        return List.of(first(this.pickaxes), first(this.shovels), first(this.axes));
    }

    private static ItemStack first(TagKey<Item> tag) {
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }

    /**
     * The tool it mines {@code state} with: one of its three that is right for the block, or, for a
     * block that needs no particular tool, its pickaxe. Empty when none of its tools can take the
     * block's drops, which is when it cannot mine it.
     */
    public ItemStack toolFor(BlockState state) {
        List<ItemStack> tools = this.getTools();
        for (ItemStack tool : tools) {
            if (!tool.isEmpty() && tool.isCorrectToolForDrops(state)) {
                return tool;
            }
        }
        return state.requiresCorrectToolForDrops() || tools.getFirst().isEmpty() ? ItemStack.EMPTY : tools.getFirst();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * The five vanilla harvest levels, in order, which are an extruder's levels. They set how big its
     * inventory is; diamond and netherite have every slot.
     */
    private enum Tier {
        WOOD(3, 1),
        STONE(5, 1),
        IRON(6, 2),
        DIAMOND(9, 3),
        NETHERITE(9, 3);

        private final int extrudeSlots;
        private final int minedRows;

        Tier(int extrudeSlots, int minedRows) {
            this.extrudeSlots = extrudeSlots;
            this.minedRows = minedRows;
        }
    }
}
