package com.grim3212.assorted.tech.common.inventory;

import com.grim3212.assorted.tech.api.util.GpsSensorMode;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorBlockEntity;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A GPS sensor's settings beside the player's inventory. The GPS slot is a real one, holding the
 * GPS whose position the sensor watches.
 */
public class GpsSensorMenu extends AbstractContainerMenu {

    public static final int MODE_BUTTON = 0;
    public static final int SHRINK_BUTTON = 1;
    public static final int GROW_BUTTON = 2;
    public static final int SHOW_BUTTON = 3;
    /** Removing entry i of the upgraded sensor's list is button REMOVE_ENTRY_BUTTON + i. */
    public static final int REMOVE_ENTRY_BUTTON = 100;

    public static final int FILTER_SLOT = 0;
    public static final int GPS_SLOT = 1;
    private static final int PLAYER_START = 2;

    private final GpsSensorBlockEntity sensor;
    private final Container filterItem = new SimpleContainer(1);

    /** The client's copy. The screen reads the rest of the settings from the client's synced block entity. */
    public GpsSensorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof GpsSensorBlockEntity sensor ? sensor : new GpsSensorBlockEntity(pos, TechBlocks.GPS_SENSOR.get().defaultBlockState()));
    }

    public GpsSensorMenu(int id, Inventory inventory, GpsSensorBlockEntity sensor) {
        super(TechMenuTypes.GPS_SENSOR.get(), id);
        this.sensor = sensor;
        this.filterItem.setItem(0, sensor.getFilterItem().copy());

        this.addSlot(new Slot(this.filterItem, 0, 8, 66) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean isActive() {
                return GpsSensorMenu.this.sensor.getMode() == GpsSensorMode.ITEMS;
            }
        });

        this.addSlot(new Slot(sensor.getGpsContainer(), 0, 152, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(TechItems.GPS.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addStandardInventorySlots(inventory, 8, 150);
    }

    public GpsSensorBlockEntity getSensor() {
        return this.sensor;
    }

    /** Every click on the ghost slot except a drag, which {@link #canDragTo} keeps away from it. */
    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        if (slotIndex == FILTER_SLOT && containerInput != ContainerInput.QUICK_CRAFT) {
            if (this.slots.get(FILTER_SLOT).isActive() && (containerInput == ContainerInput.PICKUP || containerInput == ContainerInput.QUICK_MOVE)) {
                this.takeFilterItem(containerInput == ContainerInput.PICKUP ? this.getCarried() : ItemStack.EMPTY);
            }
            return;
        }
        super.clicked(slotIndex, buttonNum, containerInput, player);
    }

    /**
     * Shift clicking moves the GPS between its slot and the inventory. Any other inventory item, or a
     * GPS once the slot is full, in item mode is the same as clicking it onto the ghost slot, and
     * never leaves the inventory.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (index == GPS_SLOT || stack.is(TechItems.GPS.get()) && !this.slots.get(GPS_SLOT).hasItem()) {
            ItemStack moved = stack.copy();
            boolean fits = index == GPS_SLOT ? this.moveItemStackTo(stack, PLAYER_START, this.slots.size(), true) : this.moveItemStackTo(stack, GPS_SLOT, GPS_SLOT + 1, false);
            if (!fits) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return moved;
        }
        if (index >= PLAYER_START && this.slots.get(FILTER_SLOT).isActive()) {
            this.takeFilterItem(stack);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return slot.index != FILTER_SLOT;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack carried, Slot target) {
        return target.index != FILTER_SLOT && super.canTakeItemForPickAll(carried, target);
    }

    /** The upgraded sensor adds the item to its entries, where the client waits for the sync. */
    private void takeFilterItem(ItemStack stack) {
        if (this.sensor.isUpgraded()) {
            if (this.isServer() && !stack.isEmpty()) {
                this.sensor.addEntry(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            }
            return;
        }
        this.filterItem.setItem(0, stack.copyWithCount(1));
        if (this.isServer()) {
            this.sensor.configure(this.sensor.getMode(), this.sensor.getFilter(), stack, this.sensor.getRadius(), this.sensor.isShowingTarget());
        }
    }

    /** Each mode keeps its own filter, so changing mode brings back what the next one was set to. */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        GpsSensorBlockEntity s = this.sensor;
        switch (id) {
            case MODE_BUTTON -> s.configure(s.getMode().next(), s.getFilter(s.getMode().next()), s.getFilterItem(), s.getRadius(), s.isShowingTarget());
            case SHRINK_BUTTON -> s.configure(s.getMode(), s.getFilter(), s.getFilterItem(), s.getRadius() - 1, s.isShowingTarget());
            case GROW_BUTTON -> s.configure(s.getMode(), s.getFilter(), s.getFilterItem(), s.getRadius() + 1, s.isShowingTarget());
            case SHOW_BUTTON -> s.configure(s.getMode(), s.getFilter(), s.getFilterItem(), s.getRadius(), !s.isShowingTarget());
            default -> {
                if (id < REMOVE_ENTRY_BUTTON || id >= REMOVE_ENTRY_BUTTON + s.getEntries().size()) {
                    return false;
                }
                s.removeEntry(id - REMOVE_ENTRY_BUTTON);
            }
        }
        return true;
    }

    /** From {@code GpsSensorFilterPacket}, as the player types. */
    public void setFilterText(String filter) {
        this.sensor.configure(this.sensor.getMode(), filter, this.sensor.getFilterItem(), this.sensor.getRadius(), this.sensor.isShowingTarget());
    }

    /** From {@code GpsSensorFilterPacket}, when the player adds what they typed to the upgraded sensor's list. */
    public void addEntry(String entry) {
        this.sensor.addEntry(entry);
    }

    /** Another player may have changed the item since this menu opened; the server's slot follows the sensor. */
    @Override
    public void broadcastChanges() {
        this.filterItem.setItem(0, this.sensor.getFilterItem().copy());
        super.broadcastChanges();
    }

    private boolean isServer() {
        return this.sensor.getLevel() != null && !this.sensor.getLevel().isClientSide();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this.sensor, player);
    }
}
