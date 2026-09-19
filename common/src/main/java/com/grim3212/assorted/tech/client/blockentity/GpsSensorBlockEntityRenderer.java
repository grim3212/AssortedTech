package com.grim3212.assorted.tech.client.blockentity;

import com.grim3212.assorted.tech.common.block.blockentity.GpsSensorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

/**
 * Outlines the box a GPS sensor watches, green when it can see into it and red when the position is
 * solid or out of range. The box is up to the upgraded sensor's range away, so it is drawn even
 * when the sensor block itself is off screen.
 */
public class GpsSensorBlockEntityRenderer implements BlockEntityRenderer<GpsSensorBlockEntity, RangeRenderState> {

    private static final float LINE_WIDTH = 2.0F;

    public GpsSensorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RangeRenderState createRenderState() {
        return new RangeRenderState();
    }

    @Override
    public void extractRenderState(GpsSensorBlockEntity entity, RangeRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);

        GpsSensorBlockEntity.TargetStatus status = entity.targetStatus();
        // A position in another dimension has nowhere here to outline.
        state.showRange = entity.isShowingTarget() && status != GpsSensorBlockEntity.TargetStatus.NONE && status != GpsSensorBlockEntity.TargetStatus.OTHER_DIMENSION;
        if (!state.showRange) {
            return;
        }

        AABB box = entity.senseBox().move(-entity.getBlockPos().getX(), -entity.getBlockPos().getY(), -entity.getBlockPos().getZ()).inflate(0.002D);
        state.obstructed = status != GpsSensorBlockEntity.TargetStatus.GOOD;
        state.shape = Shapes.create(box);
    }

    @Override
    public void submit(RangeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.showRange) {
            submitNodeCollector.submitShapeOutline(poseStack, state.shape, RenderTypes.lines(), state.color(), LINE_WIDTH, false);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
