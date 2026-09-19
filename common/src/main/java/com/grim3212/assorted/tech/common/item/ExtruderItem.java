package com.grim3212.assorted.tech.common.item;

import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.common.entity.ExtruderEntity;
import com.grim3212.assorted.tech.common.entity.TechEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/** Places an extruder of its material against the clicked face, pointed the way the player is looking. */
public class ExtruderItem extends Item {

    private final ExtruderType type;

    public ExtruderItem(Properties properties, ExtruderType type) {
        super(properties);
        this.type = type;
    }

    public ExtruderType getExtruderType() {
        return this.type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ExtruderEntity extruder = TechEntities.EXTRUDER.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (extruder == null) {
            return InteractionResult.FAIL;
        }

        extruder.setExtruderType(this.type);
        extruder.placeAt(pos);
        extruder.setFacing(context.getPlayer() != null ? Direction.orderedByNearest(context.getPlayer())[0] : context.getHorizontalDirection());
        if (!level.noCollision(extruder, extruder.getBoundingBox())) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            ItemStack stack = context.getItemInHand();
            extruder.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
            level.addFreshEntity(extruder);
            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
            stack.consume(1, context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }
}
