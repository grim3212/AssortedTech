package com.grim3212.assorted.tech.common.particle;

import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.particle.air.AirParticleData;
import com.grim3212.assorted.tech.common.particle.air.AirParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;

public class TechParticleTypes {

    public static final RegistryProvider<ParticleType<?>> PARTICLE_TYPES = RegistryProvider.create(Registries.PARTICLE_TYPE, Constants.MOD_ID);

    public static final IRegistryObject<ParticleType<AirParticleData>> AIR = PARTICLE_TYPES.register("air", () -> new AirParticleType());

    public static void init() {
    }
}
