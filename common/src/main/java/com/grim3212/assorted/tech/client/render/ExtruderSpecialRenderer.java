package com.grim3212.assorted.tech.client.render;

import com.grim3212.assorted.tech.Constants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Draws an extruder item as the extruder itself, the entity's model and its material's texture,
 * posed as {@link ExtruderRenderer} poses one facing north. The item's base model inherits
 * {@code block/block}'s display settings, where north is the face shown on the left in the
 * inventory, as a furnace shows its front, so the drill is seen there.
 */
public class ExtruderSpecialRenderer implements NoDataSpecialModelRenderer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "extruder");

    /** A resting extruder: the drill drawn in, nothing hurt. */
    private static final ExtruderRenderer.ExtruderRenderState STILL = new ExtruderRenderer.ExtruderRenderState();

    private final ExtruderModel model;
    private final Identifier texture;

    public ExtruderSpecialRenderer(ExtruderModel model, Identifier texture) {
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
        pose(poseStack);
        submitNodeCollector.order(0).submitModel(this.model, STILL, poseStack, this.texture, lightCoords, overlayCoords, outlineColor, null);
        if (hasFoil) {
            submitNodeCollector.order(1).submitModel(this.model, STILL, poseStack, RenderTypes.entityGlint(), lightCoords, overlayCoords, outlineColor, null);
        }
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        pose(poseStack);
        this.model.setupAnim(STILL);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    /** Into the middle of the item's block-sized space, the drill turned north, and the usual entity model flip. */
    private static void pose(PoseStack poseStack) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
    }

    /** The material's entity texture is all that differs between the extruders. */
    public record Unbaked(Identifier texture) implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<ExtruderSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(
                        Identifier.CODEC.fieldOf("texture").forGetter(ExtruderSpecialRenderer.Unbaked::texture)
                ).apply(i, ExtruderSpecialRenderer.Unbaked::new)
        );

        @Override
        public MapCodec<ExtruderSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ExtruderSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new ExtruderSpecialRenderer(new ExtruderModel(context.entityModelSet().bakeLayer(ExtruderModel.LAYER)), this.texture);
        }
    }
}
