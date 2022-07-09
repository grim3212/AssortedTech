package com.grim3212.assorted.tech.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.util.BridgeType;
import com.grim3212.assorted.tech.common.util.NBTHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

public abstract class BridgeBaseBakedModel extends BakedModelWrapper<BakedModel> {
	protected final ModelBakery bakery;
	protected final Function<Material, TextureAtlasSprite> spriteGetter;
	protected final ModelState transform;
	protected final ResourceLocation name;
	protected final IGeometryBakingContext owner;
	protected final ItemOverrides overrides;
	protected final TextureAtlasSprite baseSprite;

	public BridgeBaseBakedModel(BakedModel bakedBridge, IGeometryBakingContext owner, TextureAtlasSprite baseSprite, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation name) {
		super(bakedBridge);
		this.bakery = bakery;
		this.spriteGetter = spriteGetter;
		this.transform = transform;
		this.name = name;
		this.owner = owner;
		this.overrides = overrides;
		this.baseSprite = baseSprite;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand) {
		return getQuads(state, side, rand, ModelData.EMPTY, RenderType.translucent());
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {
		BlockState blockState = Blocks.AIR.defaultBlockState();
		if (extraData.get(BridgeBlockEntity.BLOCK_STATE) != null) {
			blockState = extraData.get(BridgeBlockEntity.BLOCK_STATE);
		}

		BridgeType type = state.getValue(BridgeBlock.TYPE);

		return this.getCachedModel(type, blockState).getQuads(state, side, rand, extraData, RenderType.translucent());
	}

	protected final Map<BlockState, BakedModel> cache = new HashMap<BlockState, BakedModel>();
	protected BakedModel EMPTY;
	protected BakedModel GRAVITY_EMPTY;

	public BakedModel getCachedModel(BridgeType type, BlockState blockState) {
		if (blockState == null || blockState == Blocks.AIR.defaultBlockState()) {
			if (type == BridgeType.GRAVITY) {
				if (GRAVITY_EMPTY == null) {
					ImmutableMap.Builder<String, String> newTexture = ImmutableMap.builder();
					String texture = "assortedtech:block/bridge_gravity";
					newTexture.put("particle", texture);
					newTexture.put("#stored", texture);
					GRAVITY_EMPTY = generateModel(newTexture.build());
				}
				return GRAVITY_EMPTY;
			} else {
				if (EMPTY == null) {
					ImmutableMap.Builder<String, String> newTexture = ImmutableMap.builder();
					String texture = "assortedtech:block/bridge";
					newTexture.put("particle", texture);
					newTexture.put("#stored", texture);
					EMPTY = generateModel(newTexture.build());
				}
				return EMPTY;
			}
		}

		if (!this.cache.containsKey(blockState)) {
			ImmutableMap.Builder<String, String> newTexture = ImmutableMap.builder();

			String texture = "";
			if (blockState.getBlock() == Blocks.GRASS_BLOCK) {
				texture = "minecraft:block/grass_block_top";
			} else if (blockState.getBlock() == Blocks.PODZOL) {
				texture = "minecraft:block/dirt_podzol_top";
			} else if (blockState.getBlock() == Blocks.MYCELIUM) {
				texture = "minecraft:block/mycelium_top";
			} else {
				BlockModelShaper blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
				TextureAtlasSprite blockTexture = blockModel.getParticleIcon(blockState);
				texture = blockTexture.getName().toString();
			}

			newTexture.put("particle", texture);
			newTexture.put("#stored", texture);
			this.cache.put(blockState, generateModel(newTexture.build()));
		}

		return this.cache.get(blockState);
	}

	protected abstract BakedModel generateModel(ImmutableMap<String, String> texture);

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		BlockState state = data.get(BridgeBlockEntity.BLOCK_STATE);
		if (state == null) {
			return this.baseSprite;
		} else if (state == Blocks.AIR.defaultBlockState()) {
			return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(AssortedTech.MODID, "block/bridge"));
		}
		return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(state);
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return this.getParticleIcon(ModelData.EMPTY);
	}

	public final BridgeItemOverrideList INSTANCE = new BridgeItemOverrideList();

	@Override
	public ItemOverrides getOverrides() {
		return INSTANCE;
	}

	public static final class BridgeItemOverrideList extends ItemOverrides {

		@Override
		public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int field) {
			BridgeBaseBakedModel bridgeModel = (BridgeBaseBakedModel) originalModel;

			if (stack.hasTag() && stack.getTag().contains("stored_state")) {
				return bridgeModel.getCachedModel(BridgeType.LASER, NbtUtils.readBlockState(NBTHelper.getTag(stack, "stored_state")));
			}

			return bridgeModel.getCachedModel(BridgeType.LASER, Blocks.AIR.defaultBlockState());
		}
	}
}
