package com.grim3212.assorted.tech.common.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

/** The position a GPS has stored, with its dimension so a sensor elsewhere can refuse it. */
public record GpsTarget(GlobalPos target) implements TooltipProvider {

    public static final Codec<GpsTarget> CODEC = GlobalPos.CODEC.xmap(GpsTarget::new, GpsTarget::target);
    public static final StreamCodec<ByteBuf, GpsTarget> STREAM_CODEC = GlobalPos.STREAM_CODEC.map(GpsTarget::new, GpsTarget::target);

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flag, DataComponentGetter components) {
        BlockPos pos = this.target.pos();
        tooltip.accept(Component.translatable("tooltip.gps.stored", pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GRAY));
        if (flag.isAdvanced()) {
            tooltip.accept(Component.literal(this.target.dimension().identifier().toString()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
