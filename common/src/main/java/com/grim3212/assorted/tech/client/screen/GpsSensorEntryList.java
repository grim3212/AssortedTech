package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.tech.api.util.GpsSensorMode;
import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorFilter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

/**
 * The upgraded GPS sensor's filter entries, one row each with a button to remove it. Sized for the
 * most entries a sensor takes, so it never scrolls; it is drawn over the list well in the side
 * panel's texture, so vanilla's own list background and separators are skipped.
 */
public class GpsSensorEntryList extends ContainerObjectSelectionList<GpsSensorEntryList.Row> {

    private static final int ROW_HEIGHT = 16;
    private static final int TEXT = 0xFFE0E0E0;
    /** Tags stand apart from single ids, since one covers a whole group; the tooltip uses the same colour. */
    private static final ChatFormatting TAG_FORMAT = ChatFormatting.AQUA;
    private static final int TAG = 0xFF000000 | TextColor.fromLegacyFormat(TAG_FORMAT).getValue();
    private static final int INVALID = 0xFFFF5555;

    private final Font font;
    private final IntConsumer remove;

    public GpsSensorEntryList(Minecraft minecraft, int x, int y, int width, int height, IntConsumer remove) {
        super(minecraft, width, height, y, ROW_HEIGHT);
        this.setX(x);
        this.font = minecraft.font;
        this.remove = remove;
        this.centerListVertically = false;
    }

    public void setEntries(List<String> entries, GpsSensorMode mode) {
        this.clearEntries();
        for (int i = 0; i < entries.size(); i++) {
            this.addEntry(new Row(i, entries.get(i), mode));
        }
    }

    @Override
    protected void extractListBackground(GuiGraphicsExtractor graphics) {
    }

    @Override
    protected void extractListSeparators(GuiGraphicsExtractor graphics) {
    }

    @Override
    public int getRowLeft() {
        return this.getX() + 1;
    }

    @Override
    public int getRowWidth() {
        return this.width - 2;
    }

    public class Row extends ContainerObjectSelectionList.Entry<Row> {

        private final Component shown;
        /** The friendly name, then the entry as stored, then why it can never match if it cannot. */
        private final List<Component> tooltip;
        private final boolean valid;
        private final boolean tag;
        private final Button removeButton;

        Row(int index, String entry, GpsSensorMode mode) {
            this.shown = displayName(mode, entry);
            Component problem = GpsSensorFilter.problem(mode, entry, true);
            this.valid = problem == null;
            this.tag = GpsSensorFilter.isTag(entry);
            List<Component> tooltip = new ArrayList<>();
            // The name matches the row: red if it can never match, the tag colour for a tag.
            tooltip.add(!this.valid ? this.shown.copy().withStyle(ChatFormatting.RED) : this.tag ? this.shown.copy().withStyle(TAG_FORMAT) : this.shown);
            if (mode != GpsSensorMode.PLAYERS) {
                tooltip.add(Component.literal(entry).withStyle(ChatFormatting.DARK_GRAY));
            }
            if (problem != null) {
                tooltip.add(problem.copy().withStyle(ChatFormatting.RED));
            }
            this.tooltip = tooltip;
            this.removeButton = Button.builder(Component.literal("-"), btn -> GpsSensorEntryList.this.remove.accept(index)).size(12, 12).build();
        }

        /**
         * A mob's or item's own name, or a tag's translation where the loader or a mod gives one,
         * under the {@code tag.<registry>.<namespace>.<path>} key they share. Vanilla's tags have
         * none, so those fall back to their path, tidied up. Anything unknown shows as stored.
         */
        private static Component displayName(GpsSensorMode mode, String entry) {
            if (mode == GpsSensorMode.PLAYERS) {
                return Component.literal(entry);
            }
            boolean tag = GpsSensorFilter.isTag(entry);
            Identifier id = Identifier.tryParse(tag ? entry.substring(1) : entry);
            if (id == null) {
                return Component.literal(entry);
            }
            if (tag) {
                String key = "tag." + (mode == GpsSensorMode.MOBS ? "entity_type" : "item") + "." + id.getNamespace() + "." + id.getPath().replace('/', '.');
                return Component.literal("#").append(Language.getInstance().has(key) ? Component.translatable(key) : Component.literal(tidy(id.getPath())));
            }
            return mode == GpsSensorMode.MOBS
                    ? BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(EntityType::getDescription).orElse(Component.literal(entry))
                    : BuiltInRegistries.ITEM.getOptional(id).map(item -> new ItemStack(item).getHoverName()).orElse(Component.literal(entry));
        }

        /** {@code logs_that_burn} as {@code Logs That Burn}; a nested path keeps only its last part. */
        private static String tidy(String path) {
            StringBuilder out = new StringBuilder();
            for (String word : path.substring(path.lastIndexOf('/') + 1).split("_")) {
                if (!word.isEmpty()) {
                    out.append(out.isEmpty() ? "" : " ").append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
                }
            }
            return out.toString();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            Font font = GpsSensorEntryList.this.font;
            int textWidth = this.getContentWidth() - this.removeButton.getWidth() - 4;
            boolean clipped = font.width(this.shown) > textWidth;
            FormattedCharSequence text = clipped ? ComponentRenderUtils.clipText(this.shown, font, textWidth) : this.shown.getVisualOrderText();
            graphics.text(font, text, this.getContentX() + 1, this.getContentYMiddle() - font.lineHeight / 2, !this.valid ? INVALID : this.tag ? TAG : TEXT, true);

            this.removeButton.setPosition(this.getContentRight() - this.removeButton.getWidth(), this.getContentY());
            this.removeButton.extractRenderState(graphics, mouseX, mouseY, a);

            if (hovered && !this.removeButton.isMouseOver(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(font, this.tooltip, Optional.empty(), mouseX, mouseY);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.removeButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.removeButton);
        }
    }
}
