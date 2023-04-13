package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.data.LibEntityTagProvider;
import com.grim3212.assorted.tech.api.TechTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TechEntityTagProvider extends LibEntityTagProvider {

    public TechEntityTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    public void addCommonTags(Function<TagKey<EntityType<?>>, IntrinsicTagAppender<EntityType<?>>> tagger) {
        tagger.apply(TechTags.Entities.SENSORS_NETHER).add(EntityType.BLAZE, EntityType.GHAST, EntityType.HOGLIN, EntityType.MAGMA_CUBE, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.STRIDER, EntityType.WITHER_SKELETON, EntityType.ZOGLIN, EntityType.ZOMBIFIED_PIGLIN);
        tagger.apply(TechTags.Entities.SENSORS_END).add(EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.SHULKER);
        tagger.apply(TechTags.Entities.SENSORS_ARTHROPODS).add(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BEE, EntityType.ENDERMITE, EntityType.SILVERFISH);
        tagger.apply(TechTags.Entities.SENSORS_WATER).add(EntityType.AXOLOTL, EntityType.TURTLE, EntityType.DOLPHIN, EntityType.ELDER_GUARDIAN, EntityType.GLOW_SQUID, EntityType.GUARDIAN, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.SQUID, EntityType.TROPICAL_FISH, EntityType.DROWNED);
        tagger.apply(TechTags.Entities.SENSORS_PETS).add(EntityType.CAT, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA, EntityType.MULE, EntityType.PARROT, EntityType.SKELETON_HORSE, EntityType.WOLF, EntityType.ZOMBIE_HORSE);
        tagger.apply(TechTags.Entities.SENSORS_FLYING).add(EntityType.BAT, EntityType.BEE, EntityType.BLAZE, EntityType.CHICKEN, EntityType.ENDER_DRAGON, EntityType.GHAST, EntityType.PARROT, EntityType.PHANTOM, EntityType.VEX, EntityType.WITHER);
    }

}
