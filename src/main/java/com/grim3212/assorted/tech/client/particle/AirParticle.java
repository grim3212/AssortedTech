package com.grim3212.assorted.tech.client.particle;

import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.util.FanMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AirParticle extends TextureSheetParticle {

	private final FanBlockEntity fan;
	private final BlockPos startPos;
	private final Direction direction;
	private final double distanceModifier;

	protected AirParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockPos pos) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed);
		this.fan = (FanBlockEntity) level.getBlockEntity(pos);
		this.direction = fan.getBlockState().getValue(BlockStateProperties.FACING);
		this.startPos = new BlockPos(x, y, z);
		this.distanceModifier = TechConfig.COMMON.fanMaxRange.get() - fan.getRange() + 1;

		lifetime = (int) (((TechConfig.COMMON.fanMaxRange.get() + this.distanceModifier) / 4.0F) / (this.random.nextFloat() * 0.9F + 0.1F));

		rCol = gCol = bCol = 1.0F;
		xd = yd = zd = 0.0D;
		gravity = 0.0F;
		this.setSize(0.05F, 0.05F);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return NORMAL_RENDER;
	}

	@Override
	public void tick() {
		super.tick();

		if (this.onGround || fan.getMode() == FanMode.OFF) {
			this.remove();
		}

		boolean isSucking = fan.getMode() == FanMode.SUCK;

		if (!isSucking) {
			double distanceFromStart = getDistance(this.startPos) - 1.5D;
			if (distanceFromStart > fan.getRange()) {
				this.remove();
			}
		} else {
			double distanceFromStart = getDistance(this.startPos) - 1.5D;
			if (distanceFromStart > fan.getRange()) {
				this.remove();
			}
		}

		double baseSpeed = 0.01D;
		double fanSpeed = isSucking ? -TechConfig.COMMON.fanSpeed.get() : TechConfig.COMMON.fanSpeed.get();
		double speed = (fanSpeed / this.distanceModifier) + (isSucking ? -baseSpeed : baseSpeed);

		switch (direction) {
			case UP -> {
				if (isSucking ? yd > 0 : yd < 0)
					this.remove();
				this.setVelocity(xd, yd + speed, zd);
			}
			case DOWN -> {
				if (isSucking ? yd < 0 : yd > 0)
					this.remove();
				this.setVelocity(xd, yd - speed, zd);
			}
			case EAST -> {
				if (isSucking ? xd > 0 : xd < 0)
					this.remove();
				this.setVelocity(xd + speed, yd, zd);
			}
			case WEST -> {
				if (isSucking ? xd < 0 : xd > 0)
					this.remove();
				this.setVelocity(xd - speed, yd, zd);
			}
			case SOUTH -> {
				if (isSucking ? zd > 0 : zd < 0)
					this.remove();
				this.setVelocity(xd, yd, zd + speed);
			}
			case NORTH -> {
				if (isSucking ? zd < 0 : zd > 0)
					this.remove();
				this.setVelocity(xd, yd, zd - speed);
			}
		}
	}

	public double getDistance(BlockPos pos) {
		double d0 = this.x - pos.getX() + 0.5D;
		double d1 = this.y - pos.getY() + 0.5D;
		double d2 = this.z - pos.getZ() + 0.5D;
		return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	public void setVelocity(double x, double y, double z) {
		this.xd = x;
		this.yd = y;
		this.zd = z;
	}

	private static final ParticleRenderType NORMAL_RENDER = new ParticleRenderType() {

		@Override
		public String toString() {
			return "assortedtech:air";
		}

		@Override
		public void begin(BufferBuilder builder, TextureManager textureManager) {
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
			builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
		}
	};
}
