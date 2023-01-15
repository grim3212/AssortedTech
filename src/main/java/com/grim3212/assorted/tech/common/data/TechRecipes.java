package com.grim3212.assorted.tech.common.data;

import java.util.function.Consumer;

import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.handler.EnabledCondition;
import com.grim3212.assorted.tech.common.item.TechItems;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.registries.RegistryObject;

public class TechRecipes extends RecipeProvider implements IConditionBuilder {

	public TechRecipes(PackOutput output) {
		super(output);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.TORCHES_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechItems.FLIP_FLOP_TORCH.get(), 1).define('X', Tags.Items.RODS_WOODEN).define('R', Tags.Items.DUSTS_REDSTONE).define('P', Tags.Items.DYES_BLUE).pattern("P").pattern("X").pattern("R").unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))::save).generateAdvancement().build(consumer, TechItems.FLIP_FLOP_TORCH.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.TORCHES_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechItems.GLOWSTONE_TORCH.get(), 1).define('X', Tags.Items.RODS_WOODEN).define('R', Tags.Items.DUSTS_REDSTONE).define('P', Tags.Items.DUSTS_GLOWSTONE).pattern("P").pattern("X").pattern("R").unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))::save).generateAdvancement().build(consumer, TechItems.GLOWSTONE_TORCH.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.FAN_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.FAN.get(), 1).define('X', ItemTags.PLANKS).define('R', Tags.Items.DUSTS_REDSTONE).define('I', Tags.Items.INGOTS_IRON).pattern("XIX").pattern("XRX").pattern("XXX").unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))::save).generateAdvancement().build(consumer, TechBlocks.FAN.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.ALARM_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.ALARM.get(), 1).define('X', Items.LEVER).define('R', Tags.Items.DUSTS_REDSTONE).define('I', Tags.Items.INGOTS_IRON).pattern("III").pattern("IRI").pattern("IXI").unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))::save).generateAdvancement().build(consumer, TechBlocks.ALARM.getId());

		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.BRIDGES_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_LASER.get(), 1).define('R', Tags.Items.SLIMEBALLS).define('I', Tags.Items.INGOTS_IRON).pattern("III").pattern("IRI").pattern("III").unlockedBy("has_slime", has(Tags.Items.SLIMEBALLS))::save).generateAdvancement().build(consumer, TechBlocks.BRIDGE_CONTROL_LASER.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.BRIDGES_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_ACCEL.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', Items.WATER_BUCKET).define('I', Tags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get()))::save).generateAdvancement().build(consumer,
				TechBlocks.BRIDGE_CONTROL_ACCEL.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.BRIDGES_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_TRICK.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', Tags.Items.FEATHERS).define('I', Tags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get()))::save).generateAdvancement().build(consumer,
				TechBlocks.BRIDGE_CONTROL_TRICK.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.BRIDGES_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_DEATH.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', Items.LAVA_BUCKET).define('I', Tags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get()))::save).generateAdvancement().build(consumer,
				TechBlocks.BRIDGE_CONTROL_DEATH.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.BRIDGES_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_GRAVITY.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', Tags.Items.ENDER_PEARLS).define('I', Tags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get()))::save).generateAdvancement()
				.build(consumer, TechBlocks.BRIDGE_CONTROL_GRAVITY.getId());

		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.GRAVITY_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TechItems.GRAVITY_BOOTS.get(), 1).define('I', Tags.Items.INGOTS_IRON).define('A', TechBlocks.ATTRACTOR.get()).pattern("I I").pattern("A A").unlockedBy("has_attractor", has(TechBlocks.ATTRACTOR.get()))::save).generateAdvancement().build(consumer, TechItems.GRAVITY_BOOTS.getId());

		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.GRAVITY_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.ATTRACTOR.get(), 1).define('C', Items.COMPASS).define('R', Tags.Items.DUSTS_REDSTONE).define('E', Tags.Items.ENDER_PEARLS).define('I', Tags.Items.INGOTS_IRON).pattern("IRI").pattern("RCR").pattern("IEI").unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))::save).generateAdvancement().build(consumer,
				TechBlocks.ATTRACTOR.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.GRAVITY_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.GRAVITOR.get(), 1).define('C', Items.COMPASS).define('R', Tags.Items.DUSTS_REDSTONE).define('E', Tags.Items.ENDER_PEARLS).define('I', Tags.Items.INGOTS_IRON).pattern("IRI").pattern(" C ").pattern("IEI").unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))::save).generateAdvancement().build(consumer,
				TechBlocks.GRAVITOR.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.GRAVITY_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.REPULSOR.get(), 1).define('C', Items.COMPASS).define('R', Tags.Items.DUSTS_REDSTONE).define('E', Tags.Items.ENDER_PEARLS).define('I', Tags.Items.INGOTS_IRON).pattern("RIR").pattern("ICI").pattern("RER").unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))::save).generateAdvancement().build(consumer,
				TechBlocks.REPULSOR.getId());

		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.GRAVITY_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.ATTRACTOR_DIRECTIONAL.get(), 1).define('C', Items.COMPASS).define('R', Tags.Items.DUSTS_REDSTONE).define('E', Tags.Items.ENDER_PEARLS).define('I', Tags.Items.INGOTS_IRON).pattern("IRI").pattern("RCR").pattern(" E ").unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))::save).generateAdvancement().build(consumer,
				TechBlocks.ATTRACTOR_DIRECTIONAL.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.GRAVITY_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.GRAVITOR_DIRECTIONAL.get(), 1).define('C', Items.COMPASS).define('R', Tags.Items.DUSTS_REDSTONE).define('E', Tags.Items.ENDER_PEARLS).define('I', Tags.Items.INGOTS_IRON).pattern(" R ").pattern("ICI").pattern(" E ").unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))::save).generateAdvancement().build(consumer,
				TechBlocks.GRAVITOR_DIRECTIONAL.getId());
		ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.GRAVITY_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, TechBlocks.REPULSOR_DIRECTIONAL.get(), 1).define('C', Items.COMPASS).define('R', Tags.Items.DUSTS_REDSTONE).define('E', Tags.Items.ENDER_PEARLS).define('I', Tags.Items.INGOTS_IRON).pattern("RIR").pattern("ICI").pattern(" E ").unlockedBy("has_ender_pearl", has(Tags.Items.ENDER_PEARLS))::save).generateAdvancement().build(consumer,
				TechBlocks.REPULSOR_DIRECTIONAL.getId());

		for (RegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
			Ingredient mat = b.get().getSensorType().getCraftingMaterial();
			ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.SENSORS_CONDITION)).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, b.get(), 1).define('X', Tags.Items.INGOTS_IRON).define('R', Tags.Items.DUSTS_REDSTONE).define('G', Tags.Items.GLASS).define('M', mat).pattern("XGX").pattern("MRM").pattern("XMX").unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE)).unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))::save).generateAdvancement()
					.build(consumer, b.getId());
		}

		for (RegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
			TagKey<Item> mat = b.get().getSpikeType().getMaterial();
			ConditionalRecipe.builder().addCondition(new EnabledCondition(EnabledCondition.SPIKES_CONDITION)).addCondition(not(tagEmpty(mat))).addRecipe(ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, b.get(), 6).define('X', mat).pattern("X X").pattern(" X ").pattern("XXX").unlockedBy("has_material", has(mat))::save).generateAdvancement().build(consumer, b.getId());
		}
	}
}
