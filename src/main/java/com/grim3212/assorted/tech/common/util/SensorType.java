package com.grim3212.assorted.tech.common.util;

import java.util.function.Predicate;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public enum SensorType {
	WOOD(Material.WOOD, SoundType.WOOD, Entity.class),
	STONE(SoundType.STONE, LivingEntity.class),
	IRON(Material.METAL, SoundType.METAL, Player.class),
	MOSSY_COBBLESTONE(SoundType.STONE, Monster.class),
	PRISMARINE(SoundType.STONE, (ent) -> {
		return ent instanceof WaterAnimal || ent.getType().is(TechTags.Entities.SENSORS_WATER);
	}),
	GOLD(Material.METAL, SoundType.METAL, ItemEntity.class),
	EMERALD(Material.METAL, SoundType.METAL, AbstractVillager.class),
	NETHERRACK(SoundType.STONE, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_NETHER);
	}),
	COBWEB(SoundType.STONE, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_ARTHROPODS);
	}),
	END_STONE(SoundType.STONE, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_END);
	}),
	HAY_BALE(Material.GRASS, SoundType.GRASS, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_PETS);
	}),
	FEATHER(Material.WOOL, SoundType.WOOL, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_FLYING);
	});

	private final SoundType soundType;
	private final Predicate<? super Entity> trigger;
	private final Material material;

	private SensorType(SoundType soundType, Class<? extends Entity> trigger) {
		this(Material.STONE, soundType, trigger);
	}

	private SensorType(Material material, SoundType soundType, Class<? extends Entity> trigger) {
		this.soundType = soundType;
		this.trigger = (ent) -> {
			return trigger.isInstance(ent);
		};
		this.material = material;
	}

	private SensorType(SoundType soundType, Predicate<? super Entity> trigger) {
		this(Material.STONE, soundType, trigger);
	}

	private SensorType(Material material, SoundType soundType, Predicate<? super Entity> trigger) {
		this.soundType = soundType;
		this.trigger = trigger;
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}

	public SoundType getSoundType() {
		return soundType;
	}

	public Predicate<? super Entity> getTrigger() {
		return trigger;
	}
}
