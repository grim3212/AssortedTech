package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.api.util.GravityType;
import com.grim3212.assorted.tech.common.block.blockentity.GravityDirectionalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GravityDirectionalBlock extends Block implements EntityBlock {

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    // DirectionProperty was folded back into a plain EnumProperty<Direction> in 26.x.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    private final GravityType type;

    public GravityDirectionalBlock(GravityType type, Properties props) {
        super(props.pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH));
        this.type = type;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravityDirectionalBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            boolean flag = state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    level.setBlock(pos, state.cycle(POWERED), 2);
                }
            }

        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())).setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    public GravityType getType() {
        return type;
    }

    /**
     * See {@link GravityBlock} - {@code use} split into the held-item half and the hand-agnostic
     * half, and the redstone torch branch is the only one that needs the stack.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack inHand, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (inHand.getItem() == Items.REDSTONE_TORCH && level.getBlockEntity(pos) instanceof GravityDirectionalBlockEntity gravity) {
            gravity.toggleShowRange();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof GravityDirectionalBlockEntity gravity) {
            int newRange = player.isShiftKeyDown() ? gravity.reverseCycleRange() : gravity.cycleRange();
            // Player#displayClientMessage is gone; the action-bar overlay it used lives on
            // ServerPlayer#sendSystemMessage(Component, boolean) now, and this only ever runs server side.
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.sensor.range", newRange), true);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof GravityDirectionalBlockEntity gravity) {
                gravity.tick();
            }
        };
    }
}
