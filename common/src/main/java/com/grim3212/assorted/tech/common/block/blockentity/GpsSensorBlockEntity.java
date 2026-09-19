package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.lib.core.inventory.IMenuDataProvider;
import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.GpsSensorMode;
import com.grim3212.assorted.tech.common.block.GpsSensorBlock;
import com.grim3212.assorted.tech.common.inventory.GpsSensorMenu;
import com.grim3212.assorted.tech.common.item.GpsTarget;
import com.grim3212.assorted.tech.common.item.TechDataComponents;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Watches the position stored on the GPS in its slot, rather than the space in front of it, for a
 * player, a mob or an item, optionally a particular one. The upgraded sensor reaches further, can widen the watched box
 * around its position, and filters by a list of entries, tags included, in place of the one typed
 * name or id or the ghost item (see {@link GpsSensorFilter}). Each mode keeps its own filter, so
 * switching modes and back loses nothing.
 */
public class GpsSensorBlockEntity extends BlockEntity implements IMenuDataProvider<BlockPos> {

    /** Filter text is typed by players and sent to the server, so it is bounded, as is the upgraded list. */
    public static final int MAX_FILTER_LENGTH = 64;
    public static final int MAX_ENTRIES = 6;
    private static final Codec<Map<GpsSensorMode, String>> FILTERS_CODEC = Codec.unboundedMap(GpsSensorMode.CODEC, Codec.STRING);
    private static final Codec<Map<GpsSensorMode, List<String>>> ENTRIES_CODEC = Codec.unboundedMap(GpsSensorMode.CODEC, Codec.STRING.listOf(0, MAX_ENTRIES));

