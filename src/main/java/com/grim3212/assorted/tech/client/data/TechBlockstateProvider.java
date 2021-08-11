package com.grim3212.assorted.tech.client.data;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopTorchBlock;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.util.FanMode;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class TechBlockstateProvider extends BlockStateProvider {

	public TechBlockstateProvider(DataGenerator generator, ExistingFileHelper exFileHelper) {
		super(generator, AssortedTech.MODID, exFileHelper);
	}

	@Override
	public String getName() {
		return "Assorted Tech block states";
	}

	@Override
	protected void registerStatesAndModels() {
		this.torchModel(TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get(), FlipFlopTorchBlock.PREV_LIT);
		this.torchModel(TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get());

		TechBlocks.SENSORS.forEach((sensor) -> this.sensorModel(sensor.get()));
		TechBlocks.SPIKES.forEach((spike) -> this.spikeModel(spike.get()));

		this.fanModel();
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
