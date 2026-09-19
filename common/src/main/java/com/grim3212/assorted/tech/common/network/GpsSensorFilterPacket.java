package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorBlockEntity;
import com.grim3212.assorted.tech.common.inventory.GpsSensorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

/**
 * Text typed into an open GPS sensor menu: a plain sensor's filter, sent as it changes like an
 * anvil's rename, or an entry to add to an upgraded sensor's list. It goes to the sender's open
 * menu, so the menu's own reach check covers it.
 */
public class GpsSensorFilterPacket {

    private final String text;
    private final boolean addEntry;

    public GpsSensorFilterPacket(String text, boolean addEntry) {
        this.text = text;
        this.addEntry = addEntry;
    }

    public static GpsSensorFilterPacket decode(FriendlyByteBuf buf) {
        return new GpsSensorFilterPacket(buf.readUtf(GpsSensorBlockEntity.MAX_FILTER_LENGTH), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.text, GpsSensorBlockEntity.MAX_FILTER_LENGTH);
        buf.writeBoolean(this.addEntry);
    }

    public static void handle(GpsSensorFilterPacket packet, Player player) {
        if (player.containerMenu instanceof GpsSensorMenu menu && menu.stillValid(player)) {
            if (packet.addEntry) {
                menu.addEntry(packet.text);
            } else {
                menu.setFilterText(packet.text);
            }
        }
    }
}
