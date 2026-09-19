package com.grim3212.assorted.tech.common.inventory;

import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.common.entity.ExtruderEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * The extruder's fuel slot, the slots it lays blocks from, and those it mines into, as many of each
 * as its {@link ExtruderType} has. Its direction and start/stop buttons go through vanilla's menu
 * button packet to {@link #clickMenuButton}, so there is no packet of our own.
 */
public class ExtruderMenu extends AbstractContainerMenu {

    /** Fuel, running (1 or 0), and the facing as {@link Direction#get3DDataValue()}. */
    public static final int DATA_COUNT = 3;

    private final Container container;
    private final ContainerData data;
    private final @Nullable ExtruderEntity extruder;

    private final ExtruderType type;
    /** Menu slots: fuel, then the extruder's own extrusion and mined slots, then the player's. */
    private final int playerStart;

    /** The client's copy, built for the material the server sent and filled in by its syncing. */
    public ExtruderMenu(int id, Inventory inventory, ExtruderType type) {
        this(id, inventory, new SimpleContainer(ExtruderEntity.SLOTS), new SimpleContainerData(DATA_COUNT), type, null);
    }

    public ExtruderMenu(int id, Inventory inventory, Container container, ContainerData data, ExtruderType type, @Nullable ExtruderEntity extruder) {
        super(TechMenuTypes.EXTRUDER.get(), id);
        checkContainerSize(container, ExtruderEntity.SLOTS);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.type = type;
        this.extruder = extruder;

        Level level = inventory.player.level();
        this.addSlot(new Slot(container, ExtruderEntity.FUEL_SLOT, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ExtruderEntity.fuelValue(level, stack) > 0;
            }
        });

        // Centred under the fuel slot, however many there are.
        int extrude = type.getExtrudeSlots();
        for (int i = 0; i < extrude; i++) {
            this.addSlot(new Slot(container, ExtruderEntity.FIRST_EXTRUDE_SLOT + i, extrudeSlotX(extrude, i), 48));
        }

        for (int i = 0; i < type.getMinedSlots(); i++) {
            this.addSlot(new Slot(container, ExtruderEntity.FIRST_MINED_SLOT + i, 8 + i % 9 * 18, 87 + i / 9 * 18));
        }

        this.playerStart = this.slots.size();
        this.addStandardInventorySlots(inventory, 8, 162);
        this.addDataSlots(data);
    }

    /** Where the {@code i}th of {@code count} extrusion slots sits, the row centred on the screen. */
    public static int extrudeSlotX(int count, int i) {
        return 8 + (9 - count) * 9 + i * 18;
    }

    public ExtruderType getExtruderType() {
        return this.type;
    }

    public int getFuel() {
        return this.data.get(0);
    }

    public boolean isRunning() {
        return this.data.get(1) == 1;
    }

    public Direction getFacing() {
        return Direction.from3DDataValue(this.data.get(2));
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.extruder == null || id < 0 || id > ExtruderEntity.TOGGLE_BUTTON) {
            return false;
        }
        this.extruder.handleButton(id);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.extruder == null || this.extruder.stillValid(player);
    }

    /**
     * The extruder's slots empty into the player; the player's go to the fuel slot when they burn,
     * and to the extrusion slots otherwise, never to the mined slots.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < this.playerStart) {
            if (!this.moveItemStackTo(stack, this.playerStart, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!(this.slots.get(0).mayPlace(stack) && this.moveItemStackTo(stack, 0, 1, false))
                && !this.moveItemStackTo(stack, 1, 1 + this.type.getExtrudeSlots(), false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
