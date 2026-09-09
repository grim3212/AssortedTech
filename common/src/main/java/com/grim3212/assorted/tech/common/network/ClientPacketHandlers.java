package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.tech.client.screen.AlarmScreen;
import com.grim3212.assorted.tech.client.screen.FanScreen;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientPacketHandlers {

    /**
     * {@code Minecraft#setScreen} is gone - the screen stack belongs to the GUI now, so opening one
     * goes through {@code Minecraft#gui}.
     */
    public static void openAlarmScreen(BlockPos pos) {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        BlockEntity te = clientPlayer.level().getBlockEntity(pos);
        if (te instanceof AlarmBlockEntity alarmBlockEntity) {
            Minecraft.getInstance().gui.setScreen(new AlarmScreen(alarmBlockEntity));
        }
    }

    public static void openFanScreen(BlockPos pos) {
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        BlockEntity te = clientPlayer.level().getBlockEntity(pos);
        if (te instanceof FanBlockEntity fanBlockEntity) {
            Minecraft.getInstance().gui.setScreen(new FanScreen(fanBlockEntity));
        }
    }
}
