package com.grim3212.assorted.tech.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Stores the position in front of the clicked face, the space a GPS sensor then watches. Using it in
 * the air while sneaking forgets the position.
 */
public class GpsItem extends Item {

    public GpsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos target = context.getClickedPos().relative(context.getClickedFace());
        Level level = context.getLevel();
        context.getItemInHand().set(TechDataComponents.GPS_TARGET.get(), new GpsTarget(GlobalPos.of(level.dimension(), target)));
        level.playSound(context.getPlayer(), target, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (context.getPlayer() instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("message.gps.stored", target.getX(), target.getY(), target.getZ()), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && stack.has(TechDataComponents.GPS_TARGET.get())) {
            stack.remove(TechDataComponents.GPS_TARGET.get());
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("message.gps.cleared"), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
