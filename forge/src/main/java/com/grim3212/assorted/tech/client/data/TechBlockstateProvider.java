package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.api.util.FanMode;
import com.grim3212.assorted.tech.common.block.*;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.ModelBuilder.FaceRotation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public class TechBlockstateProvider extends BlockStateProvider {

    private final BridgeModelProvider loaderModels;
    private static final ResourceLocation CUTOUT_RENDER_TYPE = new ResourceLocation("minecraft:cutout");

    public TechBlockstateProvider(PackOutput output, ExistingFileHelper exFileHelper, BridgeModelProvider loader) {
        super(output, AssortedTech.MODID, exFileHelper);
        this.loaderModels = loader;
    }

    @Override
    public String getName() {
        return "Assorted Tech block states";
    }

    @Override
    protected void registerStatesAndModels() {
        this.extraModels();

        genericGravity(TechBlocks.ATTRACTOR.get());
        genericGravity(TechBlocks.REPULSOR.get());
        genericGravity(TechBlocks.GRAVITOR.get());

        gravityDirectionalModel(TechBlocks.ATTRACTOR_DIRECTIONAL.get(), resource("block/attractor_on"), resource("block/attractor_off"));
        gravityDirectionalModel(TechBlocks.REPULSOR_DIRECTIONAL.get(), resource("block/repulsor_on"), resource("block/repulsor_off"));
        gravityDirectionalModel(TechBlocks.GRAVITOR_DIRECTIONAL.get(), resource("block/gravitor_on"), resource("block/gravitor_off"));

        this.torchModel(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get(), FlipFlopTorchBlock.PREV_LIT);
        this.torchModel(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get());

        TechBlocks.SENSORS.forEach((sensor) -> this.sensorModel(sensor.get()));
        TechBlocks.SPIKES.forEach((spike) -> this.spikeModel(spike.get()));

        this.fanModel();

        this.alarmBoxModel();

        bridge(TechBlocks.BRIDGE.get(), new ResourceLocation(AssortedTech.MODID, "block/tinted_cube"));
        bridgeControlModel(TechBlocks.BRIDGE_CONTROL_ACCEL.get());
        bridgeControlModel(TechBlocks.BRIDGE_CONTROL_LASER.get());
        bridgeControlModel(TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
        bridgeControlModel(TechBlocks.BRIDGE_CONTROL_TRICK.get());
        bridgeControlModel(TechBlocks.BRIDGE_CONTROL_DEATH.get());

        this.loaderModels.previousModels();
    }

    private void bridge(Block b, ResourceLocation model) {
        String name = name(b);

        BridgeModelBuilder bridgeParent = this.loaderModels.getBuilder(name).loader(new ResourceLocation(AssortedTech.MODID, "bridge")).bridge(model).texture("particle", new ResourceLocation(AssortedTech.MODID, "block/bridge")).addTexture("stored", new ResourceLocation(AssortedTech.MODID, "block/bridge_gravity"));

        ConfiguredModel bridgeModel = new ConfiguredModel(bridgeParent);
        customLoaderState(b, bridgeModel);

        itemModels().getBuilder(name).parent(bridgeModel.model);
    }

    private void genericGravity(Block b) {
        String name = name(b);
        ModelFile onModel = models().cubeAll(name, resource("block/" + name + "_on"));
        ModelFile offModel = models().cubeAll(name + "_off", resource("block/" + name + "_off"));
        getVariantBuilder(b).forAllStates(state -> {
            return ConfiguredModel.builder().modelFile(state.getValue(GravityBlock.POWERED) ? onModel : offModel).build();
        });

        itemModels().withExistingParent(name, prefix("block/" + name));
    }

    private void customLoaderState(Block block, ConfiguredModel model) {
        getVariantBuilder(block).partialState().setModels(model);
    }

    private void extraModels() {
        BlockModelBuilder model = this.models().getBuilder(prefix("block/tinted_cube")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/block"))).texture("particle", new ResourceLocation(AssortedTech.MODID, "block/bridge")).texture("stored", new ResourceLocation(AssortedTech.MODID, "block/bridge"));
        model.element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> {
            face.texture("#stored").cullface(dir).tintindex(0);
        });
        defaultPerspective(model);

        BlockModelBuilder color_cube = this.models().getBuilder(prefix("block/color_cube")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/block")));
        color_cube.element().from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> {
            face.texture("#" + dir.toString()).cullface(dir).tintindex(0);
        });

        BlockModelBuilder color_cube_all = this.models().getBuilder(prefix("block/color_cube_all")).parent(this.models().getExistingFile(resource("block/color_cube")));
        color_cube_all.texture("particle", "#all").texture("down", "#all").texture("up", "#all").texture("north", "#all").texture("south", "#all").texture("west", "#all").texture("east", "#all");

        BlockModelBuilder color_cube_bottom_top = this.models().getBuilder(prefix("block/color_cube_bottom_top")).parent(this.models().getExistingFile(resource("block/color_cube")));
        color_cube_bottom_top.texture("particle", "#side").texture("down", "#bottom").texture("up", "#top").texture("north", "#side").texture("south", "#side").texture("west", "#side").texture("east", "#side");
    }

    private void defaultPerspective(ModelBuilder<?> model) {
        model.transforms().transform(TransformType.GUI).rotation(30, 225, 0).translation(0, 0, 0).scale(0.625f).end().transform(TransformType.GROUND).rotation(0, 0, 0).translation(0, 3, 0).scale(0.25f).end().transform(TransformType.FIXED).rotation(0, 0, 0).translation(0, 0, 0).scale(0.5f).end().transform(TransformType.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f).end().transform(TransformType.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).translation(0, 0, 0)
                .scale(0.40f).end().transform(TransformType.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).translation(0, 0, 0).scale(0.40f).end();
    }

    private void bridgeControlModel(Block b) {
        String bridgeName = name(b);
        ResourceLocation bridgeLocation = resource("block/" + bridgeName);
        ModelFile regularModel = models().orientableWithBottom(bridgeLocation.toString(), resource("block/bridge_control_side"), bridgeLocation, resource("block/bridge_control_top"), resource("block/bridge_control_top"));
        ModelFile verticalModel = models().orientableWithBottom(bridgeLocation.toString() + "_vertical", resource("block/bridge_control_side"), resource("block/bridge_control_side"), resource("block/bridge_control_top"), bridgeLocation);

        getVariantBuilder(b).forAllStatesExcept(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder().modelFile(verticalModel).rotationX(dir == Direction.DOWN ? 180 : dir == Direction.UP ? 0 : 90).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
        }, BridgeControlBlock.POWERED);

        itemModels().getBuilder(bridgeName).parent(regularModel);
    }

    private void gravityDirectionalModel(GravityDirectionalBlock b, ResourceLocation on, ResourceLocation off) {
        String name = name(b);
        String location = prefix("block/" + name);
        ModelFile offModel = models().getBuilder(location + "_off").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable"))).texture("front", off).texture("top", resource("block/gravity_side")).texture("side", resource("block/gravity_side"));
        ModelFile onModel = models().getBuilder(location + "_on").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable"))).texture("front", on).texture("top", resource("block/gravity_side")).texture("side", resource("block/gravity_side"));

        ModelFile offModelVertical = models().getBuilder(location + "_off_vertical").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable_vertical"))).texture("front", off).texture("top", resource("block/gravity_side")).texture("side", resource("block/gravity_side"));
        ModelFile onModelVertical = models().getBuilder(location + "_on_vertical").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable_vertical"))).texture("front", on).texture("top", resource("block/gravity_side")).texture("side", resource("block/gravity_side"));

        getVariantBuilder(b).forAllStates(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            boolean powered = state.getValue(GravityDirectionalBlock.POWERED);
            ModelFile model = null;
            if (powered) {
                model = dir.getAxis().isVertical() ? onModelVertical : onModel;
            } else {
                model = dir.getAxis().isVertical() ? offModelVertical : offModel;
            }

            return ConfiguredModel.builder().modelFile(model).rotationX(dir == Direction.DOWN ? 180 : 0).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
        });

        itemModels().getBuilder(prefix("item/" + name)).parent(onModel);
    }

    private void fanModel() {
        String fanName = name(TechBlocks.FAN.get());
        String fanLoc = prefix("block/" + fanName);
        ModelFile suckModel = models().getBuilder(fanLoc + "_suck").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable"))).texture("front", fanLoc + "_suck").texture("top", mcLoc("block/note_block")).texture("side", mcLoc("block/note_block"));
        ModelFile blowModel = models().getBuilder(fanLoc + "_blow").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable"))).texture("front", fanLoc).texture("top", mcLoc("block/note_block")).texture("side", mcLoc("block/note_block"));
        ModelFile offModel = models().getBuilder(fanLoc + "_stopped").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable"))).texture("front", fanLoc + "_stopped").texture("top", mcLoc("block/note_block")).texture("side", mcLoc("block/note_block"));

        ModelFile suckModelVertical = models().getBuilder(fanLoc + "_suck_vertical").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable_vertical"))).texture("front", fanLoc + "_suck").texture("top", mcLoc("block/note_block")).texture("side", mcLoc("block/note_block"));
        ModelFile blowModelVertical = models().getBuilder(fanLoc + "_blow_vertical").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable_vertical"))).texture("front", fanLoc).texture("top", mcLoc("block/note_block")).texture("side", mcLoc("block/note_block"));
        ModelFile offModelVertical = models().getBuilder(fanLoc + "_stopped_vertical").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable_vertical"))).texture("front", fanLoc + "_stopped").texture("top", mcLoc("block/note_block")).texture("side", mcLoc("block/note_block"));

        getVariantBuilder(TechBlocks.FAN.get()).forAllStates(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            FanMode mode = state.getValue(FanBlock.MODE);
            ModelFile model = null;
            switch (mode) {
                case BLOW:
                    model = dir.getAxis().isVertical() ? blowModelVertical : blowModel;
                    break;
                case OFF:
                    model = dir.getAxis().isVertical() ? offModelVertical : offModel;
                    break;
                case SUCK:
                    model = dir.getAxis().isVertical() ? suckModelVertical : suckModel;
                    break;
            }

            return ConfiguredModel.builder().modelFile(model).rotationX(dir == Direction.DOWN ? 180 : 0).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
        });

        itemModels().getBuilder(prefix("item/" + fanName)).parent(offModel);
    }

    private void alarmBoxModel() {
        BlockModelBuilder alarmModel = this.models().getBuilder(prefix("block/alarm")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/block"))).texture("particle", prefix("block/alarm_side")).texture("side", prefix("block/alarm_side")).texture("top", prefix("block/alarm_top")).renderType(CUTOUT_RENDER_TYPE);
        alarmModel.element().from(3, 0, 3).to(13, 4, 13).allFaces((dir, face) -> {
            switch (dir) {
                case EAST:
                case NORTH:
                case SOUTH:
                case WEST:
                    face.texture("#side").uvs(2, 11, 14, 16);
                    break;
                case DOWN:
                    face.texture("#top").uvs(3, 3, 13, 13).cullface(Direction.DOWN);
                    break;
                case UP:
                default:
                    face.texture("#top").uvs(3, 3, 13, 13);
                    break;
            }
        });

        BlockModelBuilder alarmWallModel = this.models().getBuilder(prefix("block/alarm_wall")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/block"))).texture("particle", prefix("block/alarm_side")).texture("side", prefix("block/alarm_side")).texture("top", prefix("block/alarm_top")).renderType(CUTOUT_RENDER_TYPE);
        alarmWallModel.element().from(3, 3, 12).to(13, 13, 16).allFaces((dir, face) -> {
            switch (dir) {
                case EAST:
                    face.texture("#side").uvs(2, 11, 14, 16).rotation(FaceRotation.CLOCKWISE_90);
                    break;
                case NORTH:
                    face.texture("#top").uvs(3, 3, 13, 13);
                    break;
                case SOUTH:
                    face.texture("#top").uvs(3, 3, 13, 13).cullface(Direction.SOUTH);
                    break;
                case WEST:
                    face.texture("#side").uvs(2, 11, 14, 16).rotation(FaceRotation.COUNTERCLOCKWISE_90);
                    break;
                case DOWN:
                    face.texture("#side").uvs(2, 11, 14, 16).rotation(FaceRotation.UPSIDE_DOWN);
                    break;
                case UP:
                default:
                    face.texture("#side").uvs(2, 11, 14, 16);
                    break;
            }
        });

        Function<BlockState, ModelFile> modelFunc = (state) -> {
            return state.getValue(AlarmBlock.FACING).getAxis().isVertical() ? alarmModel : alarmWallModel;
        };

        getVariantBuilder(TechBlocks.ALARM.get()).forAllStatesExcept(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder().modelFile(modelFunc.apply(state)).rotationX(dir == Direction.DOWN ? 180 : 0).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
        }, BlockStateProperties.WATERLOGGED, AlarmBlock.POWERED);

        itemModels().getBuilder(name(TechBlocks.ALARM.get())).parent(alarmWallModel).renderType(CUTOUT_RENDER_TYPE);
    }

    private void sensorModel(Block b) {
        String sensorName = name(b);
        String sensorLoc = prefix("block/sensors/" + sensorName);
        ModelFile undetectedModel = models().getBuilder(sensorLoc).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable_vertical"))).texture("side", sensorLoc + "_side").texture("front", sensorLoc + "_off");
        ModelFile detectedModel = models().getBuilder(sensorLoc + "_detected").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable_vertical"))).texture("side", sensorLoc + "_side").texture("front", sensorLoc + "_on");
        ModelFile inventoryModel = models().getBuilder(sensorLoc + "_inventory").parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/orientable"))).texture("side", sensorLoc + "_side").texture("front", sensorLoc + "_off").texture("top", sensorLoc + "_side");

        getVariantBuilder(b).forAllStates(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder().modelFile(state.getValue(SensorBlock.DETECTED) ? detectedModel : undetectedModel).rotationX(dir == Direction.DOWN ? 180 : dir == Direction.UP ? 0 : 90).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
        });

        itemModels().getBuilder(prefix("item/" + sensorName)).parent(inventoryModel);
    }

    private void spikeModel(Block b) {
        String spikeName = name(b);
        String spikeUnpowered = prefix("block/spikes/" + spikeName);
        String spikePowered = prefix("block/spikes/" + spikeName + "_powered");

        ModelFile spikeModel = models().getBuilder(spikeUnpowered).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/cross"))).texture("cross", spikeUnpowered).renderType(CUTOUT_RENDER_TYPE);
        ModelFile spikePoweredModel = models().getBuilder(spikePowered).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/cross"))).texture("cross", spikePowered).renderType(CUTOUT_RENDER_TYPE);

        getVariantBuilder(b).forAllStatesExcept(state -> {
            Direction dir = state.getValue(BlockStateProperties.FACING);
            return ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.POWERED) ? spikePoweredModel : spikeModel).rotationX(dir == Direction.DOWN ? 180 : dir == Direction.UP ? 0 : 90).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
        }, BlockStateProperties.WATERLOGGED);

        itemModels().withExistingParent(spikeName, "item/generated").texture("layer0", spikePowered).renderType(CUTOUT_RENDER_TYPE);
    }

    private void torchModel(Block torch, Block wallTorch, Property<?>... ignored) {
        String torchName = name(torch);
        String wallTorchName = name(wallTorch);

        ModelFile litTorchModel = models().getBuilder(prefix("block/" + torchName)).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch"))).texture("torch", prefix("block/" + torchName)).renderType(CUTOUT_RENDER_TYPE);
        ModelFile unlitTorchModel = models().getBuilder(prefix("block/" + torchName + "_off")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch"))).texture("torch", prefix("block/" + torchName + "_off")).renderType(CUTOUT_RENDER_TYPE);
        getVariantBuilder(torch).forAllStatesExcept(state -> {
            return ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.LIT) ? litTorchModel : unlitTorchModel).build();
        }, ignored);

        ModelFile litWallTorchModel = models().getBuilder(prefix("block/" + wallTorchName)).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch_wall"))).texture("torch", prefix("block/" + torchName)).renderType(CUTOUT_RENDER_TYPE);
        ModelFile unlitWallTorchModel = models().getBuilder(prefix("block/" + wallTorchName + "_off")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch_wall"))).texture("torch", prefix("block/" + torchName + "_off")).renderType(CUTOUT_RENDER_TYPE);
        getVariantBuilder(wallTorch).forAllStatesExcept(state -> {
            return ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.LIT) ? litWallTorchModel : unlitWallTorchModel).rotationY((((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()) + 90) % 360).build();
        }, ignored);
    }

    private String name(Block b) {
        return ForgeRegistries.BLOCKS.getKey(b).getPath();
    }

    private String prefix(String name) {
        return resource(name).toString();
    }

    private ResourceLocation resource(String name) {
        return new ResourceLocation(AssortedTech.MODID, name);
    }
}
