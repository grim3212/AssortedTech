package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.lib.core.block.IBlockLightEmission;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 * See {@link FlipFlopTorchBlock} for why this extends {@link BaseTorchBlock} rather than
 * {@code TorchBlock}.
 */
public class GlowstoneTorchBlock extends BaseTorchBlock implements IBlockLightEmission {

    public static final MapCodec<GlowstoneTorchBlock> CODEC = simpleCodec(GlowstoneTorchBlock::new);

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public GlowstoneTorchBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseTorchBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean movedByPiston) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!movedByPiston) {
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        level.scheduleTick(pos, this, 2);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!state.getValue(LIT) && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(LIT, true), 3);
        } else if (state.getValue(LIT) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(LIT, false), 3);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(LIT)) {
            double d0 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
            level.addParticle(DustParticleOptions.REDSTONE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}
