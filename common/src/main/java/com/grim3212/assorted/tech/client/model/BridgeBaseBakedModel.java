package com.grim3212.assorted.tech.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.grim3212.assorted.lib.client.model.ItemOverridesExtension;
import com.grim3212.assorted.lib.client.model.baked.IDataAwareBakedModel;
import com.grim3212.assorted.lib.client.model.baked.IDelegatingBakedModel;
import com.grim3212.assorted.lib.client.model.data.IBlockModelData;
import com.grim3212.assorted.lib.client.model.loaders.context.IModelBakingContext;
import com.grim3212.assorted.lib.platform.ClientServices;
import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.properties.TechModelProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BridgeBaseBakedModel implements IDataAwareBakedModel {
    protected final BlockModel unbakedBridge;
    protected final ModelBaker bakery;
    protected final Function<Material, TextureAtlasSprite> spriteGetter;
    protected final ModelState transform;
    protected final ResourceLocation name;
    protected final IModelBakingContext context;
    protected final BridgeItemOverrideList itemOverrideList;

    public BridgeBaseBakedModel(IModelBakingContext context, BlockModel unbakedBridge, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation name) {
        this.context = context;
        this.unbakedBridge = unbakedBridge;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.transform = transform;
        this.name = name;
        this.itemOverrideList = new BridgeItemOverrideList(context);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.context.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.context.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.context.useBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    protected final Map<BlockState, BakedModel> cache = new HashMap<>();
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
                texture = blockTexture.contents().name().toString();
            }

            newTexture.put("particle", texture);
            newTexture.put("#stored", texture);
            this.cache.put(blockState, generateModel(newTexture.build()));
        }

        return this.cache.get(blockState);
    }

    protected abstract BakedModel generateModel(ImmutableMap<String, String> texture);

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.spriteGetter.apply(this.context.getMaterial("particle").orElse(null));
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.context.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.itemOverrideList;
    }

    @Override
    public @Nonnull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull IBlockModelData extraData, @Nullable RenderType renderType) {
        BlockState blockState = Blocks.AIR.defaultBlockState();
        if (extraData.hasProperty(TechModelProperties.BLOCK_STATE)) {
            blockState = extraData.getData(TechModelProperties.BLOCK_STATE);
        }

        BridgeType type = state.getValue(BridgeBlock.TYPE);

        var cached = this.getCachedModel(type, blockState);
        return cached.getQuads(state, side, rand);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand) {
        return getQuads(state, side, rand, IBlockModelData.empty(), RenderType.cutout());
    }

    @Override
    public @Nonnull List<BakedQuad> getQuads(ItemStack stack, boolean fabulous, @Nonnull RandomSource rand, @Nullable RenderType renderType) {
        return getQuads(null, null, rand, IBlockModelData.empty(), RenderType.translucent());
    }

    @Override
    public @Nonnull Collection<RenderType> getSupportedRenderTypes(BlockState state, RandomSource rand, IBlockModelData data) {
        BlockState blockState = Blocks.AIR.defaultBlockState();
        if (data.hasProperty(TechModelProperties.BLOCK_STATE)) {
            blockState = data.getData(TechModelProperties.BLOCK_STATE);
        }

        if (blockState.isAir()) {
            return ImmutableList.of(RenderType.cutout());
        }

        BridgeType type = state.getValue(BridgeBlock.TYPE);
        var cached = this.getCachedModel(type, blockState);
        Collection<RenderType> renderTypes = ClientServices.MODELS.getRenderTypesFor(cached, blockState, rand, data);
        return renderTypes.isEmpty() ? ImmutableList.of(RenderType.cutout()) : renderTypes;
    }

    @Override
    public @Nonnull Collection<RenderType> getSupportedRenderTypes(ItemStack stack, boolean fabulous) {
        return ImmutableList.of(RenderType.translucent());
    }

    public static final class BridgeItemOverrideList extends ItemOverridesExtension {

        protected BridgeItemOverrideList(IModelBakingContext context) {
            super(context);
        }

        @Override
        public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int field) {
            BakedModel returnModel = originalModel;
            if (originalModel instanceof IDelegatingBakedModel delegate) {
                returnModel = delegate.getDelegate();
            }

            if (returnModel instanceof BridgeBaseBakedModel bridgeModel) {
                if (stack.hasTag() && stack.getTag().contains("stored_state")) {
                    return bridgeModel.getCachedModel(BridgeType.LASER, NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), NBTHelper.getTag(stack, "stored_state")));
                }

                return bridgeModel.getCachedModel(BridgeType.LASER, Blocks.AIR.defaultBlockState());
            }

            return returnModel;
        }
    }
}
