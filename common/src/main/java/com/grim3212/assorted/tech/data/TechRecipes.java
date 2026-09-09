package com.grim3212.assorted.tech.data;

import com.grim3212.assorted.lib.core.conditions.ConditionalRecipeProvider;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.lib.util.LibCommonTags;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.crafting.TechConditions;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class TechRecipes extends ConditionalRecipeProvider {

    private final HolderGetter<Item> items;

    public TechRecipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output, Constants.MOD_ID);
        this.items = registries.lookupOrThrow(Registries.ITEM);
    }

    @Override
    public void registerConditions() {
        this.addConditions(partEnabled(TechConditions.Parts.ALARM), TechBlocks.ALARM.getId());
        this.addConditions(partEnabled(TechConditions.Parts.FAN), TechBlocks.FAN.getId());
        this.addConditions(partEnabled(TechConditions.Parts.TORCHES), TechBlocks.FLIP_FLOP_TORCH.getId(), TechBlocks.GLOWSTONE_TORCH.getId());
        this.addConditions(partEnabled(TechConditions.Parts.BRIDGES), TechBlocks.BRIDGE_CONTROL_LASER.getId(), TechBlocks.BRIDGE_CONTROL_ACCEL.getId(), TechBlocks.BRIDGE_CONTROL_TRICK.getId(), TechBlocks.BRIDGE_CONTROL_DEATH.getId(), TechBlocks.BRIDGE_CONTROL_GRAVITY.getId());
        this.addConditions(partEnabled(TechConditions.Parts.GRAVITY), TechBlocks.ATTRACTOR.getId(), TechBlocks.GRAVITOR.getId(), TechBlocks.REPULSOR.getId(), TechBlocks.ATTRACTOR_DIRECTIONAL.getId(), TechBlocks.GRAVITOR_DIRECTIONAL.getId(), TechBlocks.REPULSOR_DIRECTIONAL.getId());
    }

    @Override
    public void buildRecipes() {
        super.buildRecipes();

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechItems.FLIP_FLOP_TORCH.get(), 1).define('X', LibCommonTags.Items.RODS_WOODEN).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('P', LibCommonTags.Items.DYES_BLUE).pattern("P").pattern("X").pattern("R").unlockedBy("has_redstone", has(LibCommonTags.Items.DUSTS_REDSTONE)).save(this.output, recipeKey(TechItems.FLIP_FLOP_TORCH.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechItems.GLOWSTONE_TORCH.get(), 1).define('X', LibCommonTags.Items.RODS_WOODEN).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('P', LibCommonTags.Items.DUSTS_GLOWSTONE).pattern("P").pattern("X").pattern("R").unlockedBy("has_redstone", has(LibCommonTags.Items.DUSTS_REDSTONE)).save(this.output, recipeKey(TechItems.GLOWSTONE_TORCH.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.FAN.get(), 1).define('X', ItemTags.PLANKS).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("XIX").pattern("XRX").pattern("XXX").unlockedBy("has_redstone", has(LibCommonTags.Items.DUSTS_REDSTONE)).save(this.output, recipeKey(TechBlocks.FAN.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.ALARM.get(), 1).define('X', Items.LEVER).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("III").pattern("IRI").pattern("IXI").unlockedBy("has_redstone", has(LibCommonTags.Items.DUSTS_REDSTONE)).save(this.output, recipeKey(TechBlocks.ALARM.getId()));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_LASER.get(), 1).define('R', LibCommonTags.Items.SLIMEBALLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("III").pattern("IRI").pattern("III").unlockedBy("has_slime", has(LibCommonTags.Items.SLIMEBALLS)).save(this.output, recipeKey(TechBlocks.BRIDGE_CONTROL_LASER.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_ACCEL.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', fluid(FluidTags.WATER)).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get())).save(this.output, recipeKey(TechBlocks.BRIDGE_CONTROL_ACCEL.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_TRICK.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', LibCommonTags.Items.FEATHERS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get())).save(this.output, recipeKey(TechBlocks.BRIDGE_CONTROL_TRICK.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_DEATH.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', fluid(FluidTags.LAVA)).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get())).save(this.output, recipeKey(TechBlocks.BRIDGE_CONTROL_DEATH.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.BRIDGE_CONTROL_GRAVITY.get(), 1).define('L', TechBlocks.BRIDGE_CONTROL_LASER.get()).define('R', LibCommonTags.Items.ENDER_PEARLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("III").pattern("ILI").pattern("IRI").unlockedBy("has_laser_bridge", has(TechBlocks.BRIDGE_CONTROL_LASER.get())).save(this.output, recipeKey(TechBlocks.BRIDGE_CONTROL_GRAVITY.getId()));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, TechItems.GRAVITY_BOOTS.get(), 1).define('I', LibCommonTags.Items.INGOTS_IRON).define('A', TechBlocks.ATTRACTOR.get()).pattern("I I").pattern("A A").unlockedBy("has_attractor", has(TechBlocks.ATTRACTOR.get())).save(this.output, recipeKey(TechItems.GRAVITY_BOOTS.getId()));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.ATTRACTOR.get(), 1).define('C', Items.COMPASS).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('E', LibCommonTags.Items.ENDER_PEARLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("IRI").pattern("RCR").pattern("IEI").unlockedBy("has_ender_pearl", has(LibCommonTags.Items.ENDER_PEARLS)).save(this.output, recipeKey(TechBlocks.ATTRACTOR.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.GRAVITOR.get(), 1).define('C', Items.COMPASS).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('E', LibCommonTags.Items.ENDER_PEARLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("IRI").pattern(" C ").pattern("IEI").unlockedBy("has_ender_pearl", has(LibCommonTags.Items.ENDER_PEARLS)).save(this.output, recipeKey(TechBlocks.GRAVITOR.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.REPULSOR.get(), 1).define('C', Items.COMPASS).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('E', LibCommonTags.Items.ENDER_PEARLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("RIR").pattern("ICI").pattern("RER").unlockedBy("has_ender_pearl", has(LibCommonTags.Items.ENDER_PEARLS)).save(this.output, recipeKey(TechBlocks.REPULSOR.getId()));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.ATTRACTOR_DIRECTIONAL.get(), 1).define('C', Items.COMPASS).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('E', LibCommonTags.Items.ENDER_PEARLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("IRI").pattern("RCR").pattern(" E ").unlockedBy("has_ender_pearl", has(LibCommonTags.Items.ENDER_PEARLS)).save(this.output, recipeKey(TechBlocks.ATTRACTOR_DIRECTIONAL.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.GRAVITOR_DIRECTIONAL.get(), 1).define('C', Items.COMPASS).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('E', LibCommonTags.Items.ENDER_PEARLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern(" R ").pattern("ICI").pattern(" E ").unlockedBy("has_ender_pearl", has(LibCommonTags.Items.ENDER_PEARLS)).save(this.output, recipeKey(TechBlocks.GRAVITOR_DIRECTIONAL.getId()));
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, TechBlocks.REPULSOR_DIRECTIONAL.get(), 1).define('C', Items.COMPASS).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('E', LibCommonTags.Items.ENDER_PEARLS).define('I', LibCommonTags.Items.INGOTS_IRON).pattern("RIR").pattern("ICI").pattern(" E ").unlockedBy("has_ender_pearl", has(LibCommonTags.Items.ENDER_PEARLS)).save(this.output, recipeKey(TechBlocks.REPULSOR_DIRECTIONAL.getId()));

        for (IRegistryObject<SensorBlock> b : TechBlocks.SENSORS) {
            Ingredient mat = b.get().getSensorType().getCraftingMaterial(this.items);
            this.addConditions(partEnabled(TechConditions.Parts.SENSORS), b.getId());
            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, b.get(), 1).define('X', LibCommonTags.Items.INGOTS_IRON).define('R', LibCommonTags.Items.DUSTS_REDSTONE).define('G', LibCommonTags.Items.GLASS).define('M', mat).pattern("XGX").pattern("MRM").pattern("XMX").unlockedBy("has_redstone", has(LibCommonTags.Items.DUSTS_REDSTONE)).unlockedBy("has_iron", has(LibCommonTags.Items.INGOTS_IRON)).save(this.output, recipeKey(b.getId()));
        }

        for (IRegistryObject<SpikeBlock> b : TechBlocks.SPIKES) {
            TagKey<Item> mat = b.get().getSpikeType().getMaterial();
            this.addConditions(and(partEnabled(TechConditions.Parts.SPIKES), itemTagExists(mat)), b.getId());
            ShapedRecipeBuilder.shaped(this.items, RecipeCategory.REDSTONE, b.get(), 6).define('X', mat).pattern("X X").pattern(" X ").pattern("XXX").unlockedBy("has_material", has(mat)).save(this.output, recipeKey(b.getId()));
        }
    }

    /**
     * Recipes are addressed by {@code ResourceKey<Recipe<?>>} rather than a raw {@code Identifier}
     * since 1.21.2 - the recipe manager keys them and a recipe no longer carries its own id. The
     * conditions map is still keyed by {@code Identifier}, so both forms are needed side by side.
     */
    private static ResourceKey<Recipe<?>> recipeKey(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    /**
     * Recipe providers are not data providers any more - a {@link RecipeProvider.Runner} owns the
     * file writing and builds a fresh provider around the {@link RecipeOutput} it hands out. This is
     * what the loader datagen entry points register.
     */
    public static class Runner extends ConditionalRecipeProvider.Runner {

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries, Constants.MOD_ID);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new TechRecipes(registries, output);
        }

        @Override
        public String getName() {
            return "Recipes: " + Constants.MOD_ID;
        }
    }
}
