package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.common.entity.ExtruderEntity;
import com.grim3212.assorted.tech.common.inventory.ExtruderMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * The extruder's inventory, with its direction buttons and start/stop button down the left side.
 * The buttons send vanilla's menu button click, which {@link ExtruderMenu#clickMenuButton} handles.
 * Beside the fuel slot, as in GrimPack, is how far its fuel lasts if it mines every block or lays
 * one at every step.
 */
public class ExtruderScreen extends AbstractContainerScreen<ExtruderMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/extruder.png");
    private static final int TEXT = 0xFF404040;
    private static final int BRACKET = 0xFF9C9C9C;
    private static final Direction[] BUTTON_ORDER = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private final Map<Direction, Button> directions = new EnumMap<>(Direction.class);
    private Button toggle;

    public ExtruderScreen(ExtruderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 244);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos - 52;
        int y = this.topPos + 50;

        for (int i = 0; i < BUTTON_ORDER.length; i++) {
            Direction direction = BUTTON_ORDER[i];
            this.directions.put(direction, this.addRenderableWidget(Button.builder(Component.translatable("extruder.screen.direction." + direction.getSerializedName()), btn -> this.click(direction.get3DDataValue()))
                    .bounds(x, y + i * 20, 48, 20).build()));
        }
        this.toggle = this.addRenderableWidget(Button.builder(this.toggleLabel(), btn -> this.click(ExtruderEntity.TOGGLE_BUTTON)).bounds(x, y + BUTTON_ORDER.length * 20 + 6, 48, 20).build());
    }

    private void click(int id) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    private Component toggleLabel() {
        return Component.translatable(this.menu.isRunning() ? "extruder.screen.stop" : "extruder.screen.start");
    }

    /** The facing and running state arrive through the menu's data slots, so the buttons follow them. */
    @Override
    protected void containerTick() {
        super.containerTick();
        Direction facing = this.menu.getFacing();
        this.directions.forEach((direction, button) -> button.active = direction != facing);
        this.toggle.setMessage(this.toggleLabel());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Only the fuel slot is in the texture; each material shows as many others as it has.
        for (int i = 1; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            if (slot.container instanceof Inventory) {
                break;
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 176.0F, 0.0F, 18, 18, 256, 256);
        }

        // The bracket from the fuel slot down to the extrusion row, as wide as the row.
        int count = this.menu.getExtruderType().getExtrudeSlots();
        int left = this.leftPos + ExtruderMenu.extrudeSlotX(count, 0) + 7;
        int right = this.leftPos + ExtruderMenu.extrudeSlotX(count, count - 1) + 8;
        int top = this.topPos;
        graphics.fill(this.leftPos + 87, top + 38, this.leftPos + 88, top + 43, BRACKET);
        graphics.fill(left, top + 43, right + 1, top + 44, BRACKET);
        graphics.fill(left, top + 44, left + 1, top + 46, BRACKET);
        graphics.fill(right, top + 44, right + 1, top + 46, BRACKET);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(this.font, Component.translatable("extruder.screen.fuel", this.menu.getFuel()), 8, 24, TEXT, false);
        graphics.text(this.font, Component.translatable("extruder.screen.mined"), 8, 75, TEXT, false);

        ExtruderType type = this.menu.getExtruderType();
        int mine = this.blocksFor(type.blockCost(TechCommonMod.COMMON_CONFIG.extruderFuelPerMinedBlock.get()), false);
        int extrude = this.blocksFor(type.blockCost(TechCommonMod.COMMON_CONFIG.extruderFuelPerExtrudedBlock.get()), true);
        if (mine > 0 || extrude > 0) {
            graphics.text(this.font, Component.translatable("extruder.screen.mine_blocks", mine), 102, 18, TEXT, false);
            graphics.text(this.font, Component.translatable("extruder.screen.extrude_blocks", extrude), 102, 30, TEXT, false);
        }
    }

    /**
     * The fuel burning now, then the stack in the fuel slot, which the extruder burns one at a time
     * as the current fuel runs out, at its material's speed.
     */
    private int blocksFor(int costPerBlock, boolean laying) {
        ItemStack stack = this.menu.getSlot(ExtruderEntity.FUEL_SLOT).getItem();
        int perItem = ExtruderEntity.fuelValue(this.minecraft.level, stack);
        return ExtruderEntity.blocksFor(this.menu.getFuel(), perItem, perItem > 0 ? stack.getCount() : 0, this.menu.getExtruderType().getSpeed(), costPerBlock, laying);
    }
}
