package com.grim3212.assorted.tech.common.sounds;

import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.registry.RegistryProvider;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class TechSounds {

    public static final RegistryProvider<SoundEvent> SOUNDS = RegistryProvider.create(Registries.SOUND_EVENT, Constants.MOD_ID);

    public static final IRegistryObject<SoundEvent> SPIKE_DEPLOY = registerSound("spike_deploy");
    public static final IRegistryObject<SoundEvent> SPIKE_CLOSE = registerSound("spike_close");

    public static final IRegistryObject<SoundEvent> ALARM_A = registerSound("alarm_a");
    public static final IRegistryObject<SoundEvent> ALARM_B = registerSound("alarm_b");
    public static final IRegistryObject<SoundEvent> ALARM_C = registerSound("alarm_c");
    public static final IRegistryObject<SoundEvent> ALARM_D = registerSound("alarm_d");
    public static final IRegistryObject<SoundEvent> ALARM_E = registerSound("alarm_e");
    public static final IRegistryObject<SoundEvent> ALARM_F = registerSound("alarm_f");
    public static final IRegistryObject<SoundEvent> ALARM_G = registerSound("alarm_g");
    public static final IRegistryObject<SoundEvent> ALARM_H = registerSound("alarm_h");
    public static final IRegistryObject<SoundEvent> ALARM_I = registerSound("alarm_i");
    public static final IRegistryObject<SoundEvent> ALARM_J = registerSound("alarm_j");
    public static final IRegistryObject<SoundEvent> ALARM_K = registerSound("alarm_k");
    public static final IRegistryObject<SoundEvent> ALARM_L = registerSound("alarm_l");
    public static final IRegistryObject<SoundEvent> ALARM_M = registerSound("alarm_m");
    public static final IRegistryObject<SoundEvent> ALARM_N = registerSound("alarm_n");

    private static IRegistryObject<SoundEvent> registerSound(String name) {
        ResourceLocation loc = new ResourceLocation(Constants.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(loc));
    }

    public static void init() {
    }
}
