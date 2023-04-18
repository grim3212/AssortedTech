package com.grim3212.assorted.tech.common.particle.air;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.common.particle.TechParticleTypes;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

public class AirParticleData implements ParticleOptions {

    public static final Codec<AirParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(d -> d.pos)).apply(instance, AirParticleData::new));

    public final BlockPos pos;

    public AirParticleData() {
        this(BlockPos.ZERO);
    }

    public AirParticleData(BlockPos pos) {
        this.pos = pos;
    }

    public static Codec<AirParticleData> getCodec() {
        return CODEC;
    }

    @Override
    public ParticleType<?> getType() {
        return TechParticleTypes.AIR.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public String writeToString() {
        double d0 = pos.getX();
        double d1 = pos.getY();
        double d2 = pos.getZ();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", Services.PLATFORM.getRegistry(Registries.PARTICLE_TYPE).getRegistryName(this.getType()), d0, d1, d2);
    }

    public static final Deserializer<AirParticleData> DESERIALIZER = new ParticleOptions.Deserializer<AirParticleData>() {
        @Override
        public AirParticleData fromCommand(ParticleType<AirParticleData> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            double xPos = reader.readDouble();
            reader.expect(' ');
            double yPos = reader.readDouble();
            reader.expect(' ');
            double zPos = reader.readDouble();

            return new AirParticleData(BlockPos.containing(xPos, yPos, zPos));
        }

        @Override
        public AirParticleData fromNetwork(ParticleType<AirParticleData> type, FriendlyByteBuf buf) {
            return new AirParticleData(buf.readBlockPos());
        }
    };
}
