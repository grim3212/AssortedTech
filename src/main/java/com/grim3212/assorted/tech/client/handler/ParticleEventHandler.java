package com.grim3212.assorted.tech.client.handler;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.client.particle.AirParticleType;
import com.grim3212.assorted.tech.client.particle.TechParticleTypes;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = AssortedTech.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class ParticleEventHandler {

	@SubscribeEvent
	public static void initFactories(final RegisterParticleProvidersEvent event) {
		event.register(TechParticleTypes.AIR.get(), new AirParticleType.Provider());
	}
}
