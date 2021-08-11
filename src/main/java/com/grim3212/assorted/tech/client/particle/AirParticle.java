package com.grim3212.assorted.tech.client.particle;

import org.lwjgl.opengl.GL11;

import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.util.FanMode;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class AirParticle extends SpriteTexturedParticle {

	private final FanBlockEntity fan;
	private final BlockPos startPos;
	private final Direction direction;
	private final double distanceModifier;

	protected AirParticle(ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockPos pos) {
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
	public IParticleRenderType getRenderType() {
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
			case UP:
				if (isSucking ? yd > 0 : yd < 0)
					this.remove();
				this.setVelocity(xd, yd + speed, zd);
				break;
			case DOWN:
				if (isSucking ? yd < 0 : yd > 0)
					this.remove();
				this.setVelocity(xd, yd - speed, zd);
				break;
			case EAST:
				if (isSucking ? xd > 0 : xd < 0)
					this.remove();
				this.setVelocity(xd + speed, yd, zd);
				break;
			case WEST:
				if (isSucking ? xd < 0 : xd > 0)
					this.remove();
				this.setVelocity(xd - speed, yd, zd);
				break;
			case SOUTH:
				if (isSucking ? zd > 0 : zd < 0)
					this.remove();
				this.setVelocity(xd, yd, zd + speed);
				break;
			case NORTH:
				if (isSucking ? zd < 0 : zd > 0)
					this.remove();
				this.setVelocity(xd, yd, zd - speed);
				break;
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

	private static final IParticleRenderType NORMAL_RENDER = new IParticleRenderType() {

		@Override
		public String toString() {
			return "assortedtech:air";
		}

		@Override
		public void begin(BufferBuilder builder, TextureManager textureManager) {
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE);
		}

		@Override
		public void end(Tessellator tessellator) {
			tessellator.end();
		}
	};
}
