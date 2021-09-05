package com.grim3212.assorted.tech.common.block.blockentity;

import java.util.List;

import com.grim3212.assorted.tech.client.particle.AirParticleData;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.util.FanMode;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FanBlockEntity extends BlockEntity {

	private int range = 1;
	private FanMode mode = FanMode.BLOW;
	private FanMode oldMode = FanMode.BLOW;

	public FanBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public FanBlockEntity(BlockPos pos, BlockState state) {
		super(TechBlockEntityTypes.FAN.get(), pos, state);
	}

	public void tick() {
		BlockPos pos = this.getBlockPos();
		BlockState state = this.level.getBlockState(pos);

		if (mode != FanMode.OFF && state.getBlock() == TechBlocks.FAN.get()) {
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
			Vec3i fanPos = dir.getNormal().multiply(distance);
			AABB aabb = state.getCollisionShape(level, pos).bounds().move(pos).expandTowards(fanPos.getX(), fanPos.getY(), fanPos.getZ()).deflate(1D);

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

					Vec3 mot = entity.getDeltaMovement();
					switch (dir) {
						case DOWN -> entity.setDeltaMovement(mot.x, mot.y - speed, mot.z);
						case UP -> entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
						case NORTH -> entity.setDeltaMovement(mot.x, mot.y, mot.z - speed);
						case SOUTH -> entity.setDeltaMovement(mot.x, mot.y, mot.z + speed);
						case WEST -> entity.setDeltaMovement(mot.x - speed, mot.y, mot.z);
						case EAST -> entity.setDeltaMovement(mot.x + speed, mot.y, mot.z);
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
		cmp.putInt("Mode", mode.ordinal());
		cmp.putInt("OldMode", oldMode.ordinal());
		cmp.putInt("Range", range);
	}

	public void readPacketNBT(CompoundTag cmp) {
		this.mode = FanMode.VALUES[cmp.getInt("Mode")];
		this.oldMode = FanMode.VALUES[cmp.getInt("OldMode")];
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
