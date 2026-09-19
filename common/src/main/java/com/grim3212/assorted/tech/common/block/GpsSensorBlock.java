package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A sensor that watches a position a GPS stored rather than the space in front of it. Right click
 * it to put the GPS in its slot and set what it looks for.
 */
public class GpsSensorBlock extends Block implements EntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final boolean upgraded;

    public GpsSensorBlock(Properties props, boolean upgraded) {
        super(props.pushReaction(PushReaction.BLOCK));
        this.upgraded = upgraded;
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    public boolean isUpgraded() {
        return this.upgraded;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof GpsSensorBlockEntity sensor) {
            Services.PLATFORM.openMenu(serverPlayer, sensor);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof GpsSensorBlockEntity sensor ? sensor.getSignal() : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GpsSensorBlockEntity(pos, state);
    }

    // Server only, like SensorBlock: the tick switches ACTIVE and the server sends the result.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (level1, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof GpsSensorBlockEntity sensor) {
                sensor.tick();
            }
        };
    }
}
