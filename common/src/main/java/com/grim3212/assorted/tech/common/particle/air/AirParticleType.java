package com.grim3212.assorted.tech.common.particle.air;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class AirParticleType extends ParticleType<AirParticleData> {

    public AirParticleType() {
        super(true, AirParticleData.DESERIALIZER);
    }

    @Override
    public Codec<AirParticleData> codec() {
        return AirParticleData.CODEC;
    }
}
