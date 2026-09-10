package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.BridgeControlBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BridgeControlBlockEntity extends BridgeBlockEntity {

    private boolean removed = false;
    private int length = 0;
    private int checkTimer = 0;
    private boolean fillGapsRequested = false;

    public BridgeControlBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public BridgeControlBlockEntity(BlockPos pos, BlockState state) {
        super(TechBlockEntityTypes.BRIDGE_CONTROL.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.length = input.getIntOr("length", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("length", this.length);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick() {
        BlockPos pos = this.getBlockPos();
        BlockState state = this.level.getBlockState(pos);

        if (state.getBlock() instanceof BridgeControlBlock controlBridge) {
            if (state.getValue(BridgeControlBlock.POWERED)) {
                createBridge(state, controlBridge.getType());
                removed = false;
            } else {
                if (!removed) {
                    deleteBridge(state, controlBridge.getType());
                }
            }
        }
    }

    public void createBridge(BlockState state, BridgeType bridgeType) {
        if (length < TechCommonMod.COMMON_CONFIG.bridgeMaxLength.get()) {
            tryBuild(state, bridgeType);
        }

        // Don't try and fill gaps every time
        // Shouldn't be gaps except in creative
        if (fillGapsRequested || checkTimer == 1000) {
            checkTimer = 0;
            fillGapsRequested = false;
            fillGaps(state, bridgeType);
        } else {
            checkTimer++;
        }
    }

    /**
     * Asks for the sweep that repairs holes to run on the next tick instead of waiting out the timer.
     * Called by a segment that is being broken, so the gap closes immediately rather than up to 1000
     * ticks later.
     */
    public void requestGapFill() {
        this.fillGapsRequested = true;
    }

    private void fillGaps(BlockState state, BridgeType bridgeType) {
        BlockPos pos = this.getBlockPos();

        for (int i = 1; i <= length; i++) {
            BlockPos newPos = pos.relative(state.getValue(BridgeControlBlock.FACING), i);
            BlockState newPosState = level.getBlockState(newPos);

            BlockState matchingBridgeState = TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, bridgeType);

            if (newPosState == matchingBridgeState) {
                continue;
            }

            if (newPosState.isAir()) {
                level.setBlock(newPos, matchingBridgeState, 3);

                BlockEntity te = level.getBlockEntity(newPos);
                if (te instanceof BridgeBlockEntity bridge) {
                    bridge.setStoredBlockState(this.getStoredBlockState());
                    bridge.setFacing(state.getValue(BridgeControlBlock.FACING));
                }
            } else {
                length = i;
                break;
            }
        }
    }

    private void tryBuild(BlockState state, BridgeType bridgeType) {
        BlockPos pos = this.getBlockPos();
        Direction facing = state.getValue(BridgeControlBlock.FACING);
        BlockPos newPos = pos.relative(facing, length + 1);
        BlockState newPosState = level.getBlockState(newPos);

        BlockState matchingBridgeState = TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, bridgeType);

        if (BridgeBlock.canLaserBreak(level, newPos) || newPosState == matchingBridgeState) {

            if (newPosState != matchingBridgeState) {
                level.setBlockAndUpdate(newPos, matchingBridgeState);

                BlockEntity be = level.getBlockEntity(newPos);

                if (be instanceof BridgeBlockEntity bridge) {
                    bridge.setStoredBlockState(this.getStoredBlockState());
                    bridge.setFacing(facing);
                }

            }

            // Offset value for next try
            length++;
        }
    }

    public void deleteBridge(BlockState state, BridgeType bridgeType) {
        if (length == 0) {
            removed = true;
            return;
        }

        BlockPos pos = this.getBlockPos();
        // Add a little extra range in case of toggle on and off fast
        for (int i = 1; i <= length + 2; i++) {
            BlockPos newPos = pos.relative(state.getValue(BlockStateProperties.FACING), i);
            BlockState newStatePos = level.getBlockState(newPos);
            BlockState testState = TechBlocks.BRIDGE.get().defaultBlockState().setValue(BridgeBlock.TYPE, bridgeType);

            if (newStatePos == testState || newStatePos.isAir()) {
                level.removeBlock(newPos, false);
            }
        }

        length = 0;
    }
}
