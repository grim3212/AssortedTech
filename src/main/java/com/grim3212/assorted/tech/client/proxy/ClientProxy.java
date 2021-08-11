package com.grim3212.assorted.tech.client.proxy;

import com.grim3212.assorted.tech.client.blockentity.SensorBlockEntityRenderer;
import com.grim3212.assorted.tech.client.particle.AirParticleType;
import com.grim3212.assorted.tech.client.particle.TechParticleTypes;
import com.grim3212.assorted.tech.client.screen.FanScreen;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.proxy.IProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientProxy implements IProxy {

	@Override
	public void starting() {
		final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::setupClient);
		modBus.addListener(this::registerParticleFactories);
	}

	private void registerParticleFactories(final ParticleFactoryRegisterEvent event) {
		Minecraft.getInstance().particleEngine.register(TechParticleTypes.AIR.get(), AirParticleType.Provider::new);
	}

	private void setupClient(final FMLClientSetupEvent event) {
		RenderTypeLookup.setRenderLayer(TechBlocks.FLIP_FLOP_TORCH.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(TechBlocks.FLIP_FLOP_WALL_TORCH.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(TechBlocks.GLOWSTONE_TORCH.get(), RenderType.cutout());
		RenderTypeLookup.setRenderLayer(TechBlocks.GLOWSTONE_WALL_TORCH.get(), RenderType.cutout());

		TechBlocks.SPIKES.forEach((spike) -> RenderTypeLookup.setRenderLayer(spike.get(), RenderType.cutout()));

		ClientRegistry.bindTileEntityRenderer(TechBlockEntityTypes.SENSOR.get(), SensorBlockEntityRenderer::new);
	}

	@Override
	public void openFanScreen(FanBlockEntity fan) {
		Minecraft.getInstance().setScreen(new FanScreen(fan));
	}
}
