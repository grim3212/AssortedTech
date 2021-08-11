package com.grim3212.assorted.tech.common.block.blockentity;

import java.util.List;

import com.grim3212.assorted.tech.client.particle.AirParticleData;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.util.FanMode;
import com.grim3212.assorted.tech.common.util.TechUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class FanBlockEntity extends TileEntity implements ITickableTileEntity {

	private int range = 1;
	private FanMode mode = FanMode.BLOW;
	private FanMode oldMode = FanMode.BLOW;

	public FanBlockEntity() {
		super(TechBlockEntityTypes.FAN.get());
	}

	public void tick() {
		BlockPos pos = this.getBlockPos();
		BlockState state = this.level.getBlockState(pos);

		if (mode != FanMode.OFF) {
			Direction dir = state.getValue(FanBlock.FACING);

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

			int distance = obstructed ? traverse : maxLength;
			Vector3i fanPos = TechUtil.multiply(dir.getNormal(), distance);
			AxisAlignedBB aabb = state.getCollisionShape(level, pos).bounds().move(pos).expandTowards(fanPos.getX(), fanPos.getY(), fanPos.getZ()).deflate(1D);

			List<? extends Entity> list = level.getEntities((Entity) null, aabb);

			boolean isSucking = mode == FanMode.SUCK;

			double distanceModifier = TechConfig.COMMON.fanMaxRange.get() - this.range + 1;
			double fanSpeed = isSucking ? -TechConfig.COMMON.fanSpeed.get() : TechConfig.COMMON.fanSpeed.get();
			double fanModSpeed = isSucking ? -TechConfig.COMMON.fanModSpeed.get() : TechConfig.COMMON.fanModSpeed.get();
			double speed = fanSpeed + (fanModSpeed / distanceModifier);

			list.stream().forEach((entity -> {
				if (!(entity instanceof FallingBlockEntity)) {
					if (dir == Direction.UP && mode == FanMode.BLOW) {
						entity.fallDistance = 0F;
					}

					Vector3d mot = entity.getDeltaMovement();
					switch (dir) {
						case DOWN:
							entity.setDeltaMovement(mot.x, mot.y - speed, mot.z);
							break;
						case UP:
							entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
							break;
						case NORTH:
							entity.setDeltaMovement(mot.x, mot.y, mot.z - speed);
							break;
						case SOUTH:
							entity.setDeltaMovement(mot.x, mot.y, mot.z + speed);
							break;
						case WEST:
							entity.setDeltaMovement(mot.x - speed, mot.y, mot.z);
							break;
						case EAST:
							entity.setDeltaMovement(mot.x + speed, mot.y, mot.z);
							break;
					}
				}
			}));

			if (level.isClientSide) {
				if (TechConfig.CLIENT.showFanParticles.get()) {
					BlockPos particlePos = mode == FanMode.BLOW ? pos.relative(dir) : pos;
					AirParticleData particleData = new AirParticleData(pos);

					if (mode == FanMode.BLOW) {
						for (int ii = 1; ii <= range / 4 + 1; ii++) {
							level.addParticle(particleData, particlePos.getX() + this.level.random.nextDouble(), particlePos.getY() + this.level.random.nextDouble(), particlePos.getZ() + this.level.random.nextDouble(), speed, speed, speed);
						}
					} else if (mode == FanMode.SUCK) {
						for (int ii = 1; ii <= range / 4 + 1; ii++) {
							level.addParticle(particleData, particlePos.getX() + this.level.random.nextDouble() + (fanPos.getX() * this.level.random.nextDouble()), particlePos.getY() + this.level.random.nextDouble() + (fanPos.getY() * this.level.random.nextDouble()), particlePos.getZ() + this.level.random.nextDouble() + (fanPos.getZ() * this.level.random.nextDouble()), speed, speed, speed);
						}
					}
				}
			}
		}
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
		cmp.putInt("Mode", mode.ordinal());
		cmp.putInt("OldMode", oldMode.ordinal());
		cmp.putInt("Range", range);
	}

	public void readPacketNBT(CompoundNBT cmp) {
		this.mode = FanMode.VALUES[cmp.getInt("Mode")];
		this.oldMode = FanMode.VALUES[cmp.getInt("OldMode")];
		this.range = cmp.getInt("Range");
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

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public FanMode getMode() {
		return mode;
	}

	public void setMode(FanMode mode) {
		this.mode = mode;
	}

	public FanMode getOldMode() {
		return oldMode;
	}

	public void setOldMode(FanMode oldMode) {
		this.oldMode = oldMode;
	}
}
