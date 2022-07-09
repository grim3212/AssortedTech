package com.grim3212.assorted.tech.client.screen;

import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.network.FanUpdatePacket;
import com.grim3212.assorted.tech.common.network.PacketHandler;
import com.grim3212.assorted.tech.common.util.FanMode;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
		this.addRenderableWidget(new Button(width / 2 - 80, height / 4 + 120, 70, 20, Component.translatable("fan.screen.ok"), btn -> {
			this.fanBlockEntity.setOldMode(localMode);
			this.fanBlockEntity.setRange(localRange);
			PacketHandler.sendToServer(new FanUpdatePacket(this.fanBlockEntity.getBlockPos(), this.localMode, this.localRange));
			this.close();
		}));
		this.addRenderableWidget(new Button(width / 2 + 10, height / 4 + 120, 70, 20, Component.translatable("fan.screen.cancel"), btn -> {
			this.close();
		}));
		this.addRenderableWidget(new Button(width / 2 - 50, height / 4 + 10, 100, 20, Component.translatable("fan.screen.mode." + this.localMode.getSerializedName()), btn -> {
			this.localMode = this.localMode.getNext();
			btn.setMessage(Component.translatable("fan.screen.mode." + this.localMode.getSerializedName()));
		}));
		this.addRenderableWidget(new Button(width / 2 + 20, height / 4 + 65, 40, 20, Component.translatable("fan.screen.add_one"), btn -> {
			int newRange = this.localRange + 1;
			if (newRange > TechConfig.COMMON.fanMaxRange.get()) {
				this.localRange = TechConfig.COMMON.fanMaxRange.get();
			} else {
				this.localRange = newRange;
			}

		}));
		this.addRenderableWidget(new Button(width / 2 + 65, height / 4 + 65, 40, 20, Component.translatable("fan.screen.add_five"), btn -> {
			int newRange = this.localRange + 5;
			if (newRange > TechConfig.COMMON.fanMaxRange.get()) {
				this.localRange = TechConfig.COMMON.fanMaxRange.get();
			} else {
				this.localRange = newRange;
			}
		}));
		this.addRenderableWidget(new Button(width / 2 + 110, height / 4 + 65, 40, 20, Component.translatable("fan.screen.max"), btn -> {
			this.localRange = TechConfig.COMMON.fanMaxRange.get();
		}));
		this.addRenderableWidget(new Button(width / 2 - 60, height / 4 + 65, 40, 20, Component.translatable("fan.screen.minus_one"), btn -> {
			int newRange = this.localRange - 1;
			if (newRange < 1) {
				this.localRange = 1;
			} else {
				this.localRange = newRange;
			}
		}));
		this.addRenderableWidget(new Button(width / 2 - 105, height / 4 + 65, 40, 20, Component.translatable("fan.screen.minus_five"), btn -> {
			int newRange = this.localRange - 5;
			if (newRange < 1) {
				this.localRange = 1;
			} else {
				this.localRange = newRange;
			}

		}));
		this.addRenderableWidget(new Button(width / 2 - 150, height / 4 + 65, 40, 20, Component.translatable("fan.screen.min"), btn -> {
			this.localRange = 1;
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
		stack.pushPose();
		super.render(stack, mouseX, mouseY, partialTicks);
		stack.popPose();
		FanScreen.drawCenteredString(stack, font, Component.translatable("fan.screen.mode"), width / 2, height / 4 - 10, 0xffffff);
		FanScreen.drawCenteredString(stack, font, Component.translatable("fan.screen.range"), width / 2, height / 4 + 45, 0xffffff);
		FanScreen.drawCenteredString(stack, font, Component.literal("" + this.localRange), width / 2, height / 4 + 70, 0xffffff);
	}
}
