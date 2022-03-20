package com.grim3212.assorted.tech.client.data;

import java.util.function.Function;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.client.model.BridgeModel;
import com.grim3212.assorted.tech.common.block.AlarmBlock;
import com.grim3212.assorted.tech.common.block.BridgeControlBlock;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopTorchBlock;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.util.FanMode;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder.FaceRotation;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class TechBlockstateProvider extends BlockStateProvider {

	private final BridgeModelProvider loaderModels;

	public TechBlockstateProvider(DataGenerator generator, ExistingFileHelper exFileHelper, BridgeModelProvider loader) {
		super(generator, AssortedTech.MODID, exFileHelper);
		this.loaderModels = loader;
	}

	@Override
	public String getName() {
		return "Assorted Tech block states";
	}

	@Override
	protected void registerStatesAndModels() {
		this.extraModels();

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

		BridgeModelBuilder bridgeParent = this.loaderModels.getBuilder(name).loader(BridgeModel.Loader.LOCATION).bridge(model).texture("particle", new ResourceLocation(AssortedTech.MODID, "block/bridge")).addTexture("stored", new ResourceLocation(AssortedTech.MODID, "block/bridge_gravity"));

		ConfiguredModel bridgeModel = new ConfiguredModel(bridgeParent);
		customLoaderState(b, bridgeModel);

		itemModels().getBuilder(name).parent(bridgeModel.model);
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
		model.transforms().transform(Perspective.GUI).rotation(30, 225, 0).translation(0, 0, 0).scale(0.625f).end().transform(Perspective.GROUND).rotation(0, 0, 0).translation(0, 3, 0).scale(0.25f).end().transform(Perspective.FIXED).rotation(0, 0, 0).translation(0, 0, 0).scale(0.5f).end().transform(Perspective.THIRDPERSON_RIGHT).rotation(75, 45, 0).translation(0, 2.5f, 0).scale(0.375f).end().transform(Perspective.FIRSTPERSON_RIGHT).rotation(0, 45, 0).translation(0, 0, 0).scale(0.40f).end()
				.transform(Perspective.FIRSTPERSON_LEFT).rotation(0, 225, 0).translation(0, 0, 0).scale(0.40f).end();
	}

	private void bridgeControlModel(Block b) {
		String bridgeName = name(b);
		ResourceLocation bridgeLocation = resource("block/" + bridgeName);
		ModelFile regularModel = models().orientableWithBottom(bridgeLocation.toString(), resource("block/bridge_control_side"), bridgeLocation, resource("block/bridge_control_top"), resource("block/bridge_control_top"));
		ModelFile verticalModel = models().orientableWithBottom(bridgeLocation.toString(), resource("block/bridge_control_side"), resource("block/bridge_control_side"), resource("block/bridge_control_top"), bridgeLocation);

		Function<BlockState, ModelFile> modelFunc = (state) -> {
			return state.getValue(BridgeControlBlock.FACING).getAxis().isVertical() ? regularModel : verticalModel;
		};

		getVariantBuilder(b).forAllStatesExcept(state -> {
			Direction dir = state.getValue(BlockStateProperties.FACING);
			return ConfiguredModel.builder().modelFile(modelFunc.apply(state)).rotationX(dir == Direction.DOWN ? 180 : dir == Direction.UP ? 0 : 90).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
		}, BridgeControlBlock.POWERED);

		itemModels().getBuilder(bridgeName).parent(regularModel);
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
		BlockModelBuilder alarmModel = this.models().getBuilder(prefix("block/alarm")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/block"))).texture("particle", prefix("block/alarm_side")).texture("side", prefix("block/alarm_side")).texture("top", prefix("block/alarm_top"));
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

		BlockModelBuilder alarmWallModel = this.models().getBuilder(prefix("block/alarm_wall")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/block"))).texture("particle", prefix("block/alarm_side")).texture("side", prefix("block/alarm_side")).texture("top", prefix("block/alarm_top"));
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

		itemModels().getBuilder(name(TechBlocks.ALARM.get())).parent(alarmWallModel);
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

		ModelFile spikeModel = models().getBuilder(spikeUnpowered).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/cross"))).texture("cross", spikeUnpowered);
		ModelFile spikePoweredModel = models().getBuilder(spikePowered).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/cross"))).texture("cross", spikePowered);

		getVariantBuilder(b).forAllStatesExcept(state -> {
			Direction dir = state.getValue(BlockStateProperties.FACING);
			return ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.POWERED) ? spikePoweredModel : spikeModel).rotationX(dir == Direction.DOWN ? 180 : dir == Direction.UP ? 0 : 90).rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360).build();
		}, BlockStateProperties.WATERLOGGED);

		itemModels().withExistingParent(spikeName, "item/generated").texture("layer0", spikePowered);
	}

	private void torchModel(Block torch, Block wallTorch, Property<?>... ignored) {
		String torchName = name(torch);
		String wallTorchName = name(wallTorch);

		ModelFile litTorchModel = models().getBuilder(prefix("block/" + torchName)).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch"))).texture("torch", prefix("block/" + torchName));
		ModelFile unlitTorchModel = models().getBuilder(prefix("block/" + torchName + "_off")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch"))).texture("torch", prefix("block/" + torchName + "_off"));
		getVariantBuilder(torch).forAllStatesExcept(state -> {
			return ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.LIT) ? litTorchModel : unlitTorchModel).build();
		}, ignored);

		ModelFile litWallTorchModel = models().getBuilder(prefix("block/" + wallTorchName)).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch_wall"))).texture("torch", prefix("block/" + torchName));
		ModelFile unlitWallTorchModel = models().getBuilder(prefix("block/" + wallTorchName + "_off")).parent(this.models().getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/template_torch_wall"))).texture("torch", prefix("block/" + torchName + "_off"));
		getVariantBuilder(wallTorch).forAllStatesExcept(state -> {
			return ConfiguredModel.builder().modelFile(state.getValue(BlockStateProperties.LIT) ? litWallTorchModel : unlitWallTorchModel).rotationY((((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()) + 90) % 360).build();
		}, ignored);
	}

	private String name(Block b) {
		return b.getRegistryName().getPath();
	}

	private String prefix(String name) {
		return resource(name).toString();
	}

	private ResourceLocation resource(String name) {
		return new ResourceLocation(AssortedTech.MODID, name);
	}
}
