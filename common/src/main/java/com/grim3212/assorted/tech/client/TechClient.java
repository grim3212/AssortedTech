package com.grim3212.assorted.tech.client;

import com.google.common.collect.ImmutableList;
import com.grim3212.assorted.lib.platform.ClientServices;
import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.tech.client.blockentity.GravityBlockEntityRenderer;
import com.grim3212.assorted.tech.client.blockentity.GravityDirectionalBlockEntityRenderer;
import com.grim3212.assorted.tech.client.blockentity.SensorBlockEntityRenderer;
import com.grim3212.assorted.tech.client.model.BridgeUnbakedModel;
import com.grim3212.assorted.tech.client.particle.AirParticle;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.particle.TechParticleTypes;
import com.grim3212.assorted.tech.config.TechClientConfig;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TechClient {

    public static final TechClientConfig CLIENT_CONFIG = new TechClientConfig();

    public static void init() {
        ClientServices.CLIENT.registerParticle(TechParticleTypes.AIR, AirParticle.Factory::new);

        ClientServices.CLIENT.registerBlockEntityRenderer(TechBlockEntityTypes.SENSOR, SensorBlockEntityRenderer::new);
        ClientServices.CLIENT.registerBlockEntityRenderer(TechBlockEntityTypes.GRAVITY, GravityBlockEntityRenderer::new);
        ClientServices.CLIENT.registerBlockEntityRenderer(TechBlockEntityTypes.GRAVITY_DIRECTIONAL, GravityDirectionalBlockEntityRenderer::new);

        ClientServices.CLIENT.registerModelLoader(BridgeUnbakedModel.LOADER_NAME, BridgeUnbakedModel.Loader.INSTANCE);
        ClientServices.CLIENT.registerBlockColor(new BlockColor() {
            BlockColors blocks = ClientServices.CLIENT.getBlockColors();

            @Override
            public int getColor(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, int tint) {
                if (pos != null) {
                    BlockEntity te = worldIn.getBlockEntity(pos);
                    if (te != null && te instanceof BridgeBlockEntity bridge) {

                        if (bridge.getStoredBlockState() != Blocks.AIR.defaultBlockState()) {
                            return blocks.getColor(bridge.getStoredBlockState(), worldIn, pos, tint);
                        }

                        return state.getValue(BridgeBlock.TYPE).getRenderColor();
                    }
                }
                return 16777215;
            }
        }, () -> ImmutableList.of(TechBlocks.BRIDGE.get()));

        ClientServices.CLIENT.registerItemColor(new ItemColor() {
            ItemColors items = ClientServices.CLIENT.getItemColors();

            @Override
            public int getColor(ItemStack stack, int tint) {
                if (stack != null && stack.hasTag()) {
                    if (stack.getTag().contains("stored_state")) {
                        BlockState stored = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), NBTHelper.getTag(stack, "stored_state"));
                        ItemStack colorStack = new ItemStack(stored.getBlock());
                        if (colorStack.getItem() != null) {
                            return items.getColor(colorStack, tint);
                        }
                    }
                }
                return 16777215;
            }
        }, () -> ImmutableList.of(TechBlocks.BRIDGE.get().asItem()));
    }

}