    /**
     * The GPS whose stored position it watches; taking it out leaves the sensor watching nothing.
     * The client needs it too, for the status line and the outline, so every change is sent.
     */
    private final SimpleContainer gps = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            GpsSensorBlockEntity.this.markUpdated();
        }
    };
    private GpsSensorMode mode = GpsSensorMode.PLAYERS;
    private final Map<GpsSensorMode, String> filters = new EnumMap<>(GpsSensorMode.class);
    private final Map<GpsSensorMode, List<String>> entries = new EnumMap<>(GpsSensorMode.class);
    private @Nullable GpsSensorFilter parsedFilter;
    private ItemStack filterItem = ItemStack.EMPTY;
    private int radius = 0;
    private boolean showTarget = false;
    private int signal = 0;

    public GpsSensorBlockEntity(BlockPos pos, BlockState state) {
        super(TechBlockEntityTypes.GPS_SENSOR.get(), pos, state);
    }

    public enum TargetStatus {
        NONE, GOOD, BLOCKED, OUT_OF_RANGE, OTHER_DIMENSION
    }

    public Container getGpsContainer() {
        return this.gps;
    }

    /** The GPS's stored position, if it has one; {@link #targetStatus} says whether it can be watched. */
    private @Nullable GlobalPos storedPosition() {
        GpsTarget stored = this.gps.getItem(0).get(TechDataComponents.GPS_TARGET.get());
        return stored == null ? null : stored.target();
    }

    /**
     * A plain sensor gives out 15 while it sees anything; an upgraded one as much as it sees, up to
     * 15, counting items rather than the dropped stacks they are in.
     */
    public void tick() {
        int seen = 0;
        if (this.targetStatus() == TargetStatus.GOOD) {
            for (Entity entity : this.level.getEntities((Entity) null, this.senseBox(), entity -> EntitySelector.NO_SPECTATORS.test(entity) && this.matches(entity))) {
                seen += entity instanceof ItemEntity item ? item.getItem().getCount() : 1;
            }
        }
        int signal = seen == 0 ? 0 : this.isUpgraded() ? Math.min(15, seen) : 15;

        BlockState state = this.getBlockState();
        boolean changed = signal != this.signal;
        this.signal = signal;
        if (state.getValue(GpsSensorBlock.ACTIVE) != signal > 0) {
            // Updates the neighbours itself, and they read the new signal.
            this.level.setBlock(this.getBlockPos(), state.setValue(GpsSensorBlock.ACTIVE, signal > 0), 3);
        } else if (changed) {
            this.level.updateNeighborsAt(this.getBlockPos(), state.getBlock());
        }
    }

    /** The redstone signal it gives out, worked out on the server each tick. */
    public int getSignal() {
        return this.signal;
    }

    public boolean isUpgraded() {
        return this.getBlockState().getBlock() instanceof GpsSensorBlock sensor && sensor.isUpgraded();
    }

    /** How far the watched position may be from the sensor, in blocks. */
    public int getRange() {
        return this.isUpgraded() ? TechCommonMod.COMMON_CONFIG.upgradedGpsSensorRange.get() : TechCommonMod.COMMON_CONFIG.gpsSensorRange.get();
    }

    public int getMaxRadius() {
        return this.isUpgraded() ? TechCommonMod.COMMON_CONFIG.upgradedGpsSensorMaxRadius.get() : 0;
    }

    public boolean isInRange(BlockPos pos) {
        return pos.distSqr(this.getBlockPos()) <= (double) this.getRange() * this.getRange();
    }

    /** Whether the GPS in the slot has a position here, close enough, and somewhere an entity could be. */
    public TargetStatus targetStatus() {
        GlobalPos stored = this.storedPosition();
        if (stored == null || this.level == null) {
            return TargetStatus.NONE;
        }
        if (stored.dimension() != this.level.dimension()) {
            return TargetStatus.OTHER_DIMENSION;
        }
        BlockPos target = stored.pos();
        if (!this.isInRange(target)) {
            return TargetStatus.OUT_OF_RANGE;
        }
        return this.level.getBlockState(target).isCollisionShapeFullBlock(this.level, target) ? TargetStatus.BLOCKED : TargetStatus.GOOD;
    }

    /** The watched box in world space; only meaningful with a target. */
    public AABB senseBox() {
        BlockPos target = this.getTarget();
        return new AABB(target == null ? this.getBlockPos() : target).inflate(this.radius);
    }

    /** A plain sensor's items are narrowed by the ghost item; an upgraded one's by its entries alone. */
    public boolean matches(Entity entity) {
        GpsSensorFilter typed = this.parsedFilter();
        return switch (this.mode) {
            case PLAYERS -> entity instanceof Player player && (typed.isEmpty() || typed.matchesPlayer(player.getGameProfile().name()));
            case MOBS -> entity instanceof LivingEntity && !(entity instanceof Player) && (typed.isEmpty() || typed.matchesMob(entity.typeHolder()));
            case ITEMS -> entity instanceof ItemEntity item && (this.isUpgraded()
                    ? typed.isEmpty() || typed.matchesItem(item.getItem().typeHolder())
                    : this.filterItem.isEmpty() || ItemStack.isSameItem(item.getItem(), this.filterItem));
        };
    }

    private GpsSensorFilter parsedFilter() {
        if (this.parsedFilter == null) {
            this.parsedFilter = this.isUpgraded() ? GpsSensorFilter.of(this.getEntries()) : GpsSensorFilter.single(this.getFilter());
        }
        return this.parsedFilter;
    }

    /** The GPS's stored position, whatever its dimension. */
    public @Nullable BlockPos getTarget() {
        GlobalPos stored = this.storedPosition();
        return stored == null ? null : stored.pos();
    }

    public GpsSensorMode getMode() {
        return this.mode;
    }

    /** The plain sensor's typed filter for the current mode. */
    public String getFilter() {
        return this.getFilter(this.mode);
    }

    public String getFilter(GpsSensorMode mode) {
        return this.filters.getOrDefault(mode, "");
    }

    /** The upgraded sensor's filter entries for the current mode, in the order they were added. */
    public List<String> getEntries() {
        return Collections.unmodifiableList(this.entries.getOrDefault(this.mode, List.of()));
    }

    /**
     * Adds an entry to the upgraded sensor's list for the current mode, unless it is already there,
     * the list is full, or it could never match. Arrives from a client, so it is checked again here.
     */
    public boolean addEntry(String entry) {
        String normalized = GpsSensorFilter.normalize(this.mode, entry);
        List<String> current = this.getEntries();
        if (!this.isUpgraded() || normalized.isEmpty() || normalized.length() > MAX_FILTER_LENGTH || current.size() >= MAX_ENTRIES
                || current.stream().anyMatch(normalized::equalsIgnoreCase) || GpsSensorFilter.problem(this.mode, normalized, true) != null) {
            return false;
        }
        this.entries.computeIfAbsent(this.mode, m -> new ArrayList<>()).add(normalized);
        this.parsedFilter = null;
        this.markUpdated();
        return true;
    }

    public void removeEntry(int index) {
        List<String> current = this.entries.get(this.mode);
        if (current != null && index >= 0 && index < current.size()) {
            current.remove(index);
            this.parsedFilter = null;
            this.markUpdated();
        }
    }

    public ItemStack getFilterItem() {
        return this.filterItem;
    }

    public int getRadius() {
        return this.radius;
    }

    public boolean isShowingTarget() {
        return this.showTarget;
    }

    /**
     * Everything the sensor screen edits but the entry list, clamped here since it arrives from a
     * client. The filter text is kept for the given mode, beside the other modes' own.
     */
    public void configure(GpsSensorMode mode, String filter, ItemStack filterItem, int radius, boolean showTarget) {
        this.mode = mode;
        String stripped = filter.strip();
        this.filters.put(mode, stripped.length() > MAX_FILTER_LENGTH ? stripped.substring(0, MAX_FILTER_LENGTH) : stripped);
        this.parsedFilter = null;
        this.filterItem = filterItem.copyWithCount(1);
        this.radius = Math.clamp(radius, 0, this.getMaxRadius());
        this.showTarget = showTarget;
        this.markUpdated();
    }

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    /** The client's menu finds this sensor by position, for the settings the screen shows. */
    @Override
    public BlockPos getMenuData(ServerPlayer player) {
        return this.getBlockPos();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GpsSensorMenu(id, inventory, this);
    }

    /** The GPS is the sensor's to hold, not to keep, so it comes back out when the sensor is broken. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.gps);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        // Straight into the list, since setItem would send an update mid-load.
        this.gps.getItems().set(0, input.read("Gps", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        this.mode = input.read("Mode", GpsSensorMode.CODEC).orElse(GpsSensorMode.PLAYERS);
        this.filters.clear();
        this.filters.putAll(input.read("Filters", FILTERS_CODEC).orElse(Map.of()));
        this.entries.clear();
        input.read("Entries", ENTRIES_CODEC).orElse(Map.of()).forEach((mode, list) -> this.entries.put(mode, new ArrayList<>(list)));
        this.parsedFilter = null;
        this.filterItem = input.read("FilterItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.radius = input.getIntOr("Radius", 0);
        this.showTarget = input.getBooleanOr("ShowTarget", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Gps", ItemStack.OPTIONAL_CODEC, this.gps.getItem(0));
        output.store("Mode", GpsSensorMode.CODEC, this.mode);
        output.store("Filters", FILTERS_CODEC, this.filters);
        output.store("Entries", ENTRIES_CODEC, this.entries);
        output.store("FilterItem", ItemStack.OPTIONAL_CODEC, this.filterItem);
        output.putInt("Radius", this.radius);
        output.putBoolean("ShowTarget", this.showTarget);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }
}
