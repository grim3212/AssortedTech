package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.tech.client.screen.AlarmScreen;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AlarmOpenPacket {
    private final BlockPos pos;

    public AlarmOpenPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static AlarmOpenPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();

        return new AlarmOpenPacket(pos);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static void handle(AlarmOpenPacket packet, Player player) {
        BlockEntity te = player.level.getBlockEntity(packet.pos);
        if (te instanceof AlarmBlockEntity alarmBlockEntity) {
            Minecraft.getInstance().setScreen(new AlarmScreen(alarmBlockEntity));
        }
    }
}
