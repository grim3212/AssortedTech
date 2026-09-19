package com.grim3212.assorted.tech.client.render;

import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.ExtruderType;
import com.grim3212.assorted.tech.common.entity.ExtruderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The model's drill points along its +X, which after the usual flip of an entity model is west in
 * the world; the pose turns west to the extruder's facing. Hits wobble it like a boat.
 */
public class ExtruderRenderer extends EntityRenderer<ExtruderEntity, ExtruderRenderer.ExtruderRenderState> {

    /** Each material's drill is coloured to match its spikes. */
    private static final Map<ExtruderType, Identifier> TEXTURES = Arrays.stream(ExtruderType.values())
            .collect(Collectors.toMap(Function.identity(), type -> Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/entity/extruders/" + type + ".png"), (a, b) -> a, () -> new EnumMap<>(ExtruderType.class)));

    private final ExtruderModel model;

    /** Also what the item draws, through {@link ExtruderSpecialRenderer}. */
    public static Identifier texture(ExtruderType type) {
        return TEXTURES.get(type);
    }

    public ExtruderRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ExtruderModel(context.bakeLayer(ExtruderModel.LAYER));
    }

    @Override
    public ExtruderRenderState createRenderState() {
        return new ExtruderRenderState();
    }

    @Override
    public void extractRenderState(ExtruderEntity entity, ExtruderRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.facing = entity.getFacing();
        state.type = entity.getExtruderType();
        state.drillWave = entity.isRunning() ? Mth.sin((entity.tickCount + partialTicks) * 0.6F) : 0.0F;
        state.hurtTime = entity.getHurtTime() - partialTicks;
        state.hurtDir = entity.getHurtDir();
        state.damage = Math.max(entity.getDamage() - partialTicks, 0.0F);
    }

    @Override
    public void submit(ExtruderRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, state.boundingBoxHeight / 2.0F, 0.0F);

        switch (state.facing) {
            case UP -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            case DOWN -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            default -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - state.facing.toYRot()));
        }

        if (state.hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(state.hurtTime) * state.hurtTime * state.damage / 10.0F * state.hurtDir));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        submitNodeCollector.submitModel(this.model, state, poseStack, TEXTURES.get(state.type), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        poseStack.popPose();

        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    public static class ExtruderRenderState extends EntityRenderState {
        public Direction facing = Direction.NORTH;
        public ExtruderType type = ExtruderType.IRON;
        public float drillWave;
        public float hurtTime;
        public int hurtDir;
        public float damage;
    }
}
