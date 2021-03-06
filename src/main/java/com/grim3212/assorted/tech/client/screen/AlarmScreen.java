package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.network.AlarmUpdatePacket;
import com.grim3212.assorted.tech.common.network.PacketHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class AlarmScreen extends Screen {

	private static final ResourceLocation LOCATION = new ResourceLocation(AssortedTech.MODID, "textures/gui/alarm.png");

	private final AlarmBlockEntity alarmBlockEntity;
	private int alarmType = 0;

	public AlarmScreen(AlarmBlockEntity alarmEntity) {
		super(new TranslatableComponent("alarm.screen"));
		this.alarmBlockEntity = alarmEntity;
		this.alarmType = alarmEntity.getAlarmType();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		this.addRenderableWidget(new Button(this.width / 2 - 25, 210, 50, 20, new TranslatableComponent("alarm.screen.done"), btn -> {
			this.alarmBlockEntity.setAlarmType(alarmType);
			PacketHandler.sendToServer(new AlarmUpdatePacket(this.alarmBlockEntity.getBlockPos(), this.alarmType));
			this.close();
		}));
		this.addRenderableWidget(new Button(this.width / 2 - 50, 100, 100, 20, new TranslatableComponent("alarm.screen.name", (char) (65 + this.alarmType)), btn -> {
			this.alarmType += 1;
			if (this.alarmType > 13) {
				this.alarmType = 0;
			}

			btn.setMessage(new TranslatableComponent("alarm.screen.name", (char) (65 + this.alarmType)));
		}));
		this.addRenderableWidget(new Button(this.width / 2 - 25, 120, 50, 20, new TranslatableComponent("alarm.screen.test"), btn -> {
			Minecraft.getInstance().player.playSound(AlarmBlockEntity.getSound(this.alarmType).get(), 1.0F, 1.0F);
		}));
	}

	private void close() {
		this.minecraft.setScreen((Screen) null);
	}

	@Override
	public void onClose() {
		this.close();
	}

	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(stack);
		RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, LOCATION);
		int posX = (this.width - 256) / 2;
		this.blit(stack, posX, 5, 0, 0, 256, 230);

		stack.pushPose();
		super.render(stack, mouseX, mouseY, partialTicks);
		stack.popPose();
		AlarmScreen.drawCenteredString(stack, font, new TranslatableComponent("alarm.screen"), width / 2, 10, 0xFF1010);

		int textBorder = 5;
		font.drawWordWrap(new TranslatableComponent("alarm.screen.description"), posX + textBorder, 30, 256 - textBorder * 2, textBorder);

	}
}
