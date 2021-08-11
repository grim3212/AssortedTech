package com.grim3212.assorted.tech.client.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class AirParticleData implements IParticleData {

	public static final Codec<AirParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(BlockPos.CODEC.fieldOf("pos").forGetter(d -> d.pos)).apply(instance, AirParticleData::new));

	public final BlockPos pos;

	public AirParticleData(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public ParticleType<?> getType() {
		return TechParticleTypes.AIR.get();
	}

	@Override
	public void writeToNetwork(PacketBuffer buf) {
		buf.writeBlockPos(pos);
	}

	@Override
	public String writeToString() {
		double d0 = (double) pos.getX();
		double d1 = (double) pos.getY();
		double d2 = (double) pos.getZ();
		return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getKey(this.getType()), d0, d1, d2);
	}

	public static final IDeserializer<AirParticleData> DESERIALIZER = new IParticleData.IDeserializer<AirParticleData>() {
		@Override
		public AirParticleData fromCommand(ParticleType<AirParticleData> type, StringReader reader) throws CommandSyntaxException {
			reader.expect(' ');
			double xPos = reader.readDouble();
			reader.expect(' ');
			double yPos = reader.readDouble();
			reader.expect(' ');
			double zPos = reader.readDouble();

			return new AirParticleData(new BlockPos(xPos, yPos, zPos));
		}

		@Override
		public AirParticleData fromNetwork(ParticleType<AirParticleData> type, PacketBuffer buf) {
			return new AirParticleData(buf.readBlockPos());
		}
	};
}
