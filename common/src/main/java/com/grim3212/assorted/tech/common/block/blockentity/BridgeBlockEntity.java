package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.lib.client.model.data.IBlockModelData;
import com.grim3212.assorted.lib.client.model.data.IModelDataBuilder;
import com.grim3212.assorted.lib.client.model.data.IModelDataKey;
import com.grim3212.assorted.lib.core.block.IBlockEntityWithModelData;
import com.grim3212.assorted.lib.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BridgeBlockEntity extends BlockEntity implements IBlockEntityWithModelData {

    public static final IModelDataKey<BlockState> BLOCK_STATE = IModelDataKey.create();

    protected BlockState blockState = Blocks.AIR.defaultBlockState();
    protected Direction facing = Direction.NORTH;
    private IBlockModelData modelData = IModelDataBuilder.create().build();

    public BridgeBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public BridgeBlockEntity(BlockPos pos, BlockState state) {
        super(TechBlockEntityTypes.BRIDGE.get(), pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("stored_state"));
        this.facing = Direction.from3DDataValue(nbt.getInt("facing"));
    }

    @Override
    protected void saveAdditional(CompoundTag cmp) {
        super.saveAdditional(cmp);
        if (Services.PLATFORM.getRegistry(Registries.BLOCK).contains(this.blockState.getBlock()))
            cmp.put("stored_state", NbtUtils.writeBlockState(this.blockState));
        else
            cmp.put("stored_state", NbtUtils.writeBlockState(Blocks.AIR.defaultBlockState()));
        cmp.putInt("facing", this.facing.get3DDataValue());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
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
            if (!level.isClientSide) {
                level.blockUpdated(worldPosition, getBlockState().getBlock());
            }
        }

        this.setChanged();
    }

    public void setStoredBlockState(String registryName) {
        this.setStoredBlockState(Services.PLATFORM.getRegistry(Registries.BLOCK).getValue(new ResourceLocation(registryName)).orElse(Blocks.AIR).defaultBlockState());
    }

    @Override
    public @NotNull IBlockModelData getBlockModelData() {
        return IModelDataBuilder.create().withInitial(BLOCK_STATE, blockState).build();
    }
}
