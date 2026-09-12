package com.grim3212.assorted.tech.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grim3212.assorted.lib.registry.IRegistryObject;
import com.grim3212.assorted.tech.api.util.SensorType;
import com.grim3212.assorted.tech.api.util.SpikeType;
import com.grim3212.assorted.tech.common.block.GlowstoneTorchBlock;
import com.grim3212.assorted.tech.common.block.GravityBlock;
import com.grim3212.assorted.tech.common.block.GravityDirectionalBlock;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.SpikeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static com.grim3212.assorted.lib.test.TestSupport.stand;
import static com.grim3212.assorted.lib.test.TestSupport.survivalPlayer;

/**
 * Helpers, constants and fixtures shared by AssortedTech's gametest classes, which import them
 * statically, alongside AssortedLib's {@code TestSupport}.
 */
final class TechTestSupport {

    private TechTestSupport() {
    }

    /** One subject per sensor type - the narrowest thing that satisfies that type's predicate. */
    static void spawnSensorSubject(GameTestHelper helper, SensorType type, BlockPos pos) {
        switch (type) {
            // Any entity at all, so a dropped item proves it is not quietly a living-entity check.
            case WOOD -> helper.spawnItem(Items.STICK, Vec3.atCenterOf(pos));
            case STONE -> helper.spawnWithNoFreeWill(EntityTypes.PIG, pos);
            case IRON -> stand(helper, survivalPlayer(helper), pos.below());
            case MOSSY_COBBLESTONE -> helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos);
            case PRISMARINE -> helper.spawnWithNoFreeWill(EntityTypes.SQUID, pos);
            case GOLD -> helper.spawnItem(Items.STICK, Vec3.atCenterOf(pos));
            case EMERALD -> helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, pos);
            case NETHERRACK -> helper.spawnWithNoFreeWill(EntityTypes.PIGLIN, pos);
            case COBWEB -> helper.spawnWithNoFreeWill(EntityTypes.SILVERFISH, pos);
            case END_STONE -> helper.spawnWithNoFreeWill(EntityTypes.ENDERMITE, pos);
            case HAY_BALE -> helper.spawnWithNoFreeWill(EntityTypes.WOLF, pos);
            case FEATHER -> helper.spawnWithNoFreeWill(EntityTypes.CHICKEN, pos);
        }
    }

    /** Two apart in both axes, so no pig can reach into a neighbouring spike. */
    static BlockPos spikeSlot(int index) {
        return new BlockPos((index % 5) * 2, 1, (index / 5) * 2);
    }

    /** Reads a json off the mod's own classpath, or null if it is not there. */
    static JsonObject readJson(String path) {
        try (InputStream in = TechTestSupport.class.getResourceAsStream(path)) {
            return in == null ? null : JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    static boolean resourceExists(String path) {
        try (InputStream in = TechTestSupport.class.getResourceAsStream(path)) {
            return in != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads POWERED off whichever gravity block is there. The plain and directional blocks each
     * declare their own "powered" property, and properties resolve by identity, so the wrong one
     * throws.
     */
    static boolean isPowered(GameTestHelper helper, BlockPos pos) {
        BlockState state = helper.getBlockState(pos);
        return state.getBlock() instanceof GravityDirectionalBlock
                ? state.getValue(GravityDirectionalBlock.POWERED)
                : state.getValue(GravityBlock.POWERED);
    }

    /**
     * Sets every position back to air. Registered through {@code runBeforeTestEnd} by anything that
     * lights a torch, so it also runs on failure: block light crosses test box walls and would skew
     * a neighbouring test.
     */
    static void extinguish(GameTestHelper helper, BlockPos... positions) {
        for (BlockPos pos : positions) {
            helper.setBlock(pos, Blocks.AIR);
        }
    }

    /** The light level the block itself claims for the state at {@code pos}. */
    static int lightEmission(GameTestHelper helper, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        BlockState state = helper.getLevel().getBlockState(absolute);
        return ((GlowstoneTorchBlock) state.getBlock()).getLightEmission(state, helper.getLevel(), absolute);
    }

    /** The spike and sensor blocks are registered as one list per enum, in enum order. */
    static SpikeBlock spike(SpikeType type) {
        return TechBlocks.SPIKES.stream().map(IRegistryObject::get)
                .filter(block -> block.getSpikeType() == type).findFirst().orElseThrow();
    }

    static SensorBlock sensorOf(SensorType type) {
        return TechBlocks.SENSORS.stream().map(IRegistryObject::get)
                .filter(block -> block.getSensorType() == type).findFirst().orElseThrow();
    }
}
