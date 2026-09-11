package com.grim3212.assorted.tech.client.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Render state for the sensor and gravity fields' range boxes. Only the extract phase sees the
 * block entity, so the level scan that sizes the box and finds obstructions happens there and is
 * carried here to the submit phase.
 */
public class RangeRenderState extends BlockEntityRenderState {

    /** Wireframe colours as packed ARGB: red when obstructed, green when clear. */
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
