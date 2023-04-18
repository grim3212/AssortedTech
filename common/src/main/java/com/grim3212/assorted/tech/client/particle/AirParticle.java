package com.grim3212.assorted.tech.client.particle;

import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.particle.air.AirParticleData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class AirParticle extends TextureSheetParticle {

    private final FanBlockEntity fan;
    private final BlockPos startPos;
    private final Direction direction;
    private final double distanceModifier;

    public AirParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, BlockPos pos) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.fan = (FanBlockEntity) level.getBlockEntity(pos);
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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
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

        @Nullable
        @Override
        public Particle createParticle(AirParticleData data, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
            AirParticle particle = new AirParticle(level, x, y, z, mx, my, mz, data.pos);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
