package com.grim3212.assorted.tech.client.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Shared render state for the three block entities that draw a wireframe box showing how far their
 * effect reaches (sensor, gravity field, directional gravity field).
 * <p>
 * A {@code BlockEntityRenderer} is split into an extract phase and a submit phase in 26.2, and only
 * the extract phase is handed the block entity - so the level scan that decides how long the box is,
 * and whether something is blocking it, has to happen there and be carried across in this object.
 * The submit phase never touches the level.
 */
public class RangeRenderState extends BlockEntityRenderState {

    /**
     * Wireframe colours were four floats in 1.20.1 and are a single packed ARGB int now.
     * <p>
     * The 1.20.1 code passed {@code 255.0F} where {@code LevelRenderer#renderShape} wanted a 0-1
     * component, which {@code VertexConsumer#color(float,float,float,float)} multiplied by 255 and
     * truncated to a byte - so 255.0F actually reached the GPU as 1 and 1.0F as 255. The visible
     * result was a red box when obstructed and a green one when clear; these two constants say that
     * outright.
     */
    public static final int OBSTRUCTED_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.0F, 0.0F);
    public static final int CLEAR_COLOR = ARGB.colorFromFloat(1.0F, 0.0F, 1.0F, 0.0F);

    /** False when the block entity is not showing its range, in which case nothing is submitted. */
    public boolean showRange;
    /** True when a full block face stopped the scan short of the configured range. */
    public boolean obstructed;
    /** The wireframe box, in block-local coordinates. */
    public VoxelShape shape = Shapes.empty();

    public int color() {
        return this.obstructed ? OBSTRUCTED_COLOR : CLEAR_COLOR;
    }
}
