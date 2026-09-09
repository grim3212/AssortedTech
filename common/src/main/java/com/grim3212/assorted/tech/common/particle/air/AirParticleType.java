package com.grim3212.assorted.tech.common.particle.air;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * {@link ParticleType} no longer takes a {@code ParticleOptions.Deserializer}; it takes only the
 * override-limiter flag and requires a {@link MapCodec} and a {@link StreamCodec} instead.
 */
public class AirParticleType extends ParticleType<AirParticleData> {

    public AirParticleType() {
        super(true);
    }

    @Override
    public MapCodec<AirParticleData> codec() {
        return AirParticleData.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, AirParticleData> streamCodec() {
        return AirParticleData.STREAM_CODEC;
    }
}
