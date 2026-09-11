package com.grim3212.assorted.tech.gametest;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderSet;
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
        out.accept("spikes_with_no_material_are_uncraftable", SpikeTests::spikesWithNoMaterialAreUncraftable);
    }

    /**
     * A powered spike's damage is {@link SpikeType#getDamage()}: 14 kills a 10 health pig, 2 does
     * not. The redstone goes in first so the spike powers itself in {@code onPlace}; setting {@code
     * POWERED} by hand is undone by the scheduled tick.
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
     * Every {@link SpikeType} deals exactly its declared damage. The redstone goes in the floor
     * between the spikes, since a spike reads the block behind its face. Damage is read on the tick
     * a pig's health first moves: the spike hits again as soon as invulnerability lapses, so a
     * fixed wait can catch two hits. A source with no entity does not scale with difficulty.
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

    /**
     * With {@code hideUncraftableItems} on, a spike is hidden when nothing can be its material: its
     * tag is empty or undefined. The config is off in tests, so this checks the rule directly;
     * Assorted Core's metals have no tag here, which exercises the undefined case.
     */
    private static void spikesWithNoMaterialAreUncraftable(GameTestHelper helper) {
        List<String> wrong = new ArrayList<>();
        int undefined = 0;
        for (SpikeType type : SpikeType.values()) {
            Optional<HolderSet.Named<Item>> tag = BuiltInRegistries.ITEM.get(type.getMaterial());
            boolean noMaterial = tag.isEmpty() || tag.get().size() == 0;
            if (tag.isEmpty()) {
                undefined++;
            }
            if (type.isUncraftable() != noMaterial) {
                wrong.add(type.getSerializedName() + (noMaterial ? " has no material but counts as craftable" : " has a material but counts as uncraftable"));
            }
        }

        helper.assertTrue(wrong.isEmpty(), String.join(", ", wrong));
        helper.assertFalse(SpikeType.IRON.isUncraftable(), "an iron spike counts as uncraftable");
        helper.assertTrue(undefined > 0, "every spike material tag is defined here, so the undefined case was not exercised");
        helper.succeed();
    }
}
