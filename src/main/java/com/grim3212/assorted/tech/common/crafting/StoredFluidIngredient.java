package com.grim3212.assorted.tech.common.crafting;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.grim3212.assorted.tech.common.util.TechTags;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

/**
 * A basic ingredient that can handle validating if an item contains enough of a
 * fluid
 * 
 * Since each Assorted mod is supposed to be able to be ran independently this
 * is duplicated in a couple of them
 */
public class StoredFluidIngredient extends AbstractIngredient {
	private final TagKey<Item> itemTag;
	private final TagKey<Fluid> fluidTag;
	private final int amount;

	private ItemStack[] itemStacks;

	protected StoredFluidIngredient(@Nullable TagKey<Item> itemTag, TagKey<Fluid> fluidTag) {
		this(itemTag, fluidTag, FluidType.BUCKET_VOLUME);
	}

	protected StoredFluidIngredient(@Nullable TagKey<Item> itemTag, TagKey<Fluid> fluidTag, int amount) {
		super(itemTag != null ? Stream.of(new Ingredient.TagValue(itemTag)) : Stream.of());
		this.itemTag = itemTag;
		this.fluidTag = fluidTag;
		this.amount = amount;
	}

	public static StoredFluidIngredient of(@Nullable TagKey<Item> itemTag, TagKey<Fluid> fluidTag) {
		return new StoredFluidIngredient(itemTag, fluidTag);
	}

	public static StoredFluidIngredient of(@Nullable TagKey<Item> itemTag, TagKey<Fluid> fluidTag, int amount) {
		return new StoredFluidIngredient(itemTag, fluidTag, amount);
	}

	@Override
	public boolean test(@Nullable ItemStack input) {
		if (input == null)
			return false;
		// If the itemTag is null just check the fluid
		// If the itemTag isn't null check both the tag and the fluid
		return ((itemTag != null && input.is(itemTag)) || itemTag == null) && doesInputMatchFluid(input);
	}

	private boolean doesInputMatchFluid(ItemStack stack) {
		// Can we drain at least the amount given from this stack
		Optional<FluidStack> fluidStack = FluidUtil.getFluidHandler(stack).map(handler -> handler.drain(this.amount, FluidAction.SIMULATE));
		if (fluidStack.isPresent()) {
			FluidStack fluid = fluidStack.get();
			return fluid.isEmpty() ? false : fluid.getFluid().defaultFluidState().is(this.fluidTag);
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	protected void invalidate() {
		this.itemStacks = null;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", CraftingHelper.getID(Serializer.INSTANCE).toString());
		if (this.itemTag != null)
			json.addProperty("item", this.itemTag.location().toString());
		json.addProperty("fluid", this.fluidTag.location().toString());
		if (this.amount > 0)
			json.addProperty("amount", this.amount);
		return json;
	}

	@Override
	public ItemStack[] getItems() {
		if (this.itemStacks == null) {
			List<ItemStack> stacks = Lists.newArrayList();

			List<Fluid> fluids = Lists.newArrayList();
			ITagManager<Fluid> fluidTags = ForgeRegistries.FLUIDS.tags();
			fluidTags.getTag(this.fluidTag).forEach(fluids::add);
			ITagManager<Item> itemTags = ForgeRegistries.ITEMS.tags();
			List<Item> items = Lists.newArrayList();
			itemTags.getTag(TechTags.Items.FLUID_CONTAINERS).forEach(items::add);

			for (Fluid fluid : fluids) {
				for (Item itm : items) {
					FluidStack fluidStack = new FluidStack(fluid, Integer.MAX_VALUE);
					FluidUtil.getFluidHandler(new ItemStack(itm)).ifPresent(fluidHandler -> {
						int filledAmount = fluidHandler.fill(fluidStack, FluidAction.EXECUTE);
						if (filledAmount >= FluidType.BUCKET_VOLUME) {
							stacks.add(fluidHandler.getContainer());
						}
					});
				}
			}
			this.itemStacks = stacks.toArray(new ItemStack[0]);
		}
		return this.itemStacks;
	}

	public static class Serializer implements IIngredientSerializer<StoredFluidIngredient> {
		public static final Serializer INSTANCE = new Serializer();

		@Override
		public StoredFluidIngredient parse(JsonObject json) {
			TagKey<Item> itemTag = null;
			if (json.has("item")) {
				ResourceLocation itemLocation = new ResourceLocation(GsonHelper.getAsString(json, "item"));
				itemTag = TagKey.create(Registries.ITEM, itemLocation);
			}

			ResourceLocation fluidLocation = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
			TagKey<Fluid> fluidTag = TagKey.create(Registries.FLUID, fluidLocation);

			int amount = FluidType.BUCKET_VOLUME;
			if (json.has("amount")) {
				amount = GsonHelper.getAsInt(json, "amount");
			}

			if (fluidTag == null) {
				throw new JsonSyntaxException("Must set 'fluid'");
			}

			return new StoredFluidIngredient(itemTag, fluidTag, amount);
		}

		@Override
		public StoredFluidIngredient parse(FriendlyByteBuf buffer) {
			boolean hasItemTag = buffer.readBoolean();
			TagKey<Item> itemTag = null;

			if (hasItemTag) {
				itemTag = TagKey.create(Registries.ITEM, buffer.readResourceLocation());
			}
			TagKey<Fluid> fluidTag = TagKey.create(Registries.FLUID, buffer.readResourceLocation());
			int amount = buffer.readInt();
			return new StoredFluidIngredient(itemTag, fluidTag, amount);
		}

		@Override
		public void write(FriendlyByteBuf buffer, StoredFluidIngredient ingredient) {
			if (ingredient.itemTag != null) {
				buffer.writeBoolean(true);
				buffer.writeResourceLocation(ingredient.itemTag.location());
			} else {
				buffer.writeBoolean(false);
			}
			buffer.writeResourceLocation(ingredient.fluidTag.location());
			buffer.writeInt(ingredient.amount);
		}
	}
}