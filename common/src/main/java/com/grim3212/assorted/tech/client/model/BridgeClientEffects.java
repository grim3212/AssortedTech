package com.grim3212.assorted.tech.client.model;

import com.grim3212.assorted.lib.core.block.effects.ClientEffectUtils;
import com.grim3212.assorted.lib.core.block.effects.IBlockClientEffects;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BridgeClientEffects implements IBlockClientEffects {
    @Override
    public boolean addHitEffects(BlockState state, Level level, BlockPos pos, Direction dir, ParticleEngine manager) {
        if (state.getBlock() instanceof BridgeBlock bridge) {
            BlockState stored = bridge.getStoredState(level, pos);
            if (!stored.isAir()) {
                return ClientEffectUtils.addHitEffects(level, pos, dir, stored, manager);
            }
        }

        return false;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (state.getBlock() instanceof BridgeBlock bridge) {
            BlockState stored = bridge.getStoredState(level, pos);
            if (!stored.isAir()) {
                ClientEffectUtils.addBlockDestroyEffects(level, pos, stored, manager, level);
                return true;
            }
        }

        return false;
    }
}
