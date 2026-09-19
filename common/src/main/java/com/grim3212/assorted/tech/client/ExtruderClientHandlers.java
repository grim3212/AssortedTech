package com.grim3212.assorted.tech.client;

import com.grim3212.assorted.tech.common.entity.ExtruderEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;

/** Client-only work for the extruder, reached through {@code DistExecutor} so a server never loads it. */
public final class ExtruderClientHandlers {

    private ExtruderClientHandlers() {
    }

    /**
     * An extruder the player was punching has gone. The attack key is still held for the rest of
     * that click, and with the extruder out of the way the crosshair falls on the block behind it,
     * which the held key would then start breaking, at once in creative. Letting go of the key makes
     * the next block take a click of its own. The client cannot tell a broken extruder from one that
     * unloaded, so this only acts on the one the player is aiming at with attack held.
     */
    public static void releaseAttackIfPunching(ExtruderEntity extruder) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.hitResult instanceof EntityHitResult hit && hit.getEntity() == extruder && minecraft.options.keyAttack.isDown()) {
            minecraft.options.keyAttack.setDown(false);
        }
    }
}
