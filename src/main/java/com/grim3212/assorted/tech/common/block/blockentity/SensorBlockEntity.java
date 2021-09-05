package com.grim3212.assorted.tech.common.block.blockentity;

import java.util.List;

import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.util.SensorType;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public class SensorBlockEntity extends BlockEntity {

	private static final int MAX_RANGE = 15;

	private boolean showRange = false;
	private int range = 1;

	public SensorBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public SensorBlockEntity(BlockPos pos, BlockState state) {
		super(TechBlockEntityTypes.SENSOR.get(), pos, state);
	}

	public void tick(SensorType type) {
		BlockPos pos = this.getBlockPos();
		BlockState state = this.level.getBlockState(pos);

		if (state.getBlock() instanceof SensorBlock) {
			Direction dir = state.getValue(BlockStateProperties.FACING);

			int maxLength = this.range + 1;

			boolean obstructed = false;
			int traverse = 1;
			while (traverse < maxLength && !obstructed) {
				BlockPos checkPos = pos.relative(dir, traverse);
				BlockState checkState = level.getBlockState(checkPos);
				if (Block.isFaceFull(checkState.getCollisionShape(level, checkPos), dir.getOpposite())) {
					obstructed = true;
				} else {
					traverse++;
				}
			}

			Vec3i sensorPos = dir.getNormal().multiply(obstructed ? traverse : maxLength);

			AABB aabb = state.getCollisionShape(level, pos).bounds().move(pos).expandTowards(sensorPos.getX(), sensorPos.getY(), sensorPos.getZ()).deflate(1D);

			List<? extends Entity> list = level.getEntities((Entity) null, aabb, type.getTrigger());

			if (list.isEmpty() && state.getValue(SensorBlock.DETECTED)) {
				level.setBlock(pos, state.setValue(SensorBlock.DETECTED, false), 3);
			} else if (!list.isEmpty() && !state.getValue(SensorBlock.DETECTED)) {
				level.setBlock(pos, state.setValue(SensorBlock.DETECTED, true), 3);
			}
		}
	}

	public boolean shouldShowRange() {
		return showRange;
	}

	public void toggleShowRange() {
		this.showRange = !this.showRange;
	}

	public int getRange() {
		return this.range;
	}

	public int cycleRange() {
		int newRange = this.range + 1;
		if (newRange > MAX_RANGE)
			return this.range = 1;
		else
			return this.range = newRange;
	}

	public int reverseCycleRange() {
		int newRange = this.range - 1;
		if (newRange < 1)
			return this.range = MAX_RANGE;
		else
			return this.range = newRange;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.readPacketNBT(nbt);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		super.save(compound);
		this.writePacketNBT(compound);
		return compound;
	}

	public void writePacketNBT(CompoundTag cmp) {
		cmp.putBoolean("ShowRange", showRange);
		cmp.putInt("Range", range);
	}

	public void readPacketNBT(CompoundTag cmp) {
		this.showRange = cmp.getBoolean("ShowRange");
		this.range = cmp.getInt("Range");
	}

	@Override
	public CompoundTag getUpdateTag() {
		return save(new CompoundTag());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag nbtTagCompound = new CompoundTag();
		writePacketNBT(nbtTagCompound);
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 1, nbtTagCompound);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		this.readPacketNBT(pkt.getTag());
		if (level instanceof ClientLevel) {
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
		}
	}
}
