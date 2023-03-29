package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.tech.TechCommonMod;
import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.client.TechClient;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.particle.air.AirParticleData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

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

            double distanceModifier = TechCommonMod.COMMON_CONFIG.fanMaxRange.get() - this.range + 1;
            double fanSpeed = isSucking ? -TechCommonMod.COMMON_CONFIG.fanSpeed.get() : TechCommonMod.COMMON_CONFIG.fanSpeed.get();
            double fanModSpeed = isSucking ? -TechCommonMod.COMMON_CONFIG.fanModSpeed.get() : TechCommonMod.COMMON_CONFIG.fanModSpeed.get();
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
                if (TechClient.CLIENT_CONFIG.showFanParticles.get()) {
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
        this.mode = FanMode.VALUES[nbt.getInt("Mode")];
        this.oldMode = FanMode.VALUES[nbt.getInt("OldMode")];
        this.range = nbt.getInt("Range");
    }

    @Override
    protected void saveAdditional(CompoundTag cmp) {
        super.saveAdditional(cmp);
        cmp.putInt("Mode", mode.ordinal());
        cmp.putInt("OldMode", oldMode.ordinal());
        cmp.putInt("Range", range);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
        this.markUpdated();
    }

    public FanMode getMode() {
        return mode;
    }

    public void setMode(FanMode mode) {
        this.mode = mode;
        this.markUpdated();
    }

    public FanMode getOldMode() {
        return oldMode;
    }

    public void setOldMode(FanMode oldMode) {
        this.oldMode = oldMode;
        this.markUpdated();
    }
}
