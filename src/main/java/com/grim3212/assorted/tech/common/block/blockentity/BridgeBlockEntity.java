package com.grim3212.assorted.tech.common.block.blockentity;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

public class BridgeBlockEntity extends BlockEntity {

	public static final ModelProperty<BlockState> BLOCK_STATE = new ModelProperty<BlockState>();
	protected BlockState blockState = Blocks.AIR.defaultBlockState();
	protected Direction facing = Direction.NORTH;

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
		if (ForgeRegistries.BLOCKS.getKey(this.blockState.getBlock()) != null)
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

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		requestModelDataUpdate();
		if (level instanceof ClientLevel) {
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
		}
	}

	@Override
	public ModelData getModelData() {
		return ModelData.builder().with(BLOCK_STATE, blockState).build();
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
		this.setStoredBlockState(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName)).defaultBlockState());
	}
}
