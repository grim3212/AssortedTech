package com.grim3212.assorted.tech.client.particle;

import com.mojang.serialization.Codec;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.ParticleType;

public class AirParticleType extends ParticleType<AirParticleData> {

	public AirParticleType() {
		super(true, AirParticleData.DESERIALIZER);
	}

	@Override
	public Codec<AirParticleData> codec() {
		return AirParticleData.CODEC;
	}

	public static class Provider implements IParticleFactory<AirParticleData> {
		private final IAnimatedSprite sprite;

		public Provider(IAnimatedSprite sprite) {
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(AirParticleData data, ClientWorld level, double x, double y, double z, double mx, double my, double mz) {
			AirParticle particle = new AirParticle(level, x, y, z, mx, my, mz, data.pos);
			particle.pickSprite(sprite);
			return particle;
		}
	}
}
