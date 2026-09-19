package com.grim3212.assorted.tech.client.render;

import com.grim3212.assorted.tech.Constants;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** The extruder's body and its two drill pieces, the outer one of which works in and out as it runs. */
public class ExtruderModel extends Model<ExtruderRenderer.ExtruderRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "extruder"), "main");

    private static final float DRILL_X = 3.0F;

    private final ModelPart drill;

    public ExtruderModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.drill = root.getChild("drill");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-7.0F, -8.0F, -7.0F, 13.0F, 15.0F, 14.0F), PartPose.ZERO);
        root.addOrReplaceChild("drill_head", CubeListBuilder.create().texOffs(0, 29).mirror().addBox(0.0F, -8.0F, 0.0F, 3.0F, 12.0F, 12.0F), PartPose.offset(5.0F, 2.0F, -6.0F));
        root.addOrReplaceChild("drill", CubeListBuilder.create().texOffs(30, 41).mirror().addBox(0.0F, -8.0F, 0.0F, 8.0F, 6.0F, 6.0F), PartPose.offset(DRILL_X, 5.0F, -3.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ExtruderRenderer.ExtruderRenderState state) {
        super.setupAnim(state);
        this.drill.x = DRILL_X + state.drillWave * 2.5F;
    }
}
