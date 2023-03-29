package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AlarmUpdatePacket {

    private final BlockPos pos;
    private final int alarmType;

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

    public static void handle(AlarmUpdatePacket packet, Player player) {
        BlockEntity te = player.level.getBlockEntity(packet.pos);

        if (te instanceof AlarmBlockEntity fan) {
            fan.setAlarmType(packet.alarmType);
        }
    }
}
