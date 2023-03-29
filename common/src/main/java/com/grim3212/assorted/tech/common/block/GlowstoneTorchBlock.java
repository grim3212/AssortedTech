package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.lib.core.block.IBlockExtraProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class GlowstoneTorchBlock extends TorchBlock implements IBlockExtraProperties {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public GlowstoneTorchBlock(Properties props) {
        super(props, DustParticleOptions.REDSTONE);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean flag) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state2, boolean flag) {
        if (!flag) {
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }

        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean flag) {
        level.scheduleTick(pos, this, 2);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, LevelReader levelReader, BlockPos pos, Direction side) {
        return state.isRedstoneConductor(levelReader, pos);
    }

    @Override
    public float getFriction(BlockState state, LevelReader levelReader, BlockPos pos, @Nullable Entity entity) {
        return super.getFriction();
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter blockGetter, BlockPos pos, Player player) {
        return player.hasCorrectToolForDrops(state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter blockGetter, BlockPos pos, Player player) {
        return super.getCloneItemStack(blockGetter, pos, state);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader levelReader, BlockPos pos, @Nullable Entity entity) {
        return super.getSoundType(state);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter blockGetter, BlockPos position, Explosion explosion) {
        return super.getExplosionResistance();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
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
            level.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }
}
