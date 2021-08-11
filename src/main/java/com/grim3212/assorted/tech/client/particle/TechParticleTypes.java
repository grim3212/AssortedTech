package com.grim3212.assorted.tech.client.particle;

import com.grim3212.assorted.tech.AssortedTech;

import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TechParticleTypes {

	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, AssortedTech.MODID);

	public static final RegistryObject<ParticleType<AirParticleData>> AIR = PARTICLE_TYPES.register("air", () -> new AirParticleType());
}
