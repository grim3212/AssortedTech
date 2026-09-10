package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.network.AlarmUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The GUI went retained-mode in 26.x: a screen no longer draws from {@code render}, it records
 * elements into a {@link GuiGraphicsExtractor} that {@code GuiRenderer} plays back later. So
 * {@code render(GuiGraphics, ...)} became {@code extractRenderState(GuiGraphicsExtractor, ...)}, and
 * the panel texture moved into {@code extractBackground} so it stays behind the buttons - the base
 * screen sequences background, contents and tooltips itself now, which is why the explicit
 * {@code renderBackground} call and the {@code super.render} sandwich are gone.
 */
public class AlarmScreen extends Screen {

    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/alarm.png");
    private static final int TEXT_TOP = 30;

    private final AlarmBlockEntity alarmBlockEntity;
    private int alarmType = 0;

    public AlarmScreen(AlarmBlockEntity alarmEntity) {
        super(Component.translatable("alarm.screen"));
        this.alarmBlockEntity = alarmEntity;
        this.alarmType = alarmEntity.getAlarmType();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("alarm.screen.done"), btn -> {
            this.alarmBlockEntity.setAlarmType(alarmType);
            Services.NETWORK.sendToServer(new AlarmUpdatePacket(this.alarmBlockEntity.getBlockPos(), this.alarmType));
            this.close();
        }).bounds(this.width / 2 - 25, 210, 50, 20).build());
        this.addRenderableWidget(Button.builder(alarmName(this.alarmType), btn -> {
            this.alarmType += 1;
            if (this.alarmType > 13) {
                this.alarmType = 0;
            }

            btn.setMessage(alarmName(this.alarmType));
        }).bounds(this.width / 2 - 50, 100, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("alarm.screen.test"), btn -> {
            Minecraft.getInstance().player.playSound(AlarmBlockEntity.getSound(this.alarmType).get(), 1.0F, 1.0F);
        }).bounds(this.width / 2 - 25, 120, 50, 20).build());
    }

    // String.valueOf, not the bare char: TranslatableContents only accepts a Component, Number,
    // Boolean or String, and a Character throws while the component is built - which killed the whole
    // screen before it could open.
    private static Component alarmName(int alarmType) {
        return Component.translatable("alarm.screen.name", String.valueOf((char) ('A' + alarmType)));
    }

    private void close() {
        // Minecraft#setScreen is gone; the current screen lives on Gui now.
        this.minecraft.gui.setScreen(null);
    }

    @Override
    public void onClose() {
        this.close();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        int posX = (this.width - 256) / 2;
        // The texture is 256x230, but the 1.20.1 blit assumed the 256x256 default, so it sampled
        // 0..230/256 of the sheet into a 230px tall quad. Kept as-is so the panel looks exactly as it
        // did; passing 256, 230 here would be the fix.
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, posX, 5, 0.0F, 0.0F, 256, 230, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        int posX = (this.width - 256) / 2;

        // The panel texture is a busy pastel swirl. Dark text on it - even on a light plate - stays hard
        // to read at GUI scale, so the body uses the idiom vanilla reaches for when text has to survive
        // an arbitrary background (see GuiGraphicsExtractor#textWithBackdrop, used by the HUD title):
        // an opaque dark backdrop with white, drop-shadowed glyphs.
        // Font no longer forces the alpha byte, so a colour without one is invisible: 0xFF1010.
        Component title = Component.translatable("alarm.screen");
        graphics.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 10, 0xFFFF1010, true);

        Component description = Component.translatable("alarm.screen.description");
        int textBorder = 5;
        int textX = posX + textBorder;
        int textWidth = 256 - textBorder * 2;
        int textHeight = this.font.wordWrapHeight(description, textWidth);

        graphics.fill(textX - 3, TEXT_TOP - 3, textX + textWidth + 3, TEXT_TOP + textHeight + 3, 0xF01A1A22);
        graphics.textWithWordWrap(this.font, description, textX, TEXT_TOP, textWidth, 0xFFFFFFFF, true);
    }
}
