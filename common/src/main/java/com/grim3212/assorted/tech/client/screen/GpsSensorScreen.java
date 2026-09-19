package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.GpsSensorMode;
import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorFilter;
import com.grim3212.assorted.tech.common.inventory.GpsSensorMenu;
import com.grim3212.assorted.tech.common.network.GpsSensorFilterPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Holds a GPS sensor's GPS, in the slot at the top right, and sets what it looks for: the mode, a
 * player name or mob id to narrow it to, or for items the item in the ghost slot, plus the upgraded
 * sensor's radius and whether to outline the watched box. The upgraded sensor adds what is typed to
 * a list of entries, tags allowed, shown in a panel to the right. Every change applies at once; the
 * buttons go through {@link GpsSensorMenu#clickMenuButton} and the screen reads the result back
 * from the client's synced block entity.
 */
public class GpsSensorScreen extends AbstractContainerScreen<GpsSensorMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/gps_sensor.png");
    private static final Identifier LIST_TEXTURE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/container/gps_sensor_list.png");
    private static final int TEXT = 0xFF404040;
    private static final int INVALID = 0xFFFF5555;
    private static final int MAIN_WIDTH = 176;
    private static final int PANEL_WIDTH = 124;
    /** Tall enough for every entry the sensor allows, six rows of 16, so the list never needs to scroll. */
    private static final int PANEL_HEIGHT = 126;
    private static final int HEIGHT = 232;
    private static final int WIDTH = 160;

    private final GpsSensorBlockEntity sensor;
    private final boolean upgraded;
    private GpsSensorMode shownMode;
    private List<String> shownEntries = List.of();
    private @Nullable Component shownTooltip;
    private @Nullable Component shownAddTooltip;

    private Button mode;
    private EditBox filter;
    private @Nullable Button add;
    private @Nullable GpsSensorEntryList entries;
    private @Nullable Button shrink;
    private @Nullable Button grow;
    private Button show;

    public GpsSensorScreen(GpsSensorMenu menu, Inventory inventory, Component title) {
        // The upgraded sensor's panel counts as part of the screen, so clicks on it do not drop the carried item.
        super(menu, inventory, title, menu.getSensor().isUpgraded() ? MAIN_WIDTH + PANEL_WIDTH : MAIN_WIDTH, HEIGHT);
        this.sensor = menu.getSensor();
        this.upgraded = this.sensor.isUpgraded();
        this.shownMode = this.sensor.getMode();
    }

    @Override
    protected void init() {
        super.init();
        // The upgraded sensor's panel hangs off the right, so centre the main panel rather than the
        // pair, unless that would push the panel off a narrow screen.
        if (this.upgraded) {
            this.leftPos = Math.max(0, Math.min((this.width - MAIN_WIDTH) / 2, this.width - this.imageWidth));
        }
        int x = this.leftPos + 8;
        int y = this.topPos;
        // init runs again on resize, so the box keeps what was typed rather than the last value synced.
        String typed = this.filter == null ? (this.upgraded ? "" : this.sensor.getFilter()) : this.filter.getValue();

        this.mode = this.addRenderableWidget(Button.builder(this.modeLabel(), btn -> this.click(GpsSensorMenu.MODE_BUTTON)).bounds(x, y + 40, WIDTH, 20).build());

        this.filter = new EditBox(this.font, x, y + 64, WIDTH, 20, Component.translatable("gps_sensor.screen.filter"));
        this.filter.setMaxLength(GpsSensorBlockEntity.MAX_FILTER_LENGTH);
        this.filter.setValue(typed);
        this.filter.setResponder(value -> {
            this.validateFilter();
            // The upgraded sensor's box is only what is about to be added; the list is its filter.
            if (!this.upgraded) {
                Services.NETWORK.sendToServer(new GpsSensorFilterPacket(value, false));
            }
        });
        this.addRenderableWidget(this.filter);

        if (this.upgraded) {
            this.add = this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> this.addTyped()).bounds(x + WIDTH - 20, y + 64, 20, 20).build());
            this.entries = this.addRenderableWidget(new GpsSensorEntryList(this.minecraft, this.leftPos + MAIN_WIDTH + 8, y + 18, 108, 100, i -> this.click(GpsSensorMenu.REMOVE_ENTRY_BUTTON + i)));
            this.shownEntries = List.of();
        }

        if (this.sensor.getMaxRadius() > 0) {
            this.shrink = this.addRenderableWidget(Button.builder(Component.literal("-"), btn -> this.click(GpsSensorMenu.SHRINK_BUTTON)).bounds(x, y + 88, 20, 20).build());
            this.grow = this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> this.click(GpsSensorMenu.GROW_BUTTON)).bounds(x + WIDTH - 20, y + 88, 20, 20).build());
        }

        this.show = this.addRenderableWidget(Button.builder(this.showLabel(), btn -> this.click(GpsSensorMenu.SHOW_BUTTON)).bounds(x, y + 112, WIDTH, 20).build());

        this.refresh();
    }

    private void click(int id) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    /** The server checks the entry again, and the list follows once the sensor syncs back. */
    private void addTyped() {
        if (this.add != null && this.add.active) {
            Services.NETWORK.sendToServer(new GpsSensorFilterPacket(this.filter.getValue().strip(), true));
            this.filter.setValue("");
        }
    }

    /** The server's answer to a button arrives as a block entity update, so the widgets follow it here. */
    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.shownMode != this.sensor.getMode()) {
            this.shownMode = this.sensor.getMode();
            // A plain sensor's box shows the new mode's own saved filter; an upgraded one's is only unsent text.
            this.filter.setValue(this.upgraded ? "" : this.sensor.getFilter());
        }
        this.refresh();
    }

    /**
     * The filter row is a text box for players and mobs, and the ghost slot for items; the upgraded
     * sensor keeps a box beside the slot for item ids and tags, and a + to add what is typed. Its
     * slot adds a clicked item to the list, so an item can be added either way.
     */
    private void refresh() {
        boolean items = this.shownMode == GpsSensorMode.ITEMS;
        this.mode.setMessage(this.modeLabel());
        this.show.setMessage(this.showLabel());
        this.filter.setVisible(!items || this.upgraded);
        this.filter.setX(this.leftPos + (items ? 30 : 8));
        this.filter.setWidth(WIDTH - (items ? 22 : 0) - (this.upgraded ? 22 : 0));
        if (this.filter.isVisible()) {
            this.filter.setHint(Component.translatable((this.upgraded ? "gps_sensor.screen.add_" : "gps_sensor.screen.any_") + this.shownMode.getSerializedName()).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (this.shrink != null) {
            this.shrink.active = this.sensor.getRadius() > 0;
            this.grow.active = this.sensor.getRadius() < this.sensor.getMaxRadius();
        }
        if (this.entries != null && !this.shownEntries.equals(this.sensor.getEntries())) {
            this.shownEntries = List.copyOf(this.sensor.getEntries());
            this.entries.setEntries(this.shownEntries, this.shownMode);
        }
        this.validateFilter();
    }

    /**
     * Filter text that could never match is drawn red, with the reason in the tooltip. The upgraded
     * sensor's tooltip explains adding and tags, which do not fit in the hint, and its + is only
     * active for an entry the server would take.
     */
    private void validateFilter() {
        String typed = this.filter.getValue().strip();
        Component problem = GpsSensorFilter.problem(this.shownMode, typed, this.upgraded);
        this.filter.setTextColor(problem == null ? 0xFFE0E0E0 : INVALID);

        Component help = this.upgraded ? Component.translatable("gps_sensor.screen.help." + this.shownMode.getSerializedName()) : null;
        Component tooltip = problem == null ? help : help == null ? problem.copy().withStyle(ChatFormatting.RED) : help.copy().append("\n").append(problem.copy().withStyle(ChatFormatting.RED));
        if (!Objects.equals(tooltip, this.shownTooltip)) {
            this.shownTooltip = tooltip;
            this.filter.setTooltip(tooltip == null ? null : Tooltip.create(tooltip));
        }

        if (this.add != null) {
            Component refused = problem != null ? problem
                    : this.shownEntries.size() >= GpsSensorBlockEntity.MAX_ENTRIES ? Component.translatable("gps_sensor.screen.problem.full")
                    : this.shownEntries.stream().anyMatch(GpsSensorFilter.normalize(this.shownMode, typed)::equalsIgnoreCase) ? Component.translatable("gps_sensor.screen.problem.duplicate")
                    : null;
            this.add.active = refused == null && !typed.isEmpty();
            if (!Objects.equals(refused, this.shownAddTooltip)) {
                this.shownAddTooltip = refused;
                this.add.setTooltip(refused == null ? null : Tooltip.create(refused));
            }
        }
    }

    private Component modeLabel() {
        return Component.translatable("gps_sensor.screen.mode", Component.translatable("gps_sensor.screen.mode." + this.sensor.getMode().getSerializedName()));
    }

    private Component showLabel() {
        return Component.translatable(this.sensor.isShowingTarget() ? "gps_sensor.screen.show.on" : "gps_sensor.screen.show.off");
    }

    private Component statusLine() {
        BlockPos target = this.sensor.getTarget();
        return switch (this.sensor.targetStatus()) {
            case NONE -> Component.translatable("gps_sensor.screen.status.none").withStyle(ChatFormatting.DARK_GRAY);
            case GOOD -> Component.translatable("gps_sensor.screen.status.good", target.getX(), target.getY(), target.getZ()).withStyle(ChatFormatting.DARK_GREEN);
            case BLOCKED -> Component.translatable("gps_sensor.screen.status.blocked", target.getX(), target.getY(), target.getZ()).withStyle(ChatFormatting.DARK_RED);
            case OUT_OF_RANGE -> Component.translatable("gps_sensor.screen.status.out_of_range", this.sensor.getRange()).withStyle(ChatFormatting.DARK_RED);
            case OTHER_DIMENSION -> Component.translatable("gps_sensor.screen.status.other_dimension").withStyle(ChatFormatting.DARK_RED);
        };
    }

    /** Vanilla only moves focus to what was clicked, so a click anywhere else would leave the box taking keys. */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.filter.isFocused() && !this.filter.isMouseOver(event.x(), event.y())) {
            this.filter.setFocused(false);
            if (this.getFocused() == this.filter) {
                this.setFocused(null);
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    /**
     * Typing in the box must not close the screen when a letter is also the inventory key, and Enter
     * adds to the upgraded sensor's list like its +.
     */
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!event.isEscape() && this.filter.canConsumeInput()) {
            if (this.upgraded && event.isConfirmation()) {
                this.addTyped();
            } else {
                this.filter.keyPressed(event);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    /** The upgraded sensor's box sits where the slot's hint would go, so its always empty slot explains itself on hover. */
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        if (this.hoveredSlot != null && this.hoveredSlot.index == GpsSensorMenu.FILTER_SLOT && this.upgraded) {
            graphics.setTooltipForNextFrame(Component.translatable("gps_sensor.screen.item_add_hint"), mouseX, mouseY);
        }
        if (this.hoveredSlot != null && this.hoveredSlot.index == GpsSensorMenu.GPS_SLOT && !this.hoveredSlot.hasItem() && this.menu.getCarried().isEmpty()) {
            graphics.setTooltipForNextFrame(Component.translatable("gps_sensor.screen.gps_hint"), mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, MAIN_WIDTH, HEIGHT, 256, 256);
        if (this.upgraded) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, LIST_TEXTURE, this.leftPos + MAIN_WIDTH, this.topPos, 0.0F, 0.0F, PANEL_WIDTH, PANEL_HEIGHT, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);

        // The longer statuses do not fit on one line beside the GPS slot.
        List<FormattedCharSequence> status = this.font.split(this.statusLine(), WIDTH - 24);
        for (int i = 0; i < Math.min(2, status.size()); i++) {
            graphics.text(this.font, status.get(i), 8, 18 + i * 10, TEXT, false);
        }

        if (this.shownMode == GpsSensorMode.ITEMS && !this.upgraded) {
            ItemStack item = this.menu.getSlot(GpsSensorMenu.FILTER_SLOT).getItem();
            Component name = Component.translatable("gps_sensor.screen.item", item.isEmpty() ? Component.translatable("gps_sensor.screen.any_item") : item.getHoverName());
            graphics.text(this.font, this.font.split(name, WIDTH - 22).getFirst(), 30, 66, TEXT, false);
            // A plain sensor has no radius row, so the hint has room to wrap down toward the outline button.
            List<FormattedCharSequence> hint = this.font.split(Component.translatable(item.isEmpty() ? "gps_sensor.screen.item_hint" : "gps_sensor.screen.item_change_hint"), WIDTH - 22);
            for (int i = 0; i < Math.min(3, hint.size()); i++) {
                graphics.text(this.font, hint.get(i), 30, 76 + i * 10, 0xFF808080, false);
            }
        }

        if (this.shrink != null) {
            Component radius = Component.translatable("gps_sensor.screen.radius", this.sensor.getRadius());
            graphics.text(this.font, radius, (MAIN_WIDTH - this.font.width(radius)) / 2, 94, TEXT, false);
        }

        if (this.upgraded) {
            graphics.text(this.font, Component.translatable("gps_sensor.screen.entries", this.shownEntries.size(), GpsSensorBlockEntity.MAX_ENTRIES), MAIN_WIDTH + 8, 6, TEXT, false);
            if (this.shownEntries.isEmpty()) {
                List<FormattedCharSequence> empty = this.font.split(Component.translatable("gps_sensor.screen.entries.empty." + this.shownMode.getSerializedName()), 100);
                for (int i = 0; i < empty.size(); i++) {
                    graphics.text(this.font, empty.get(i), MAIN_WIDTH + 12, 24 + i * 10, 0xFF808080, false);
                }
            }
        }
    }
}
