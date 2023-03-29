package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.tech.client.screen.FanScreen;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

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

    public static void handle(FanOpenPacket packet, Player player) {
        BlockEntity te = player.level.getBlockEntity(packet.pos);
        if (te instanceof FanBlockEntity fanBlockEntity) {
            Minecraft.getInstance().setScreen(new FanScreen(fanBlockEntity));
        }
    }
}
