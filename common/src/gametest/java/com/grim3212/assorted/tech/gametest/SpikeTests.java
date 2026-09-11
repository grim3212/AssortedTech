package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Blocks;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.grim3212.assorted.tech.gametest.TechTestSupport.*;

/**
 * Spikes damaging entities, scaled by spike type.
 */
final class SpikeTests {

    private SpikeTests() {
    }

    static void register(BiConsumer<String, Consumer<GameTestHelper>> out) {
        out.accept("spike_damage_scales_with_type", SpikeTests::spikeDamageScalesWithType);
        out.accept("every_spike_type_damages", SpikeTests::everySpikeTypeDamages);
    }

    /**
     * A powered spike hurts what stands on it, and {@link SpikeType#getDamage()} is what decides how
     * much: 14 damage kills a 10 health pig outright, 2 damage does not come close.
     * <p>
     * The redstone goes in first so the spike powers itself from its own {@code onPlace} neighbour
     * sweep; setting {@code POWERED} by hand would be undone by the scheduled tick that follows.
     */
    private static void spikeDamageScalesWithType(GameTestHelper helper) {
        BlockPos weak = new BlockPos(2, 1, 4);
        BlockPos strong = new BlockPos(6, 1, 4);

        helper.setBlock(new BlockPos(2, 0, 3), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(6, 0, 3), Blocks.REDSTONE_BLOCK);
        helper.setBlock(weak, spike(SpikeType.WOOD).defaultBlockState().setValue(SpikeBlock.FACING, Direction.UP));
        helper.setBlock(strong, spike(SpikeType.NETHERITE).defaultBlockState().setValue(SpikeBlock.FACING, Direction.UP));

        Pig weakVictim = helper.spawnWithNoFreeWill(EntityTypes.PIG, weak);
        Pig strongVictim = helper.spawnWithNoFreeWill(EntityTypes.PIG, strong);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(weak, SpikeBlock.POWERED, true);
            helper.assertBlockProperty(strong, SpikeBlock.POWERED, true);
            helper.assertFalse(strongVictim.isAlive(),
                    "a netherite spike (14 damage) did not kill a 10 health pig");
            helper.assertTrue(weakVictim.isAlive(),
                    "a wood spike (2 damage) killed a 10 health pig");
            helper.assertTrue(weakVictim.getHealth() < weakVictim.getMaxHealth(),
                    "a wood spike did not hurt the pig standing on it");
        });
    }

    /**
     * Every {@link SpikeType}, each with its own pig standing on it, checked against the exact
     * damage the enum declares rather than against "more" and "less".
     * <p>
     * A spike reads its signal from the block <em>behind</em> its face, so the redstone goes in the
     * floor rows between the spikes rather than under them.
     * <p>
     * Only the <em>first</em> hit carries the material's damage, so each pig is watched every tick
     * and the number is taken from the tick its health first moves. Waiting a fixed number of ticks
     * and then reading does not work: a spike is standing on its victim, so it hits again as soon as
     * the invulnerability window lets it, and the second hit lands well inside any window wide enough
     * to be sure the first one has. The spike damage type scales only
     * {@code when_caused_by_living_non_player} and this source has no entity, so difficulty does not
     * enter into it.
     */
    private static void everySpikeTypeDamages(GameTestHelper helper) {
        SpikeType[] types = SpikeType.values();
        Pig[] victims = new Pig[types.length];

        for (int x = 0; x <= 8; x += 2) {
            for (int z = 1; z <= 7; z += 2) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.REDSTONE_BLOCK);
            }
        }

        for (int i = 0; i < types.length; i++) {
            BlockPos pos = spikeSlot(i);
            helper.setBlock(pos, spike(types[i]).defaultBlockState().setValue(SpikeBlock.FACING, Direction.UP));
            victims[i] = helper.spawnWithNoFreeWill(EntityTypes.PIG, pos);
        }

        float[] firstHit = new float[types.length];
        Arrays.fill(firstHit, -1.0F);

        helper.startSequence()
                .thenExecuteFor(20, () -> {
                    for (int i = 0; i < types.length; i++) {
                        if (firstHit[i] < 0.0F) {
                            float taken = victims[i].getMaxHealth() - victims[i].getHealth();
                            if (taken > 0.0F) {
                                firstHit[i] = taken;
                            }
                        }
                    }
                })
                .thenExecute(() -> {
                    for (int i = 0; i < types.length; i++) {
                        SpikeType type = types[i];
                        // A spike cannot take more than the pig has, so the lethal materials all
                        // land on its full ten health.
                        float expected = Math.min(type.getDamage(), victims[i].getMaxHealth());

                        helper.assertBlockProperty(spikeSlot(i), SpikeBlock.POWERED, true);
                        helper.assertTrue(firstHit[i] > 0.0F,
                                "a powered " + type + " spike never hurt the pig standing on it");
                        helper.assertTrue(Math.abs(firstHit[i] - expected) < 0.01F,
                                "a " + type + " spike is worth " + type.getDamage() + " damage but its first hit took "
                                        + firstHit[i]);
                    }
                })
                .thenSucceed();
    }
}
