package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.network.AlarmUpdatePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AlarmScreen extends Screen {

    private static final ResourceLocation LOCATION = new ResourceLocation(Constants.MOD_ID, "textures/gui/alarm.png");

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
        this.addRenderableWidget(Button.builder(Component.translatable("alarm.screen.name", (char) (65 + this.alarmType)), btn -> {
            this.alarmType += 1;
            if (this.alarmType > 13) {
                this.alarmType = 0;
            }

            btn.setMessage(Component.translatable("alarm.screen.name", (char) (65 + this.alarmType)));
        }).bounds(this.width / 2 - 50, 100, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("alarm.screen.test"), btn -> {
            Minecraft.getInstance().player.playSound(AlarmBlockEntity.getSound(this.alarmType).get(), 1.0F, 1.0F);
        }).bounds(this.width / 2 - 25, 120, 50, 20).build());
    }

    private void close() {
        this.minecraft.setScreen((Screen) null);
    }

    @Override
    public void onClose() {
        this.close();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        int posX = (this.width - 256) / 2;
        guiGraphics.blit(LOCATION, posX, 5, 0, 0, 256, 230);

        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        stack.popPose();
        guiGraphics.drawCenteredString(font, Component.translatable("alarm.screen"), width / 2, 10, 0xFF1010);

        int textBorder = 5;
        guiGraphics.drawWordWrap(font, Component.translatable("alarm.screen.description"), posX + textBorder, 30, 256 - textBorder * 2, textBorder);

    }
}
