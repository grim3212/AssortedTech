package com.grim3212.assorted.tech.common.block.blockentity;

import com.grim3212.assorted.tech.common.util.TechSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class AlarmBlockEntity extends BlockEntity {

	private int alarmType = 0;

	public AlarmBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public AlarmBlockEntity(BlockPos pos, BlockState state) {
		super(TechBlockEntityTypes.ALARM.get(), pos, state);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.alarmType = nbt.getInt("AlarmType");
	}

	@Override
	protected void saveAdditional(CompoundTag cmp) {
		super.saveAdditional(cmp);
		cmp.putInt("AlarmType", this.alarmType);
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

	public int getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(int alarmType) {
		this.alarmType = alarmType;
		this.markUpdated();
	}

	public static RegistryObject<SoundEvent> getSound(int type) {
		switch (type) {
			case 0:
				return TechSounds.ALARM_A;
			case 1:
				return TechSounds.ALARM_B;
			case 2:
				return TechSounds.ALARM_C;
			case 3:
				return TechSounds.ALARM_D;
			case 4:
				return TechSounds.ALARM_E;
			case 5:
				return TechSounds.ALARM_F;
			case 6:
				return TechSounds.ALARM_G;
			case 7:
				return TechSounds.ALARM_H;
			case 8:
				return TechSounds.ALARM_I;
			case 9:
				return TechSounds.ALARM_J;
			case 10:
				return TechSounds.ALARM_K;
			case 11:
				return TechSounds.ALARM_L;
			case 12:
				return TechSounds.ALARM_M;
			default:
				return TechSounds.ALARM_N;
		}
	}
}
