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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bridge takes its geometry from a fixed shape and its texture from whatever block state the bridge
 * block entity is holding, so one baked bridge owns a cache of models keyed by that stored state.
 * <p>
 * The base type is {@link IDataAwareBakedModel} - a {@link BlockStateModel} that additionally sees the
 * block entity's model data - because the vanilla
 * {@link BlockStateModel#collectParts(RandomSource, List)} receives no level, position or model data.
 * <p>
 * <b>This model only survives if it is reached from the blockstate side.</b> A model json loader can
 * only contribute geometry, so AssortedLib's specification delegators flatten whatever a
 * specification bakes, once, against empty model data - which for a bridge is the fallback texture
 * everywhere. The blockstate json therefore names {@code assortedlib:specification}, which bakes this
 * model whole and hands it to {@code ForgeBakedModelDelegate} / {@code FabricBakedModelDelegate}.
 * <p>
 * The {@code BridgeType} the 1.20.1 version read is gone from this class rather than lost: it only
 * ever chose between the {@code bridge} and {@code bridge_gravity} fallback textures, and a
 * {@link BlockStateModel} is baked per block state now, so {@code TechBlockstateProvider} emits one
 * loader model per fallback and dispatches {@code BridgeBlock.TYPE} between them. The fallback here is
 * "apply no overrides", which leaves the {@code stored} slot the model json declares in place. For the
 * item side see the TODO on {@link BridgeBakedModel}.
 * <p>
 * The {@link ModelBaker} is deliberately held past baking, as it was in 1.20.1: a stored block state
 * is only known while rendering and there is no bounded set of them to bake eagerly. It stays usable
 * because the bakery's resolved models and atlas preparations live as long as the baked models do.
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

    /**
     * Concurrent because it is filled during rendering, not during baking: the stored states are only
     * known once chunks are being built, and section compilation runs on several threads at once.
     */
    protected final Map<BlockState, BlockStateModel> cache = new ConcurrentHashMap<>();
    protected final Map<BlockState, Material.Baked> particleCache = new ConcurrentHashMap<>();
    protected volatile BlockStateModel EMPTY;

    public BlockStateModel getCachedModel(BlockState blockState) {
        if (isEmpty(blockState)) {
            BlockStateModel empty = EMPTY;
            if (empty == null) {
                // No overrides: an empty bridge is whatever the model json's own stored slot names,
                // which is what lets one loader model per BridgeType fallback texture stand in for the
                // type check this class used to make.
                EMPTY = empty = generateModel(ImmutableMap.of());
            }
            return empty;
        }

        return this.cache.computeIfAbsent(blockState, state -> generateModel(textures(storedTexture(state))));
    }

    // Resolved from the stored texture rather than read off the cached model, so it costs no bake for
    // a state that was only ever broken and never drawn.
    public Material.Baked getCachedParticle(BlockState blockState) {
        if (isEmpty(blockState)) {
            return this.particle;
        }

        return this.particleCache.computeIfAbsent(blockState, state -> this.bakery.materials().get(new Material(Identifier.parse(storedTexture(state))), this.debugName));
    }

    private static boolean isEmpty(BlockState blockState) {
        return blockState == null || blockState == Blocks.AIR.defaultBlockState();
    }

    // Grass, podzol and mycelium are special cased: their particle sprite is the side texture, not the
    // top one that reads as the block's colour.
    private static String storedTexture(BlockState state) {
        if (state.getBlock() == Blocks.GRASS_BLOCK) {
            return "minecraft:block/grass_block_top";
        } else if (state.getBlock() == Blocks.PODZOL) {
            return "minecraft:block/dirt_podzol_top";
        } else if (state.getBlock() == Blocks.MYCELIUM) {
            return "minecraft:block/mycelium_top";
        }

        // BlockModelShaper is gone; the particle sprite is answered by the ModelManager's baked models.
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite().contents().name().toString();
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
        collectCachedParts(this.getCachedModel(storedState(extraData)), random, output);
    }

    // Break and hit particles read the sprite, not the geometry; without this they show the model
    // json's own particle slot - the fallback bridge texture - whatever the block entity has stored.
    @Override
    public Material.Baked particleMaterial(@NotNull IBlockModelData extraData) {
        return this.getCachedParticle(storedState(extraData));
    }

    private static BlockState storedState(IBlockModelData extraData) {
        if (extraData.hasProperty(TechModelProperties.BLOCK_STATE)) {
            return extraData.getData(TechModelProperties.BLOCK_STATE);
        }

        return Blocks.AIR.defaultBlockState();
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
