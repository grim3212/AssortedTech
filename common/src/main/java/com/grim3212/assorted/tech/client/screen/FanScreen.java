package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.network.FanUpdatePacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The GUI went retained-mode in 26.x: a screen records elements into a {@link GuiGraphicsExtractor}
 * from {@code extractRenderState} instead of drawing from {@code render}, and the base screen
 * sequences background, contents and tooltips itself - so the explicit {@code renderBackground} call
 * and the {@code super.render} sandwich are gone.
 */
public class FanScreen extends Screen {

    private final FanBlockEntity fanBlockEntity;
    private int localRange;
    private FanMode localMode;

    public FanScreen(FanBlockEntity fanBlockEntity) {
        super(Component.translatable("fan.screen"));
        this.fanBlockEntity = fanBlockEntity;
        this.localMode = this.fanBlockEntity.getMode() == FanMode.OFF ? this.fanBlockEntity.getOldMode() : this.fanBlockEntity.getMode();
        this.localRange = this.fanBlockEntity.getRange();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.ok"), btn -> {
            this.fanBlockEntity.setOldMode(localMode);
            this.fanBlockEntity.setRange(localRange);
            Services.NETWORK.sendToServer(new FanUpdatePacket(this.fanBlockEntity.getBlockPos(), this.localMode, this.localRange));
            this.close();
        }).bounds(width / 2 - 80, height / 4 + 120, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.cancel"), btn -> {
            this.close();
        }).bounds(width / 2 + 10, height / 4 + 120, 70, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.mode." + this.localMode.getSerializedName()), btn -> {
            this.localMode = this.localMode.getNext();
            btn.setMessage(Component.translatable("fan.screen.mode." + this.localMode.getSerializedName()));
        }).bounds(width / 2 - 50, height / 4 + 10, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.add_one"), btn -> {
            int newRange = this.localRange + 1;
            if (newRange > TechCommonMod.COMMON_CONFIG.fanMaxRange.get()) {
                this.localRange = TechCommonMod.COMMON_CONFIG.fanMaxRange.get();
            } else {
                this.localRange = newRange;
            }

        }).bounds(width / 2 + 20, height / 4 + 65, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.add_five"), btn -> {
            int newRange = this.localRange + 5;
            if (newRange > TechCommonMod.COMMON_CONFIG.fanMaxRange.get()) {
                this.localRange = TechCommonMod.COMMON_CONFIG.fanMaxRange.get();
            } else {
                this.localRange = newRange;
            }
        }).bounds(width / 2 + 65, height / 4 + 65, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.max"), btn -> {
            this.localRange = TechCommonMod.COMMON_CONFIG.fanMaxRange.get();
        }).bounds(width / 2 + 110, height / 4 + 65, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.minus_one"), btn -> {
            int newRange = this.localRange - 1;
            if (newRange < 1) {
                this.localRange = 1;
            } else {
                this.localRange = newRange;
            }
        }).bounds(width / 2 - 60, height / 4 + 65, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.minus_five"), btn -> {
            int newRange = this.localRange - 5;
            if (newRange < 1) {
                this.localRange = 1;
            } else {
                this.localRange = newRange;
            }

        }).bounds(width / 2 - 105, height / 4 + 65, 40, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("fan.screen.min"), btn -> {
            this.localRange = 1;
        }).bounds(width / 2 - 150, height / 4 + 65, 40, 20).build());
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

        // Font no longer forces the alpha byte, so 0xffffff would draw nothing.
        graphics.centeredText(this.font, Component.translatable("fan.screen.mode"), this.width / 2, this.height / 4 - 10, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.translatable("fan.screen.range"), this.width / 2, this.height / 4 + 45, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal("" + this.localRange), this.width / 2, this.height / 4 + 70, 0xFFFFFFFF);
    }
}
