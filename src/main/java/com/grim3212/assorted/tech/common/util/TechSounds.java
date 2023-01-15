package com.grim3212.assorted.tech.common.util;

import com.grim3212.assorted.tech.AssortedTech;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TechSounds {

	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AssortedTech.MODID);

	public static final RegistryObject<SoundEvent> SPIKE_DEPLOY = registerSound("spike_deploy");
	public static final RegistryObject<SoundEvent> SPIKE_CLOSE = registerSound("spike_close");

	public static final RegistryObject<SoundEvent> ALARM_A = registerSound("alarm_a");
	public static final RegistryObject<SoundEvent> ALARM_B = registerSound("alarm_b");
	public static final RegistryObject<SoundEvent> ALARM_C = registerSound("alarm_c");
	public static final RegistryObject<SoundEvent> ALARM_D = registerSound("alarm_d");
	public static final RegistryObject<SoundEvent> ALARM_E = registerSound("alarm_e");
	public static final RegistryObject<SoundEvent> ALARM_F = registerSound("alarm_f");
	public static final RegistryObject<SoundEvent> ALARM_G = registerSound("alarm_g");
	public static final RegistryObject<SoundEvent> ALARM_H = registerSound("alarm_h");
	public static final RegistryObject<SoundEvent> ALARM_I = registerSound("alarm_i");
	public static final RegistryObject<SoundEvent> ALARM_J = registerSound("alarm_j");
	public static final RegistryObject<SoundEvent> ALARM_K = registerSound("alarm_k");
	public static final RegistryObject<SoundEvent> ALARM_L = registerSound("alarm_l");
	public static final RegistryObject<SoundEvent> ALARM_M = registerSound("alarm_m");
	public static final RegistryObject<SoundEvent> ALARM_N = registerSound("alarm_n");

	private static RegistryObject<SoundEvent> registerSound(String name) {
		ResourceLocation loc = new ResourceLocation(AssortedTech.MODID, name);
		return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(loc));
	}
}
