package com.grim3212.assorted.tech.common.util;

import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.Tags;

public enum SensorType {
	WOOD(Material.WOOD, SoundType.WOOD, Entity.class, () -> {
		return Ingredient.of(ItemTags.PLANKS);
	}),
	STONE(Material.STONE, SoundType.STONE, LivingEntity.class, () -> {
		return Ingredient.of(Tags.Items.STONE);
	}),
	IRON(Material.METAL, SoundType.METAL, Player.class, () -> {
		return Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON);
	}),
	MOSSY_COBBLESTONE(Material.STONE, SoundType.STONE, Monster.class, () -> {
		return Ingredient.of(Blocks.MOSSY_COBBLESTONE);
	}),
	PRISMARINE(Material.STONE, SoundType.STONE, (ent) -> {
		return ent instanceof WaterAnimal || ent.getType().is(TechTags.Entities.SENSORS_WATER);
	}, () -> {
		return Ingredient.of(Blocks.PRISMARINE);
	}),
	GOLD(Material.METAL, SoundType.METAL, ItemEntity.class, () -> {
		return Ingredient.of(Tags.Items.STORAGE_BLOCKS_GOLD);
	}),
	EMERALD(Material.METAL, SoundType.METAL, AbstractVillager.class, () -> {
		return Ingredient.of(Tags.Items.STORAGE_BLOCKS_EMERALD);
	}),
	NETHERRACK(Material.STONE, SoundType.STONE, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_NETHER);
	}, () -> {
		return Ingredient.of(Tags.Items.NETHERRACK);
	}),
	COBWEB(Material.STONE, SoundType.STONE, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_ARTHROPODS);
	}, () -> {
		return Ingredient.of(Tags.Items.STRING);
	}),
	END_STONE(Material.STONE, SoundType.STONE, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_END);
	}, () -> {
		return Ingredient.of(Tags.Items.END_STONES);
	}),
	HAY_BALE(Material.GRASS, SoundType.GRASS, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_PETS);
	}, () -> {
		return Ingredient.of(Blocks.HAY_BLOCK);
	}),
	FEATHER(Material.WOOL, SoundType.WOOL, (ent) -> {
		return ent.getType().is(TechTags.Entities.SENSORS_FLYING);
	}, () -> {
		return Ingredient.of(Tags.Items.FEATHERS);
	});

	private final SoundType soundType;
	private final Predicate<? super Entity> trigger;
	private final Material material;
	private final Supplier<Ingredient> craftingMaterial;

	private SensorType(Material material, SoundType soundType, Class<? extends Entity> trigger, Supplier<Ingredient> craftingMaterial) {
		this.soundType = soundType;
		this.trigger = (ent) -> {
			return trigger.isInstance(ent);
		};
		this.material = material;
		this.craftingMaterial = craftingMaterial;
	}

	private SensorType(Material material, SoundType soundType, Predicate<? super Entity> trigger, Supplier<Ingredient> craftingMaterial) {
		this.soundType = soundType;
		this.trigger = trigger;
		this.material = material;
		this.craftingMaterial = craftingMaterial;
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

	public Ingredient getCraftingMaterial() {
		return craftingMaterial.get();
	}

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
