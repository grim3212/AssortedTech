package com.grim3212.assorted.tech.common.data;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.util.TechTags;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class TechEntityTagProvider extends EntityTypeTagsProvider {

	public TechEntityTagProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, AssortedTech.MODID, existingFileHelper);
	}

	@Override
	protected void addTags() {
		this.tag(TechTags.Entities.SENSORS_NETHER).add(EntityType.BLAZE, EntityType.GHAST, EntityType.HOGLIN, EntityType.MAGMA_CUBE, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.STRIDER, EntityType.WITHER_SKELETON, EntityType.ZOGLIN, EntityType.ZOMBIFIED_PIGLIN);
		this.tag(TechTags.Entities.SENSORS_END).add(EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ENDER_DRAGON, EntityType.SHULKER);
		this.tag(TechTags.Entities.SENSORS_ARTHROPODS).add(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BEE, EntityType.ENDERMITE, EntityType.SILVERFISH);
		this.tag(TechTags.Entities.SENSORS_WATER).add(EntityType.AXOLOTL, EntityType.TURTLE, EntityType.DOLPHIN, EntityType.ELDER_GUARDIAN, EntityType.GLOW_SQUID, EntityType.GUARDIAN, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.SQUID, EntityType.TROPICAL_FISH, EntityType.DROWNED);
		this.tag(TechTags.Entities.SENSORS_PETS).add(EntityType.CAT, EntityType.DONKEY, EntityType.HORSE, EntityType.LLAMA, EntityType.MULE, EntityType.PARROT, EntityType.SKELETON_HORSE, EntityType.WOLF, EntityType.ZOMBIE_HORSE);
		this.tag(TechTags.Entities.SENSORS_FLYING).add(EntityType.BAT, EntityType.BEE, EntityType.BLAZE, EntityType.CHICKEN, EntityType.ENDER_DRAGON, EntityType.GHAST, EntityType.PARROT, EntityType.PHANTOM, EntityType.VEX, EntityType.WITHER);
	}

	@Override
	public String getName() {
		return "Assorted Tech entity tags";
	}

}
