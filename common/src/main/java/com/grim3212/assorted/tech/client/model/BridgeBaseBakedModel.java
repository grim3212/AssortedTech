package com.grim3212.assorted.tech.client.model;

import com.google.common.collect.ImmutableMap;
import com.grim3212.assorted.lib.client.model.baked.IDataAwareBakedModel;
import com.grim3212.assorted.lib.client.model.data.IBlockModelData;
import com.grim3212.assorted.lib.client.model.loaders.context.IModelBakingContext;
import com.grim3212.assorted.tech.common.properties.TechModelProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bridge takes its geometry from a fixed shape and its texture from whatever block state the bridge
 * block entity is holding, so one baked bridge owns a cache of models keyed by that stored state.
 * <p>
 * The base type is {@link IDataAwareBakedModel} - a {@link BlockStateModel} that additionally sees the
 * block entity's model data - because {@code BakedModel} is gone and the vanilla replacement,
 * {@link BlockStateModel#collectParts(RandomSource, List)}, receives no level, position or model data.
 * Everything the 1.20.1 version answered about how to <em>draw</em> the model went with it: ambient
 * occlusion, gui light and the particle sprite are properties of the individual
 * {@linkplain BlockStateModelPart parts} now, item transforms belong to the item pipeline, and a model
 * no longer picks a {@code RenderType} at all - the chunk layer is derived per quad from
 * {@link BakedQuad.MaterialInfo#layer()}.
 * <p>
 * Two inputs the 1.20.1 version read are gone from this class rather than lost:
 * <ul>
 * <li>the {@code BridgeType} of the block state, which only ever chose between the {@code bridge} and
 * {@code bridge_gravity} fallback textures. A {@link BlockStateModel} is baked per block state now, so
 * that choice belongs in the blockstate json: {@code TechBlockstateProvider} emits one loader model per
 * fallback texture and dispatches {@code BridgeBlock.TYPE} between them, and the fallback here is
 * simply "apply no overrides", which leaves the {@code stored} slot the model json declares in
 * place.</li>
 * <li>the item override list - see the TODO on {@link BridgeBakedModel}.</li>
 * </ul>
 * <p>
 * TODO(26.2): reached through a model json loader, this class collapses to its unseeded output, so a
 *  placed bridge draws its fallback texture and never the block it has absorbed.
 *  What it used to do: a custom model loader returned a whole {@code BakedModel}, so the bridge could
 *  pick a different set of quads per draw from the block entity's model data.
 *  Why it is at risk: {@code UnbakedGeometry#bake} has to return a {@code QuadCollection}, so
 *  AssortedLib's {@code ForgeModelGeometryToSpecificationPlatformDelegator} (and the Fabric
 *  equivalent) flatten whatever a specification bakes into a single quad collection at bake time, with
 *  empty model data. The per position behaviour only survives if the baked model reaches the
 *  blockstate layer intact, which in 26.2 means a {@code CustomUnbakedBlockStateModel} registered from
 *  the blockstate json (NeoForge's {@code RegisterBlockStateModels} / Fabric's own registry) rather
 *  than a model json loader. AssortedLib's {@code ForgeBakedModelDelegate} /
 *  {@code FabricBakedModelDelegate} already route
 *  {@link #collectParts(RandomSource, IBlockModelData, List)} correctly once the model gets there, so
 *  what is missing is the blockstate side entry point, in the library and in this mod's generated
 *  blockstate json - not this class. This is the identical gap AssortedDecor's
 *  {@code ColorizerBaseBakedModel} carries.
 *  <p>
 *  Note also that the {@link ModelBaker} is held past baking, as it was in 1.20.1, because a stored
 *  block state is only known at render time and there is no bounded set of them to bake eagerly. On
 *  the flattening path above that never matters - the model is collected from immediately after it is
 *  baked - but a blockstate side wrapper would bake children long after the model manager has moved
 *  on, and that is the thing to check first if the bridges misbehave once one exists.
 */
public abstract class BridgeBaseBakedModel implements IDataAwareBakedModel {

    protected final BridgeUnbakedModel.Bridge model;
    protected final ModelBaker bakery;
    protected final ModelState transform;
    protected final Identifier name;
    protected final ModelDebugName debugName;
    protected final IModelBakingContext context;

    private final Material.Baked particle;

    public BridgeBaseBakedModel(IModelBakingContext context, BridgeUnbakedModel.Bridge model, ModelBaker bakery, ModelState transform, Identifier name) {
        this.context = context;
        this.model = model;
        this.bakery = bakery;
        this.transform = transform;
        this.name = name;
        this.debugName = name::toString;

        Material particleMaterial = context.getMaterial("particle").orElse(null);
        this.particle = particleMaterial != null ? bakery.materials().get(particleMaterial, this.debugName) : bakery.materials().reportMissingReference("particle", this.debugName);
    }

    protected final Map<BlockState, BlockStateModel> cache = new HashMap<>();
    protected BlockStateModel EMPTY;

    public BlockStateModel getCachedModel(BlockState blockState) {
        if (blockState == null || blockState == Blocks.AIR.defaultBlockState()) {
            if (EMPTY == null) {
                // No overrides: an empty bridge is whatever the model json's own stored slot names,
                // which is what lets one loader model per BridgeType fallback texture stand in for the
                // type check this class used to make.
                EMPTY = generateModel(ImmutableMap.of());
            }
            return EMPTY;
        }

        if (!this.cache.containsKey(blockState)) {
            String texture;
            if (blockState.getBlock() == Blocks.GRASS_BLOCK) {
                texture = "minecraft:block/grass_block_top";
            } else if (blockState.getBlock() == Blocks.PODZOL) {
                texture = "minecraft:block/dirt_podzol_top";
            } else if (blockState.getBlock() == Blocks.MYCELIUM) {
                texture = "minecraft:block/mycelium_top";
            } else {
                // BlockModelShaper is gone; the particle sprite of a block state is answered by the
                // baked block state models the ModelManager holds.
                texture = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(blockState).sprite().contents().name().toString();
            }

            this.cache.put(blockState, generateModel(textures(texture)));
        }

        return this.cache.get(blockState);
    }

    /**
     * The texture slot overrides for a stored block texture. The slot is named {@code stored}, not
     * {@code #stored} as it was in 1.20.1: a leading {@code #} marks a <em>reference</em> to another
     * slot and {@link net.minecraft.client.resources.model.sprite.TextureSlots#getMaterial} strips it
     * before looking a slot up, so a slot declared as {@code #stored} could never be found.
     */
    private static ImmutableMap<String, String> textures(String texture) {
        return ImmutableMap.of("particle", texture, "stored", texture);
    }

    protected abstract BlockStateModel generateModel(ImmutableMap<String, String> textures);

    @Override
    public void collectParts(@NotNull RandomSource random, @NotNull IBlockModelData extraData, @NotNull List<BlockStateModelPart> output) {
        BlockState blockState = Blocks.AIR.defaultBlockState();
        if (extraData.hasProperty(TechModelProperties.BLOCK_STATE)) {
            blockState = extraData.getData(TechModelProperties.BLOCK_STATE);
        }

        collectCachedParts(this.getCachedModel(blockState), random, output);
    }

    // Deprecated by NeoForge in favour of a level/pos aware overload that only exists in its patched
    // jar; the cached models are plain vanilla BlockStateModels, so this is the only way to reach
    // their parts.
    @SuppressWarnings("deprecation")
    private static void collectCachedParts(BlockStateModel model, RandomSource random, List<BlockStateModelPart> output) {
        model.collectParts(random, output);
    }

    // Deprecated by NeoForge in favour of level/pos aware overloads that only exist in its patched
    // jar; vanilla still declares these abstract, so they have to be implemented here.
    @SuppressWarnings("deprecation")
    @Override
    public Material.Baked particleMaterial() {
        return this.particle;
    }

    /**
     * The union of the flags of every model baked so far, plus the empty one. A bridge bakes its
     * children lazily as stored states are encountered, so there is no complete set to report; the
     * flags of a state that has not been seen yet cannot be known without baking it.
     */
    @SuppressWarnings("deprecation")
    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        int flags = this.getCachedModel(Blocks.AIR.defaultBlockState()).materialFlags();
        for (BlockStateModel cached : this.cache.values()) {
            flags |= cached.materialFlags();
        }

        return flags;
    }
}
