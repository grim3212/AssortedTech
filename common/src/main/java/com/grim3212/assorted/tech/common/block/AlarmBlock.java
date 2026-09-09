package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.network.AlarmOpenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AlarmBlock extends Block implements EntityBlock {

    private static final VoxelShape UP_BOUNDS = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 4.0D, 13.0D);
    private static final VoxelShape DOWN_BOUNDS = Block.box(3.0D, 12.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    private static final VoxelShape NORTH_BOUNDS = Block.box(3.0D, 3.0D, 12.0D, 13.0D, 13.0D, 16.0D);
    private static final VoxelShape SOUTH_BOUNDS = Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 4.0D);
    private static final VoxelShape WEST_BOUNDS = Block.box(12.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D);
    private static final VoxelShape EAST_BOUNDS = Block.box(0.0D, 3.0D, 3.0D, 4.0D, 13.0D, 13.0D);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    // DirectionProperty was folded back into a plain EnumProperty<Direction> in 26.x.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public AlarmBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING, WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlarmBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case EAST:
                return EAST_BOUNDS;
            case NORTH:
                return NORTH_BOUNDS;
            case SOUTH:
                return SOUTH_BOUNDS;
            case UP:
                return UP_BOUNDS;
            case WEST:
                return WEST_BOUNDS;
            case DOWN:
            default:
                return DOWN_BOUNDS;
        }
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return Shapes.empty();
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

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AlarmBlockEntity alarm) {
                level.playSound(null, pos, AlarmBlockEntity.getSound(alarm.getAlarmType()).get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            level.setBlock(pos, state.setValue(POWERED, true), 3);
        } else if (state.getValue(POWERED) && !level.hasNeighborSignal(poweredPos)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    /**
     * {@code use} split into {@code useItemOn} / {@code useWithoutItem}; opening the alarm screen
     * does not care what is in hand, so this is the hand-agnostic half and a held item falls
     * through to it.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (!worldIn.isClientSide()) {
            Services.NETWORK.sendTo(player, new AlarmOpenPacket(pos));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }
}
