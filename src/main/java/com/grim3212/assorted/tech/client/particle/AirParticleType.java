package com.grim3212.assorted.tech.client.particle;

import com.mojang.serialization.Codec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;

public class AirParticleType extends ParticleType<AirParticleData> {

	public AirParticleType() {
		super(true, AirParticleData.DESERIALIZER);
	}

	@Override
	public Codec<AirParticleData> codec() {
		return AirParticleData.CODEC;
	}

	public static class Provider implements SpriteParticleRegistration<AirParticleData> {
		@Override
		public ParticleProvider<AirParticleData> create(SpriteSet set) {
			return new ParticleProvider<AirParticleData>() {
				@Override
				public Particle createParticle(AirParticleData data, ClientLevel level, double x, double y, double z, double mx, double my, double mz) {
					AirParticle particle = new AirParticle(level, x, y, z, mx, my, mz, data.pos);
					particle.pickSprite(set);
					return particle;
				}
				
			};
		}
	}
}
