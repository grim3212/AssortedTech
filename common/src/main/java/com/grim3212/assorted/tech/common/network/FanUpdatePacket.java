package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FanUpdatePacket {

    private final BlockPos pos;
    private final FanMode mode;
    private final int range;

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

    public static void handle(FanUpdatePacket packet, Player player) {
        BlockState state = player.level.getBlockState(packet.pos);
        BlockEntity te = player.level.getBlockEntity(packet.pos);

        if (te instanceof FanBlockEntity fan) {
            fan.setOldMode(packet.mode);
            fan.setRange(packet.range);
            if (fan.getMode() != FanMode.OFF) {
                fan.setMode(packet.mode);
                player.level.setBlock(packet.pos, state.setValue(FanBlock.MODE, packet.mode), 3);
            }

        }
    }
}
