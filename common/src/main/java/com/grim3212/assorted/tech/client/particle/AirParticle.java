package com.grim3212.assorted.tech.client.particle;

import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.particle.air.AirParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TextureSheetParticle} was removed in 26.2; the textured base class is
 * {@link SingleQuadParticle}, which takes its {@link TextureAtlasSprite} in the constructor instead of
 * having one pushed in afterwards by {@code pickSprite}. {@code getRenderType()} was replaced by
 * {@link SingleQuadParticle#getLayer()}, which returns a {@link SingleQuadParticle.Layer} record
 * naming the atlas and the render pipeline rather than a {@code ParticleRenderType} constant.
 */
public class AirParticle extends SingleQuadParticle {

    private final FanBlockEntity fan;
    private final BlockPos startPos;
    private final Direction direction;
    private final double distanceModifier;

    public AirParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, FanBlockEntity fan, TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        this.fan = fan;
        this.direction = fan.getBlockState().getValue(BlockStateProperties.FACING);
        this.startPos = BlockPos.containing(x, y, z);
        this.distanceModifier = TechCommonMod.COMMON_CONFIG.fanMaxRange.get() - fan.getRange() + 1;

        lifetime = (int) (((TechCommonMod.COMMON_CONFIG.fanMaxRange.get() + this.distanceModifier) / 4.0F) / (this.random.nextFloat() * 0.9F + 0.1F));

        rCol = gCol = bCol = 1.0F;
        xd = yd = zd = 0.0D;
        gravity = 0.0F;
        this.setSize(0.05F, 0.05F);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
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
        double fanSpeed = isSucking ? -TechCommonMod.COMMON_CONFIG.fanSpeed.get() : TechCommonMod.COMMON_CONFIG.fanSpeed.get();
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

    public static class Factory implements ParticleProvider<AirParticleData> {

        private final SpriteSet sprites;

        public Factory(SpriteSet sprite) {
            this.sprites = sprite;
        }

        /**
         * {@code createParticle} is handed the engine's {@link RandomSource} now, and the provider is
         * what resolves the sprite - {@code Particle#pickSprite} no longer exists. Returning null is
         * the documented way to decline, which is used here when the fan the particle would follow is
         * not loaded on the client; the 1.20.1 code cast the lookup unchecked and would have thrown.
         */
        @Nullable
        @Override
        public Particle createParticle(AirParticleData data, ClientLevel level, double x, double y, double z, double mx, double my, double mz, RandomSource random) {
            BlockEntity blockEntity = level.getBlockEntity(data.pos);
            if (!(blockEntity instanceof FanBlockEntity fan)) {
                return null;
            }
            return new AirParticle(level, x, y, z, mx, my, mz, fan, this.sprites.get(random));
        }
    }
}
