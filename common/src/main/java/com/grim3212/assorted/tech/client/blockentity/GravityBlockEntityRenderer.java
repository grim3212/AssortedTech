package com.grim3212.assorted.tech.client.blockentity;

import com.grim3212.assorted.tech.common.block.blockentity.GravityBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The undirected gravity field's range box. See {@link RangeRenderState} for why the level scan moved
 * into {@code extractRenderState}.
 * <p>
 * The {@code RenderSystem.enableBlend()} / {@code defaultBlendFunc()} / {@code lineWidth(2.0F)} /
 * {@code depthMask(false)} calls around the old draw are gone. Blending and depth masking are baked
 * into the render pipeline a {@code RenderType} names, and the line width is a parameter of
 * {@link SubmitNodeCollector#submitShapeOutline} - in 1.20.1 those calls were made while only queuing
 * into a {@code MultiBufferSource}, so they had already stopped affecting this draw.
 */
public class GravityBlockEntityRenderer implements BlockEntityRenderer<GravityBlockEntity, RangeRenderState> {

    private static final float LINE_WIDTH = 2.0F;

    public GravityBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RangeRenderState createRenderState() {
        return new RangeRenderState();
    }

    @Override
    public void extractRenderState(GravityBlockEntity entity, RangeRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);

        Level level = entity.getLevel();
        state.showRange = entity.shouldShowRange() && level != null;
        if (!state.showRange) {
            return;
        }

        Direction dir = Direction.NORTH;

        // The plus 1 is because we deflate by 1 so we don't include the block itself
        int maxLength = entity.getRange() + 1;

        boolean obstructed = false;
        int traverse = 1;
        while (traverse < maxLength && !obstructed) {
            BlockPos checkPos = entity.getBlockPos().relative(dir, traverse);
            BlockState checkState = level.getBlockState(checkPos);
            if (Block.isFaceFull(checkState.getCollisionShape(level, checkPos), dir.getOpposite())) {
                obstructed = true;
            } else {
                traverse++;
            }
        }

        AABB aabb = entity.getBlockState().getCollisionShape(level, entity.getBlockPos()).bounds().inflate(maxLength).deflate(1D);
        state.obstructed = obstructed;
        state.shape = Block.box(aabb.minX * 16D, aabb.minY * 16D, aabb.minZ * 16D, aabb.maxX * 16D, aabb.maxY * 16D, aabb.maxZ * 16D);
    }

    @Override
    public void submit(RangeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.showRange) {
            return;
        }

        submitNodeCollector.submitShapeOutline(poseStack, state.shape, RenderTypes.lines(), state.color(), LINE_WIDTH, false);
    }

}
