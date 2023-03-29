package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.lib.platform.Services;
import com.grim3212.assorted.lib.platform.services.INetworkHelper;
import com.grim3212.assorted.tech.Constants;
import net.minecraft.resources.ResourceLocation;

public class TechPackets {

    public static void init() {
        Services.NETWORK.register(new INetworkHelper.MessageHandler<>(resource("alarm_open"), AlarmOpenPacket.class, AlarmOpenPacket::encode, AlarmOpenPacket::decode, AlarmOpenPacket::handle, INetworkHelper.MessageBoundSide.CLIENT));
        Services.NETWORK.register(new INetworkHelper.MessageHandler<>(resource("alarm_update"), AlarmUpdatePacket.class, AlarmUpdatePacket::encode, AlarmUpdatePacket::decode, AlarmUpdatePacket::handle, INetworkHelper.MessageBoundSide.SERVER));
        Services.NETWORK.register(new INetworkHelper.MessageHandler<>(resource("fan_open"), FanOpenPacket.class, FanOpenPacket::encode, FanOpenPacket::decode, FanOpenPacket::handle, INetworkHelper.MessageBoundSide.CLIENT));
        Services.NETWORK.register(new INetworkHelper.MessageHandler<>(resource("fan_update"), FanUpdatePacket.class, FanUpdatePacket::encode, FanUpdatePacket::decode, FanUpdatePacket::handle, INetworkHelper.MessageBoundSide.SERVER));
    }

    private static ResourceLocation resource(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

}
