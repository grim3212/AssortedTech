package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.lib.client.model.data.IBlockModelData;
import com.grim3212.assorted.lib.client.model.data.IModelDataBuilder;
import com.grim3212.assorted.lib.core.block.IBlockEntityWithModelData;
import com.grim3212.assorted.lib.platform.ClientServices;
import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.common.properties.TechModelProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class BridgeBlockEntity extends BlockEntity implements IBlockEntityWithModelData {

    protected BlockState blockState = Blocks.AIR.defaultBlockState();
    protected Direction facing = Direction.NORTH;

    public BridgeBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public BridgeBlockEntity(BlockPos pos, BlockState state) {
        super(TechBlockEntityTypes.BRIDGE.get(), pos, state);
    }

    /**
     * Serialization moved onto {@link ValueInput} / {@link ValueOutput}. The stored state is written
     * through {@code BlockState.CODEC}, which produces the same {@code Name}/{@code Properties}
     * shape {@code NbtUtils.writeBlockState} did, so the on-disk format is unchanged.
     */
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.blockState = input.read("stored_state", BlockState.CODEC).orElse(Blocks.AIR.defaultBlockState());
        this.facing = Direction.from3DDataValue(input.getIntOr("facing", Direction.NORTH.get3DDataValue()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (Services.PLATFORM.getRegistry(Registries.BLOCK).contains(this.blockState.getBlock()))
            output.store("stored_state", BlockState.CODEC, this.blockState);
        else
            output.store("stored_state", BlockState.CODEC, Blocks.AIR.defaultBlockState());
        output.putInt("facing", this.facing.get3DDataValue());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    public BlockState getStoredBlockState() {
        return blockState;
    }

    public void setStoredBlockState(BlockState blockState) {
        this.blockState = blockState;

        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            level.getLightEngine().checkBlock(getBlockPos());
            if (!level.isClientSide()) {
                // Level#blockUpdated is gone; it was only ever a call through to updateNeighborsAt.
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            } else {
                ClientServices.MODELS.requestModelDataRefresh(this);
            }
        }

        this.setChanged();
    }

    public void setStoredBlockState(String registryName) {
        this.setStoredBlockState(Services.PLATFORM.getRegistry(Registries.BLOCK).getValue(Identifier.parse(registryName)).orElse(Blocks.AIR).defaultBlockState());
    }

    @Override
    public @NotNull IBlockModelData getBlockModelData() {
        return IModelDataBuilder.create().withInitial(TechModelProperties.BLOCK_STATE, blockState).build();
    }
}
