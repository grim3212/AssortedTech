package com.grim3212.assorted.tech.common.util;

import com.grim3212.assorted.tech.AssortedTech;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TechSounds {

	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AssortedTech.MODID);

	public static final RegistryObject<SoundEvent> SPIKE_DEPLOY = registerSound("spike_deploy");
	public static final RegistryObject<SoundEvent> SPIKE_CLOSE = registerSound("spike_close");

	private static RegistryObject<SoundEvent> registerSound(String name) {
		ResourceLocation loc = new ResourceLocation(AssortedTech.MODID, name);
		return SOUNDS.register(name, () -> new SoundEvent(loc));
	}
}
