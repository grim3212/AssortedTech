package com.grim3212.assorted.tech.common.network;

import java.util.function.Supplier;

import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.util.FanMode;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class FanUpdatePacket {

	private BlockPos pos;
	private FanMode mode;
	private int range;

	public FanUpdatePacket(BlockPos pos, FanMode mode, int range) {
		this.pos = pos;
		this.mode = mode;
		this.range = range;
	}

	public static FanUpdatePacket decode(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		FanMode mode = buf.readEnum(FanMode.class);
		int range = buf.readInt();

		return new FanUpdatePacket(pos, mode, range);
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.pos);
		buf.writeEnum(this.mode);
		buf.writeInt(this.range);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
			ctx.get().enqueueWork(() -> {
				Player player = ctx.get().getSender();
				BlockState state = player.level.getBlockState(this.pos);
				BlockEntity te = player.level.getBlockEntity(this.pos);

				if (te instanceof FanBlockEntity fan) {
					fan.setOldMode(this.mode);
					fan.setRange(this.range);
					if (fan.getMode() != FanMode.OFF) {
						fan.setMode(this.mode);
						player.level.setBlock(pos, state.setValue(FanBlock.MODE, mode), 3);
					}

				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}
