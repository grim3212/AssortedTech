package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.common.block.blockentity.SensorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class SensorBlock extends Block implements EntityBlock {

    public static final BooleanProperty DETECTED = BooleanProperty.create("detected");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

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

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.sensor.detects." + this.sensorType.name().toLowerCase()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction dir) {
        return state.getValue(DETECTED) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SensorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack inHand = player.getItemInHand(hand);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof SensorBlockEntity sensor) {
            if (inHand.getItem() == Items.REDSTONE_TORCH) {
                sensor.toggleShowRange();
                return InteractionResult.SUCCESS;
            } else {
                if (player.isShiftKeyDown()) {
                    int newRange = sensor.reverseCycleRange();
                    player.displayClientMessage(Component.translatable("message.sensor.range", newRange), true);
                } else {
                    int newRange = sensor.cycleRange();
                    player.displayClientMessage(Component.translatable("message.sensor.range", newRange), true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (level1, blockPos, blockState, t) -> {
            if (t instanceof SensorBlockEntity sensor) {
                sensor.tick(this.sensorType);
            }
        };
    }
}
