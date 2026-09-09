package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.data.LibEntityTagProvider;
import com.grim3212.assorted.tech.api.TechTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TechEntityTagProvider extends LibEntityTagProvider {

    public TechEntityTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    public void addCommonTags(Function<TagKey<EntityType<?>>, TagAppender<EntityType<?>>> appender) {
        // The intrinsic tag appender is gone; TagAppender only accepts ResourceKeys. Wrap it back
        // into something that takes objects so the tag lists below stay readable.
        // Note the constants also moved: EntityType.BLAZE is EntityTypes.BLAZE now, the same split
        // into a separate holder class that Items and Blocks already had.
        Function<TagKey<EntityType<?>>, EntityTagger> tagger = (tag) -> new EntityTagger(appender.apply(tag));

        tagger.apply(TechTags.Entities.SENSORS_NETHER).add(EntityTypes.BLAZE, EntityTypes.GHAST, EntityTypes.HOGLIN, EntityTypes.MAGMA_CUBE, EntityTypes.PIGLIN, EntityTypes.PIGLIN_BRUTE, EntityTypes.STRIDER, EntityTypes.WITHER_SKELETON, EntityTypes.ZOGLIN, EntityTypes.ZOMBIFIED_PIGLIN);
        tagger.apply(TechTags.Entities.SENSORS_END).add(EntityTypes.ENDERMAN, EntityTypes.ENDERMITE, EntityTypes.ENDER_DRAGON, EntityTypes.SHULKER);
        tagger.apply(TechTags.Entities.SENSORS_ARTHROPODS).add(EntityTypes.SPIDER, EntityTypes.CAVE_SPIDER, EntityTypes.BEE, EntityTypes.ENDERMITE, EntityTypes.SILVERFISH);
        tagger.apply(TechTags.Entities.SENSORS_WATER).add(EntityTypes.AXOLOTL, EntityTypes.TURTLE, EntityTypes.DOLPHIN, EntityTypes.ELDER_GUARDIAN, EntityTypes.GLOW_SQUID, EntityTypes.GUARDIAN, EntityTypes.PUFFERFISH, EntityTypes.SALMON, EntityTypes.SQUID, EntityTypes.TROPICAL_FISH, EntityTypes.DROWNED);
        tagger.apply(TechTags.Entities.SENSORS_PETS).add(EntityTypes.CAT, EntityTypes.DONKEY, EntityTypes.HORSE, EntityTypes.LLAMA, EntityTypes.MULE, EntityTypes.PARROT, EntityTypes.SKELETON_HORSE, EntityTypes.WOLF, EntityTypes.ZOMBIE_HORSE);
        tagger.apply(TechTags.Entities.SENSORS_FLYING).add(EntityTypes.BAT, EntityTypes.BEE, EntityTypes.BLAZE, EntityTypes.CHICKEN, EntityTypes.ENDER_DRAGON, EntityTypes.GHAST, EntityTypes.PARROT, EntityTypes.PHANTOM, EntityTypes.VEX, EntityTypes.WITHER);
    }

    private record EntityTagger(TagAppender<EntityType<?>> appender) {

        EntityTagger add(EntityType... values) {
            for (EntityType value : values) {
                this.appender.add(BuiltInRegistries.ENTITY_TYPE.getResourceKey(value).orElseThrow());
            }

            return this;
        }

        EntityTagger addTag(TagKey<EntityType<?>> tag) {
            this.appender.addTag(tag);
            return this;
        }

        EntityTagger addOptionalTag(TagKey<EntityType<?>> tag) {
            this.appender.addOptionalTag(tag);
            return this;
        }
    }
}
