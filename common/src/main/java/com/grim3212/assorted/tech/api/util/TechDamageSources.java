package com.grim3212.assorted.tech.api.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;

public class TechDamageSources {

    public static DamageSource spike(Entity entity) {
        return (new EntityDamageSource("assortedtech.spike", entity));
    }

    public static DamageSource laser(Entity entity) {
        return (new EntityDamageSource("assortedtech.laser", entity));
    }

}
