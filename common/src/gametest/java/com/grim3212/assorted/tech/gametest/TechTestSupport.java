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
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helpers, constants and fixtures shared by AssortedTech's gametest classes, which import them statically.
 */
final class TechTestSupport {

    private TechTestSupport() {
    }

    /** Keeps every test player's profile name distinct when a test places more than one. */
    static final AtomicInteger PLAYERS = new AtomicInteger();

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

    /** Every variant object in a blockstate json: each "variants" entry and each multipart "apply". */
    static List<JsonObject> blockstateVariants(JsonObject blockstate) {
        List<JsonObject> out = new ArrayList<>();
        if (blockstate.has("variants")) {
            for (Map.Entry<String, com.google.gson.JsonElement> entry : blockstate.getAsJsonObject("variants").entrySet()) {
                addVariants(entry.getValue(), out);
            }
        }
        if (blockstate.has("multipart")) {
            for (com.google.gson.JsonElement part : blockstate.getAsJsonArray("multipart")) {
                addVariants(part.getAsJsonObject().get("apply"), out);
            }
        }
        return out;
    }

    static void addVariants(com.google.gson.JsonElement element, List<JsonObject> out) {
        if (element == null) {
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(variant -> out.add(variant.getAsJsonObject()));
        } else {
            out.add(element.getAsJsonObject());
        }
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

    /**
     * Writes a block entity out and reads it back exactly as a chunk save and load would, and hands
     * back the copy that came off the tag.
     */
    static <T extends BlockEntity> T afterReload(GameTestHelper helper, BlockPos pos, Class<T> type) {
        ServerLevel level = helper.getLevel();
        CompoundTag saved = helper.getBlockEntity(pos, type).saveWithFullMetadata(level.registryAccess());
        BlockEntity reloaded = BlockEntity.loadStatic(helper.absolutePos(pos), helper.getBlockState(pos), saved, level.registryAccess());
        helper.assertTrue(type.isInstance(reloaded),
                "a " + type.getSimpleName() + " did not come back from a save and load round trip");
        return type.cast(reloaded);
    }

    /**
     * A real survival mode {@link ServerPlayer} placed in the test level. The mock helpers are
     * either creative and deprecated, or never placed in a level, so any message to them NPEs.
     */
    static ServerPlayer survivalPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "tech-test-" + PLAYERS.incrementAndGet());
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        helper.runBeforeTestEnd(() -> level.getServer().getPlayerList().remove(player));

        player.setGameMode(GameType.SURVIVAL);
        helper.assertFalse(player.isCreative(), "the test player is in creative, which changes every path under test");
        return player;
    }

    /** Stands {@code entity} on top of {@code rel}. */
    static <T extends Entity> T stand(GameTestHelper helper, T entity, BlockPos rel) {
        Vec3 on = helper.absoluteVec(Vec3.atBottomCenterOf(rel.above()));
        entity.snapTo(on.x, on.y, on.z, 0.0F, 0.0F);
        return entity;
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
