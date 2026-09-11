package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.common.block.blockentity.SensorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.BlockHitResult;

public class SensorBlock extends Block implements EntityBlock {

    public static final BooleanProperty DETECTED = BooleanProperty.create("detected");
    // DirectionProperty was folded back into a plain EnumProperty<Direction> in 26.x.
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private final SensorType sensorType;

    public SensorBlock(Properties props, SensorType sensorType) {
        super(props.pushReaction(PushReaction.BLOCK));
        this.registerDefaultState(this.stateDefinition.any().setValue(DETECTED, false).setValue(FACING, Direction.NORTH));
        this.sensorType = sensorType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(DETECTED, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(DETECTED, false);
    }

    // Block.appendHoverText no longer exists - tooltips are an item concern now - so the
    // "detects X" line moved onto the block item; see TechBlocks.TooltipBlockItem.

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction dir) {
        return state.getValue(DETECTED) ? 15 : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SensorBlockEntity(pos, state);
    }

    /**
     * See {@link GravityBlock} - {@code use} split into the held-item half and the hand-agnostic
     * half, and the redstone torch branch is the only one that needs the stack.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack inHand, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (inHand.getItem() == Items.REDSTONE_TORCH && level.getBlockEntity(pos) instanceof SensorBlockEntity sensor) {
            sensor.toggleShowRange();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SensorBlockEntity sensor) {
            int newRange = player.isShiftKeyDown() ? sensor.reverseCycleRange() : sensor.cycleRange();
            // Player#displayClientMessage is gone; the action-bar overlay it used lives on
            // ServerPlayer#sendSystemMessage(Component, boolean) now, and this only ever runs server side.
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.sensor.range", newRange), true);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    // Server only, like the vanilla hopper: the tick switches the DETECTED state of the block, and
    // the server sends clients the result.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof SensorBlockEntity sensor) {
                sensor.tick(this.sensorType);
            }
        };
    }
}
