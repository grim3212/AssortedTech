package com.grim3212.assorted.tech.common.block.blockentity;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentGetter;
import com.grim3212.assorted.lib.client.model.data.IBlockModelData;
import com.grim3212.assorted.lib.client.model.data.IModelDataBuilder;
import com.grim3212.assorted.lib.core.block.IBlockEntityWithModelData;
import com.grim3212.assorted.lib.platform.ClientServices;
import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.BridgeControlBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.properties.TechModelProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
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

    /**
     * A bridge placed from an item takes the block the item carries as {@code stored_state} in its
     * custom data. {@code BlockItem#place} hands the stack's components over here, and reading
     * {@code custom_data} marks it used, so it is not also kept on the block entity.
     */
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        CompoundTag data = components.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.contains("stored_state")) {
            this.setStoredBlockState(NbtUtils.readBlockState(BuiltInRegistries.BLOCK, data.getCompoundOrEmpty("stored_state")));
        }
    }

    /**
     * Asks the controller to close the gap when a segment is broken. Only here can the segment's
     * facing still be read, but it cannot restore itself mid-removal, so the controller refills on
     * its next tick. {@link BridgeControlBlockEntity} inherits this, hence the block check.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (this.level == null || this.level.isClientSide() || !(state.getBlock() instanceof BridgeBlock)) {
            return;
        }

        Direction towardsController = this.facing.getOpposite();
        int maxLength = TechCommonMod.COMMON_CONFIG.bridgeMaxLength.get();

        for (int i = 1; i <= maxLength; i++) {
            BlockPos check = pos.relative(towardsController, i);
            BlockState checkState = this.level.getBlockState(check);

            if (checkState.getBlock() instanceof BridgeControlBlock) {
                if (checkState.getValue(BridgeControlBlock.POWERED) && checkState.getValue(BridgeControlBlock.FACING) == this.facing
                        && this.level.getBlockEntity(check) instanceof BridgeControlBlockEntity controller) {
                    controller.requestGapFill();
                }

                return;
            }

            // Anything that is not another segment of the same run means this one was not projected
            // from a controller in that direction.
            if (!checkState.is(TechBlocks.BRIDGE.get())) {
                return;
            }
        }
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
            // Light dampening is baked into the block state, so a bridge carries its stored block's
            // in a property. Changing it is an ordinary block update, and vanilla does the rest:
            // relights, recomputes the sky column and sends the state to every client. Server only;
            // a client takes the state from that update. A bridge control inherits this and is not a
            // bridge, hence the block check.
            if (!level.isClientSide() && getBlockState().getBlock() instanceof BridgeBlock) {
                final BlockState lit = BridgeBlock.withStoredDampening(getBlockState(), blockState);
                if (lit != getBlockState()) {
                    level.setBlock(worldPosition, lit, Block.UPDATE_ALL);
                }
            }
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            // Emission is still read from the stored block per position, so the light has to be
            // asked to look again.
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
