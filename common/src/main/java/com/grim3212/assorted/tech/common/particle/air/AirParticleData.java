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
 * The options carried by the fan's air particle: the position of the fan that spawned it, which the
 * particle reads back so it can follow the fan's range and mode.
 * <p>
 * {@link ParticleOptions} is down to a single {@code getType()} method in 26.2 -
 * {@code writeToNetwork}, {@code writeToString} and the {@code Deserializer} inner interface are all
 * gone. Serialization is entirely a {@link MapCodec} (for the {@code /particle} command and any data
 * that names a particle) plus a {@link StreamCodec} (for the wire), both handed to the game by
 * {@link AirParticleType}.
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
