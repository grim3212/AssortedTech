package com.grim3212.assorted.tech.common.network;

import com.grim3212.assorted.tech.AssortedTech;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class PacketHandler {
	private static final String PROTOCOL = "7";
	public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(AssortedTech.MODID, "channel"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

	public static void init() {
		int id = 0;
		HANDLER.registerMessage(id++, FanUpdatePacket.class, FanUpdatePacket::encode, FanUpdatePacket::decode, FanUpdatePacket::handle);
	}

	/**
	 * Send message to all within 64 blocks that have this chunk loaded
	 */
	public static void sendToNearby(World world, BlockPos pos, Object toSend) {
		if (world instanceof ServerWorld) {
			ServerWorld ws = (ServerWorld) world;

			ws.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64 * 64).forEach(p -> HANDLER.send(PacketDistributor.PLAYER.with(() -> p), toSend));
		}
	}

	public static void sendToNearby(World world, Entity e, Object toSend) {
		sendToNearby(world, e.blockPosition(), toSend);
	}

	public static void sendTo(ServerPlayerEntity playerMP, Object toSend) {
		HANDLER.sendTo(toSend, playerMP.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void sendNonLocal(ServerPlayerEntity playerMP, Object toSend) {
		if (playerMP.server.isDedicatedServer() || !playerMP.getGameProfile().getName().equals(playerMP.server.getSingleplayerName())) {
			sendTo(playerMP, toSend);
		}
	}

	public static void sendToServer(Object msg) {
		HANDLER.sendToServer(msg);
	}

	private PacketHandler() {
	}

}
