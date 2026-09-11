package com.grim3212.assorted.tech.common.particle.air;

import com.grim3212.assorted.tech.common.particle.TechParticleTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;

/**
 * The fan air particle's options: the position of the fan that spawned it, so the particle can
 * follow the fan's range and mode. Serialized by the codecs {@link AirParticleType} provides.
 */
public class AirParticleData implements ParticleOptions {

    /**
     * The command/data form is now the codec's, so an air particle is named
     * {@code assortedtech:air{pos:[x,y,z]}} rather than the old positional
     * {@code assortedtech:air <x> <y> <z>}.
     */
    public static final MapCodec<AirParticleData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(data -> data.pos)
    ).apply(instance, AirParticleData::new));

    public static final StreamCodec<ByteBuf, AirParticleData> STREAM_CODEC = BlockPos.STREAM_CODEC.map(AirParticleData::new, data -> data.pos);

    public final BlockPos pos;

    public AirParticleData(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public ParticleType<AirParticleData> getType() {
        return TechParticleTypes.AIR.get();
    }
}
