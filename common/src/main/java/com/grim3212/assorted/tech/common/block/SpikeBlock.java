package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.api.util.TechDamageTypes;
import com.grim3212.assorted.tech.common.sounds.TechSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SpikeBlock extends Block implements SimpleWaterloggedBlock {

    private static final VoxelShape UP_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    private static final VoxelShape DOWN_SHAPE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    // DirectionProperty was folded back into a plain EnumProperty<Direction> in 26.x.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private final SpikeType spikeType;

    public SpikeBlock(Properties props, SpikeType spikeType) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
        this.spikeType = spikeType;
    }

    public SpikeType getSpikeType() {
        return spikeType;
    }

    // Block.appendHoverText no longer exists - tooltips are an item concern now - so the
    // "damage X" line moved onto the block item; see TechBlocks.TooltipBlockItem.

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
        if (!state.getValue(POWERED)) {
            switch (state.getValue(FACING)) {
                case DOWN:
                    return DOWN_SHAPE;
                case EAST:
                    return EAST_SHAPE;
                case NORTH:
                    return NORTH_SHAPE;
                case SOUTH:
                    return SOUTH_SHAPE;
                case UP:
                    return UP_SHAPE;
                case WEST:
                    return WEST_SHAPE;
            }
        }

        return Shapes.block();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = this.defaultBlockState();
        LevelReader iworldreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction[] adirection = context.getNearestLookingDirections();
        FluidState fluidstate = context.getLevel().getFluidState(blockpos);

        for (Direction direction : adirection) {
            Direction direction1 = direction.getOpposite();
            blockstate = blockstate.setValue(FACING, direction1);
            if (blockstate.canSurvive(iworldreader, blockpos)) {
                return blockstate.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
            }
        }

        return null;
    }

    /**
     * {@code updateShape} takes the neighbour's position and state explicitly now, and gets a
     * {@link ScheduledTickAccess} instead of the {@code LevelAccessor} it used to schedule through.
     */
    @Override
    protected BlockState updateShape(BlockState stateIn, LevelReader level, ScheduledTickAccess ticks, BlockPos currentPos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (stateIn.getValue(WATERLOGGED)) {
            ticks.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return directionToNeighbour.getOpposite() == stateIn.getValue(FACING) && !stateIn.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return blockstate.isFaceSturdy(worldIn, blockpos, direction);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean movedByPiston) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }

    /**
     * Replaces {@code onRemove}; 26.x only calls this for a real removal.
     */
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
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        Direction dir = state.getValue(FACING);
        BlockPos poweredPos = pos.offset(dir.getOpposite().getUnitVec3i());
        if (!state.getValue(POWERED) && level.hasNeighborSignal(poweredPos)) {
            level.playSound(null, pos, TechSounds.SPIKE_DEPLOY.get(), SoundSource.BLOCKS, 0.3F, 0.6F);
            level.setBlock(pos, state.setValue(POWERED, true), 3);
        } else if (state.getValue(POWERED) && !level.hasNeighborSignal(poweredPos)) {
            level.playSound(null, pos, TechSounds.SPIKE_CLOSE.get(), SoundSource.BLOCKS, 0.3F, 0.6F);
            level.setBlock(pos, state.setValue(POWERED, false), 3);
        }
    }

    /**
     * Hurts a living entity by this spike type's damage while powered. The effect applier and
     * {@code isPrecise} are ignored, as vanilla's own hazard blocks do.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (state.getValue(POWERED) && entity instanceof LivingEntity && level instanceof ServerLevel serverLevel) {
            DamageSource spike = TechDamageTypes.source(serverLevel, TechDamageTypes.SPIKE);
            entity.hurtServer(serverLevel, spike, this.spikeType.getDamage());
        }
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
}
