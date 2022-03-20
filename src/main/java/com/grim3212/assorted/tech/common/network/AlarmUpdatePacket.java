package com.grim3212.assorted.tech.common.network;

import java.util.function.Supplier;

import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class AlarmUpdatePacket {

	private BlockPos pos;
	private int alarmType;

	public AlarmUpdatePacket(BlockPos pos, int type) {
		this.pos = pos;
		this.alarmType = type;
	}

	public static AlarmUpdatePacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int range = buf.readInt();

		return new AlarmUpdatePacket(pos, range);
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeInt(this.alarmType);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
			ctx.get().enqueueWork(() -> {
				Player player = ctx.get().getSender();
				BlockEntity te = player.level.getBlockEntity(this.pos);

				if (te instanceof AlarmBlockEntity fan) {
					fan.setAlarmType(this.alarmType);

				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}
