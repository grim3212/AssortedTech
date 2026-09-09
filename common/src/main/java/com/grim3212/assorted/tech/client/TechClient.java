package com.grim3212.assorted.tech.client;

import com.google.common.collect.ImmutableList;
import com.grim3212.assorted.lib.platform.ClientServices;
import com.grim3212.assorted.tech.client.blockentity.GravityBlockEntityRenderer;
import com.grim3212.assorted.tech.client.blockentity.GravityDirectionalBlockEntityRenderer;
import com.grim3212.assorted.tech.client.blockentity.SensorBlockEntityRenderer;
import com.grim3212.assorted.tech.client.color.BridgeItemTintSource;
import com.grim3212.assorted.tech.client.model.BridgeUnbakedModel;
import com.grim3212.assorted.tech.client.particle.AirParticle;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.particle.TechParticleTypes;
import com.grim3212.assorted.tech.config.TechClientConfig;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
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

        // TODO(26.2): the four registerRenderType calls that used to live here are gone.
        //  What they did: told ItemBlockRenderTypes that the alarm, the flip flop torch, the glowstone
        //  torch and every spike block draw in the cutout chunk layer.
        //  Why they cannot be expressed: ItemBlockRenderTypes was deleted and RenderType lost its
        //  solid()/cutout()/translucent() factories - a quad's chunk layer is derived while baking from
        //  the transparency of the sprite it uses and lands on BakedQuad.MaterialInfo#layer(). A block
        //  declares its layer from its block model json with "render_type": "minecraft:cutout"
        //  instead, so those blocks' model jsons need that key adding in datagen. AssortedLib keeps
        //  IClientHelper#registerRenderType as a no-op on both loaders, so calling it would have looked
        //  correct and done nothing.

        ClientServices.CLIENT.registerModelLoader(BridgeUnbakedModel.LOADER_NAME, BridgeUnbakedModel.Loader.INSTANCE);

        // BlockColor became BlockTintSource: color(state) answers the in-hand colour and
        // colorInWorld(state, level, pos) the placed one, so the nullable BlockPos the old lambda
        // branched on is gone - the two cases are separate methods. The tint layer is the source's
        // position in the block's list rather than an argument, and colours are ARGB, so an opaque
        // white is -1 rather than 0xFFFFFF.
        ClientServices.CLIENT.registerBlockColor(new BlockTintSource() {
            @Override
            public int color(BlockState state) {
                return -1;
            }

            @Override
            public int colorInWorld(BlockState state, BlockAndTintGetter worldIn, BlockPos pos) {
                BlockEntity te = worldIn.getBlockEntity(pos);
                if (te instanceof BridgeBlockEntity bridge) {
                    BlockState stored = bridge.getStoredBlockState();
                    if (stored != Blocks.AIR.defaultBlockState()) {
                        BlockTintSource source = ClientServices.CLIENT.getBlockColors().getTintSource(stored, 0);
                        return source != null ? source.colorInWorld(stored, worldIn, pos) : -1;
                    }

                    return ARGB.opaque(state.getValue(BridgeBlock.TYPE).getRenderColor());
                }
                return -1;
            }
        }, () -> ImmutableList.of(TechBlocks.BRIDGE.get()));

        // TODO(26.2): registering this codec is only half of what the old registerItemColor call did.
        //  See BridgeItemTintSource - the bridge's item model json has to list
        //  {"type": "assortedtech:bridge"} in its "tints" array before anything consults it, which is a
        //  datagen change this class cannot make.
        ClientServices.CLIENT.registerItemTintSource(BridgeItemTintSource.ID, BridgeItemTintSource.MAP_CODEC);
    }

}
