package com.grim3212.assorted.tech.api.util;

import com.grim3212.assorted.tech.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TechDamageTypes {

    public static final ResourceKey<DamageType> SPIKE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "spike"));
    public static final ResourceKey<DamageType> LASER = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, "laser"));

    /**
     * Builds a damage source for one of this mod's damage types.
     * <p>
     * {@code DamageSources#source} is private now - it only exists to seed the vanilla types that
     * class caches - so a modded type has to be turned into a {@link DamageSource} by resolving its
     * {@link ResourceKey} against the level's dynamic damage type registry and calling the public
     * {@link DamageSource} constructor directly.
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return source(level, key, null, null);
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> key, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key), directEntity, causingEntity);
    }
}
