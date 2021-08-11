package com.grim3212.assorted.tech.common.block.blockentity;

import java.util.List;

import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.util.SensorType;
import com.grim3212.assorted.tech.common.util.TechUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public class SensorBlockEntity extends TileEntity implements ITickableTileEntity {

	private static final int MAX_RANGE = 15;

	private boolean showRange = false;
	private int range = 1;
	private SensorType sensorType;

	public SensorBlockEntity() {
		super(TechBlockEntityTypes.SENSOR.get());
		this.sensorType = SensorType.WOOD;
	}

	public SensorBlockEntity(final SensorType sensorType) {
		super(TechBlockEntityTypes.SENSOR.get());
		this.sensorType = sensorType;
	}

	@Override
	public void tick() {
		BlockPos pos = this.getBlockPos();
		BlockState state = this.level.getBlockState(pos);

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

		Vector3i sensorPos = TechUtil.multiply(dir.getNormal(), obstructed ? traverse : maxLength);
		AxisAlignedBB aabb = state.getCollisionShape(level, pos).bounds().move(pos).expandTowards(sensorPos.getX(), sensorPos.getY(), sensorPos.getZ()).deflate(1D);

		List<? extends Entity> list = level.getEntities((Entity) null, aabb, this.sensorType.getTrigger());

		if (list.isEmpty() && state.getValue(SensorBlock.DETECTED)) {
			level.setBlock(pos, state.setValue(SensorBlock.DETECTED, false), 3);
		} else if (!list.isEmpty() && !state.getValue(SensorBlock.DETECTED)) {
			level.setBlock(pos, state.setValue(SensorBlock.DETECTED, true), 3);
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
	public void load(BlockState state, CompoundNBT nbt) {
		super.load(state, nbt);
		this.readPacketNBT(nbt);
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		super.save(compound);
		this.writePacketNBT(compound);
		return compound;
	}

	public void writePacketNBT(CompoundNBT cmp) {
		cmp.putBoolean("ShowRange", showRange);
		cmp.putInt("Range", range);
		cmp.putInt("SensorType", sensorType.ordinal());
	}

	public void readPacketNBT(CompoundNBT cmp) {
		this.showRange = cmp.getBoolean("ShowRange");
		this.range = cmp.getInt("Range");
		this.sensorType = SensorType.values()[cmp.getInt("SensorType")];
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return save(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbtTagCompound = new CompoundNBT();
		writePacketNBT(nbtTagCompound);
		return new SUpdateTileEntityPacket(this.worldPosition, 1, nbtTagCompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		this.readPacketNBT(pkt.getTag());
		if (level instanceof ClientWorld) {
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
		}
	}

}
