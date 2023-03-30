package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.lib.dist.Dist;
import com.grim3212.assorted.lib.dist.DistExecutor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class FanOpenPacket {
    private final BlockPos pos;

    public FanOpenPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static FanOpenPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();

        return new FanOpenPacket(pos);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public static void handle(FanOpenPacket packet, Player serverPlayer) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandlers.openFanScreen(packet.pos));
    }
}
