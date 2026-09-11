package com.grim3212.assorted.tech.client.data;

import com.mojang.math.Quadrant;
import com.grim3212.assorted.lib.client.data.SpecificationBlockStateModelBuilder;
import com.grim3212.assorted.tech.Constants;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.client.color.BridgeItemTintSource;
import com.grim3212.assorted.tech.client.model.BridgeItemModel;
import com.grim3212.assorted.tech.common.block.AlarmBlock;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.FanBlock;
import com.grim3212.assorted.tech.common.block.FlipFlopTorchBlock;
import com.grim3212.assorted.tech.common.block.GravityBlock;
import com.grim3212.assorted.tech.common.block.GravityDirectionalBlock;
import com.grim3212.assorted.tech.common.block.SensorBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.List;
import java.util.stream.Stream;

/**
 * Block states and block models. This owns every block and every block item
 * {@link TechItemModelProvider} does not claim, so the two never write the same file.
 */
public class TechBlockstateProvider extends ModelProvider {

    /**
     * The slot the bridge's shape reads its face texture from. {@link TextureSlot} has no
     * {@code equals}, so it has to be created exactly once and shared.
     */
    private static final TextureSlot STORED = TextureSlot.create("stored");

    private static final Identifier MC_BLOCK = Identifier.withDefaultNamespace("block/block");
    private static final Identifier TINTED_CUBE = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block/tinted_cube");

    /**
     * The shape every bridge inherits: a full cube whose faces all read {@code #stored} and all carry
     * tint index 0, which is where the bridge's {@code BlockTintSource} colours it.
     */
    private static final ModelTemplate TINTED_CUBE_TEMPLATE = defaultPerspective(ExtendedModelTemplateBuilder.builder()
            .parent(MC_BLOCK)
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .requiredTextureSlot(STORED)
            .element(e -> e.from(0, 0, 0).to(16, 16, 16).allFaces((dir, face) -> face.texture(STORED).cullface(dir).tintindex(0))))
            .build();

    public TechBlockstateProvider(PackOutput output) {
        super(output, Constants.MOD_ID);
    }

    @Override
    public String getName() {
        return "Assorted Tech block states";
    }

    /**
     * Block items this provider models: all but the two torches, which {@link
     * TechItemModelProvider} draws as flat sprites.
     */
    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return super.getKnownItems().filter(holder -> holder.value() instanceof BlockItem && !TechItemModelProvider.owns(holder.value()));
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        TINTED_CUBE_TEMPLATE.create(TINTED_CUBE, new TextureMapping()
                .put(TextureSlot.PARTICLE, texture("block/bridge"))
                .put(STORED, texture("block/bridge")), blockModels.modelOutput);

        genericGravity(blockModels, TechBlocks.ATTRACTOR.get());
        genericGravity(blockModels, TechBlocks.REPULSOR.get());
        genericGravity(blockModels, TechBlocks.GRAVITOR.get());

        gravityDirectional(blockModels, TechBlocks.ATTRACTOR_DIRECTIONAL.get(), texture("block/attractor_on"), texture("block/attractor_off"));
        gravityDirectional(blockModels, TechBlocks.REPULSOR_DIRECTIONAL.get(), texture("block/repulsor_on"), texture("block/repulsor_off"));
        gravityDirectional(blockModels, TechBlocks.GRAVITOR_DIRECTIONAL.get(), texture("block/gravitor_on"), texture("block/gravitor_off"));

        torch(blockModels, TechBlocks.FLIP_FLOP_TORCH.get(), TechBlocks.FLIP_FLOP_WALL_TORCH.get());
        torch(blockModels, TechBlocks.GLOWSTONE_TORCH.get(), TechBlocks.GLOWSTONE_WALL_TORCH.get());

        TechBlocks.SENSORS.forEach(sensor -> sensor(blockModels, sensor.get()));
        TechBlocks.SPIKES.forEach(spike -> spike(blockModels, spike.get()));

        fan(blockModels);
        alarm(blockModels);

        bridge(blockModels);
        bridgeControl(blockModels, TechBlocks.BRIDGE_CONTROL_ACCEL.get());
        bridgeControl(blockModels, TechBlocks.BRIDGE_CONTROL_LASER.get());
        bridgeControl(blockModels, TechBlocks.BRIDGE_CONTROL_GRAVITY.get());
        bridgeControl(blockModels, TechBlocks.BRIDGE_CONTROL_TRICK.get());
        bridgeControl(blockModels, TechBlocks.BRIDGE_CONTROL_DEATH.get());
    }

    // ------------------------------------------------------------------ bridge

    /**
     * One bridge loader model per fallback texture, dispatched on {@link BridgeBlock#TYPE}: {@code
     * block/bridge_gravity} for {@link BridgeType#GRAVITY}, {@code block/bridge} for the rest.
     * {@code collectParts} never sees the block state, so the choice is made in the blockstate
     * json.
     */
    private void bridge(BlockModelGenerators blockModels) {
        Block b = TechBlocks.BRIDGE.get();
        Identifier plain = bridgeModel(blockModels, "block/" + name(b), texture("block/bridge"));
        Identifier gravity = bridgeModel(blockModels, "block/" + name(b) + "_gravity", texture("block/bridge_gravity"));

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b)
                .with(PropertyDispatch.initial(BridgeBlock.TYPE)
                        .generate(type -> bridgeVariant(type == BridgeType.GRAVITY ? gravity : plain))));

        // The item draws its stored block through BridgeItemModel, or the plain model when empty.
        // The tint source sits at index 0, the tint index tinted_cube puts on every face.
        blockModels.itemModelOutput.accept(b.asItem(), new BridgeItemModel.Unbaked(plain, List.of(new BridgeItemTintSource())));
    }

    /**
     * A bridge's {@link MultiVariant}, through {@link SpecificationBlockStateModelBuilder} so the
     * bridge's own model reaches the blockstate layer, the only one that sees the level and
     * position. A plain variant is baked once with no block entity, and every bridge draws its
     * fallback.
     */
    private static MultiVariant bridgeVariant(Identifier model) {
        return SpecificationBlockStateModelBuilder.specificationVariant(model);
    }

    private Identifier bridgeModel(BlockModelGenerators blockModels, String path, Material stored) {
        ModelTemplate template = defaultPerspective(ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .customLoader(BridgeModelBuilder::begin, loader -> loader
                        .bridge(TINTED_CUBE)
                        .addTexture("stored", stored.sprite())))
                .build();

        return template.create(resource(path), new TextureMapping().put(TextureSlot.PARTICLE, stored), blockModels.modelOutput);
    }

    /**
     * The block-sized item perspectives the 1.20.1 provider stamped on the bridge models and on
     * {@code tinted_cube}. They were an inline {@code transforms()} block on the old builder and are
     * template transforms now; the values are copied across unchanged.
     */
    private static ExtendedModelTemplateBuilder defaultPerspective(ExtendedModelTemplateBuilder builder) {
        return builder
                .transform(ItemDisplayContext.GUI, t -> t.rotation(30.0F, 225.0F, 0.0F).translation(0.0F, 0.0F, 0.0F).scale(0.625F))
                .transform(ItemDisplayContext.GROUND, t -> t.rotation(0.0F, 0.0F, 0.0F).translation(0.0F, 3.0F, 0.0F).scale(0.25F))
                .transform(ItemDisplayContext.FIXED, t -> t.rotation(0.0F, 0.0F, 0.0F).translation(0.0F, 0.0F, 0.0F).scale(0.5F))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(75.0F, 45.0F, 0.0F).translation(0.0F, 2.5F, 0.0F).scale(0.375F))
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, t -> t.rotation(0.0F, 45.0F, 0.0F).translation(0.0F, 0.0F, 0.0F).scale(0.4F))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, t -> t.rotation(0.0F, 225.0F, 0.0F).translation(0.0F, 0.0F, 0.0F).scale(0.4F));
    }

    private void bridgeControl(BlockModelGenerators blockModels, Block b) {
        String name = name(b);
        Material front = texture("block/" + name);
        Material side = texture("block/bridge_control_side");
        Material top = texture("block/bridge_control_top");

        Identifier inventoryModel = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(resource("block/" + name), new TextureMapping()
                .put(TextureSlot.TOP, top).put(TextureSlot.BOTTOM, top).put(TextureSlot.SIDE, side).put(TextureSlot.FRONT, front), blockModels.modelOutput);
        Identifier placedModel = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(resource("block/" + name + "_vertical"), new TextureMapping()
                .put(TextureSlot.TOP, front).put(TextureSlot.BOTTOM, top).put(TextureSlot.SIDE, side).put(TextureSlot.FRONT, side), blockModels.modelOutput);

        // Every placed state used the "_vertical" model in 1.20.1, including the horizontal ones; the
        // other model exists only for the item. Preserved verbatim.
        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b, BlockModelGenerators.plainVariant(placedModel))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));

        blockModels.registerSimpleItemModel(b, inventoryModel);
    }

    // ------------------------------------------------------------------ gravity

    private void genericGravity(BlockModelGenerators blockModels, Block b) {
        String name = name(b);
        Identifier on = ModelTemplates.CUBE_ALL.create(resource("block/" + name), TextureMapping.cube(texture("block/" + name + "_on")), blockModels.modelOutput);
        Identifier off = ModelTemplates.CUBE_ALL.create(resource("block/" + name + "_off"), TextureMapping.cube(texture("block/" + name + "_off")), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b)
                .with(BlockModelGenerators.createBooleanModelDispatch(GravityBlock.POWERED, BlockModelGenerators.plainVariant(on), BlockModelGenerators.plainVariant(off))));

        blockModels.registerSimpleItemModel(b, on);
    }

    private void gravityDirectional(BlockModelGenerators blockModels, GravityDirectionalBlock b, Material on, Material off) {
        String name = name(b);
        Material side = texture("block/gravity_side");

        Identifier onModel = orientable(blockModels, "block/" + name + "_on", on, side);
        Identifier offModel = orientable(blockModels, "block/" + name + "_off", off, side);
        Identifier onVertical = orientableVertical(blockModels, "block/" + name + "_on_vertical", on, side);
        Identifier offVertical = orientableVertical(blockModels, "block/" + name + "_off_vertical", off, side);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b)
                .with(PropertyDispatch.initial(GravityDirectionalBlock.FACING, GravityDirectionalBlock.POWERED)
                        .generate((dir, powered) -> orientVertical(powered ? onModel : offModel, powered ? onVertical : offVertical, dir))));

        blockModels.registerSimpleItemModel(b, onModel);
    }

    // ------------------------------------------------------------------ fan, alarm

    private void fan(BlockModelGenerators blockModels) {
        Block b = TechBlocks.FAN.get();
        String name = name(b);
        Material noteBlock = new Material(Identifier.withDefaultNamespace("block/note_block"));

        Identifier blow = orientable(blockModels, "block/" + name + "_blow", texture("block/" + name), noteBlock);
        Identifier suck = orientable(blockModels, "block/" + name + "_suck", texture("block/" + name + "_suck"), noteBlock);
        Identifier stopped = orientable(blockModels, "block/" + name + "_stopped", texture("block/" + name + "_stopped"), noteBlock);

        Identifier blowVertical = orientableVertical(blockModels, "block/" + name + "_blow_vertical", texture("block/" + name), noteBlock);
        Identifier suckVertical = orientableVertical(blockModels, "block/" + name + "_suck_vertical", texture("block/" + name + "_suck"), noteBlock);
        Identifier stoppedVertical = orientableVertical(blockModels, "block/" + name + "_stopped_vertical", texture("block/" + name + "_stopped"), noteBlock);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b)
                .with(PropertyDispatch.initial(FanBlock.FACING, FanBlock.MODE).generate((dir, mode) -> switch (mode) {
                    case BLOW -> orientVertical(blow, blowVertical, dir);
                    case SUCK -> orientVertical(suck, suckVertical, dir);
                    case OFF -> orientVertical(stopped, stoppedVertical, dir);
                })));

        blockModels.registerSimpleItemModel(b, stopped);
    }

    /**
     * The alarm box: two hand built element models, a floor one and a wall one, chosen by whether the
     * block faces vertically. {@code WATERLOGGED} and {@code POWERED} are not dispatched on, exactly
     * as the 1.20.1 {@code forAllStatesExcept} did.
     */
    private void alarm(BlockModelGenerators blockModels) {
        Block b = TechBlocks.ALARM.get();
        Material side = texture("block/alarm_side");
        Material top = texture("block/alarm_top");

        ModelTemplate floorTemplate = ExtendedModelTemplateBuilder.builder()
                .parent(MC_BLOCK)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(TextureSlot.SIDE)
                .requiredTextureSlot(TextureSlot.TOP)
                .element(e -> e.from(3, 0, 3).to(13, 4, 13).allFaces((dir, face) -> {
                    switch (dir) {
                        case EAST, NORTH, SOUTH, WEST -> face.texture(TextureSlot.SIDE).uvs(2, 11, 14, 16);
                        case DOWN -> face.texture(TextureSlot.TOP).uvs(3, 3, 13, 13).cullface(Direction.DOWN);
                        case UP -> face.texture(TextureSlot.TOP).uvs(3, 3, 13, 13);
                    }
                }))
                .build();

        ModelTemplate wallTemplate = ExtendedModelTemplateBuilder.builder()
                .parent(MC_BLOCK)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(TextureSlot.SIDE)
                .requiredTextureSlot(TextureSlot.TOP)
                .element(e -> e.from(3, 3, 12).to(13, 13, 16).allFaces((dir, face) -> {
                    switch (dir) {
                        case EAST -> face.texture(TextureSlot.SIDE).uvs(2, 11, 14, 16).rotation(Quadrant.R90);
                        case NORTH -> face.texture(TextureSlot.TOP).uvs(3, 3, 13, 13);
                        case SOUTH -> face.texture(TextureSlot.TOP).uvs(3, 3, 13, 13).cullface(Direction.SOUTH);
                        case WEST -> face.texture(TextureSlot.SIDE).uvs(2, 11, 14, 16).rotation(Quadrant.R270);
                        case DOWN -> face.texture(TextureSlot.SIDE).uvs(2, 11, 14, 16).rotation(Quadrant.R180);
                        case UP -> face.texture(TextureSlot.SIDE).uvs(2, 11, 14, 16);
                    }
                }))
                .build();

        TextureMapping textures = new TextureMapping().put(TextureSlot.PARTICLE, side).put(TextureSlot.SIDE, side).put(TextureSlot.TOP, top);
        Identifier floorModel = floorTemplate.create(resource("block/alarm"), textures, blockModels.modelOutput);
        Identifier wallModel = wallTemplate.create(resource("block/alarm_wall"), textures, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b)
                .with(PropertyDispatch.initial(AlarmBlock.FACING).generate(dir -> orientVertical(wallModel, floorModel, dir))));

        blockModels.registerSimpleItemModel(b, wallModel);
    }

    // ------------------------------------------------------------------ sensors, spikes, torches

    private void sensor(BlockModelGenerators blockModels, Block b) {
        String name = name(b);
        Material side = texture("block/sensors/" + name + "_side");

        Identifier undetected = orientableVertical(blockModels, "block/sensors/" + name, texture("block/sensors/" + name + "_off"), side);
        Identifier detected = orientableVertical(blockModels, "block/sensors/" + name + "_detected", texture("block/sensors/" + name + "_on"), side);
        Identifier inventory = orientable(blockModels, "block/sensors/" + name + "_inventory", texture("block/sensors/" + name + "_off"), side);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b)
                .with(BlockModelGenerators.createBooleanModelDispatch(SensorBlock.DETECTED, BlockModelGenerators.plainVariant(detected), BlockModelGenerators.plainVariant(undetected)))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));

        blockModels.registerSimpleItemModel(b, inventory);
    }

    private void spike(BlockModelGenerators blockModels, Block b) {
        String name = name(b);
        Material unpowered = texture("block/spikes/" + name);
        Material powered = texture("block/spikes/" + name + "_powered");

        Identifier unpoweredModel = ModelTemplates.CROSS.create(resource("block/spikes/" + name), TextureMapping.cross(unpowered), blockModels.modelOutput);
        Identifier poweredModel = ModelTemplates.CROSS.create(resource("block/spikes/" + name + "_powered"), TextureMapping.cross(powered), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(b)
                .with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.POWERED, BlockModelGenerators.plainVariant(poweredModel), BlockModelGenerators.plainVariant(unpoweredModel)))
                .with(BlockModelGenerators.ROTATIONS_COLUMN_WITH_FACING));

        // The spike item is a flat sprite of the powered texture, not the block model.
        Identifier itemModel = ModelTemplates.FLAT_ITEM.create(resource("item/" + name), TextureMapping.layer0(powered), blockModels.modelOutput);
        blockModels.registerSimpleItemModel(b, itemModel);
    }

    /**
     * A torch pair. The wall torch's {@code rotationY((toYRot() + 90) % 360)} is
     * {@link BlockModelGenerators#ROTATION_TORCH}, which is how vanilla's own wall torches are
     * oriented. {@code PREV_LIT} on the flip flop torch is not dispatched on, as before.
     */
    private void torch(BlockModelGenerators blockModels, Block torch, Block wallTorch) {
        String name = name(torch);
        String wallName = name(wallTorch);

        Identifier lit = ModelTemplates.TORCH.create(resource("block/" + name), torchMapping(texture("block/" + name)), blockModels.modelOutput);
        Identifier unlit = ModelTemplates.TORCH.create(resource("block/" + name + "_off"), torchMapping(texture("block/" + name + "_off")), blockModels.modelOutput);
        Identifier litWall = ModelTemplates.WALL_TORCH.create(resource("block/" + wallName), torchMapping(texture("block/" + name)), blockModels.modelOutput);
        Identifier unlitWall = ModelTemplates.WALL_TORCH.create(resource("block/" + wallName + "_off"), torchMapping(texture("block/" + name + "_off")), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(torch)
                .with(BlockModelGenerators.createBooleanModelDispatch(FlipFlopTorchBlock.LIT, BlockModelGenerators.plainVariant(lit), BlockModelGenerators.plainVariant(unlit))));

        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(wallTorch)
                .with(BlockModelGenerators.createBooleanModelDispatch(FlipFlopTorchBlock.LIT, BlockModelGenerators.plainVariant(litWall), BlockModelGenerators.plainVariant(unlitWall)))
                .with(BlockModelGenerators.ROTATION_TORCH));
    }

    // ------------------------------------------------------------------ helpers

    private static TextureMapping torchMapping(Material torch) {
        return new TextureMapping().put(TextureSlot.TORCH, torch);
    }

    private Identifier orientable(BlockModelGenerators blockModels, String path, Material front, Material side) {
        return ModelTemplates.CUBE_ORIENTABLE.create(resource(path), new TextureMapping()
                .put(TextureSlot.FRONT, front).put(TextureSlot.SIDE, side).put(TextureSlot.TOP, side), blockModels.modelOutput);
    }

    /**
     * {@code orientable_vertical} takes only {@code front} and {@code side}. The 1.20.1 builder also
     * wrote a {@code top} entry into these models; the template does not, because nothing reads it.
     */
    private Identifier orientableVertical(BlockModelGenerators blockModels, String path, Material front, Material side) {
        return ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(resource(path), new TextureMapping()
                .put(TextureSlot.FRONT, front).put(TextureSlot.SIDE, side), blockModels.modelOutput);
    }

    /**
     * The rotation the gravity emitters, the fan and the alarm shared: a vertical facing draws the
     * {@code _vertical} model (flipped for {@code DOWN}) and a horizontal facing draws the plain model
     * turned to face the player, which is {@link BlockModelGenerators#ROTATION_HORIZONTAL_FACING}.
     */
    private static MultiVariant orientVertical(Identifier horizontal, Identifier vertical, Direction dir) {
        if (dir == Direction.DOWN) {
            return BlockModelGenerators.plainVariant(vertical).with(BlockModelGenerators.X_ROT_180);
        }

        if (dir == Direction.UP) {
            return BlockModelGenerators.plainVariant(vertical);
        }

        return BlockModelGenerators.plainVariant(horizontal).with(horizontalRotation(dir));
    }

    private static VariantMutator horizontalRotation(Direction dir) {
        return switch (dir) {
            case EAST -> BlockModelGenerators.Y_ROT_90;
            case SOUTH -> BlockModelGenerators.Y_ROT_180;
            case WEST -> BlockModelGenerators.Y_ROT_270;
            default -> BlockModelGenerators.NOP;
        };
    }

    private static String name(Block b) {
        return BuiltInRegistries.BLOCK.getKey(b).getPath();
    }

    private static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }

    private static Material texture(String path) {
        return new Material(resource(path));
    }
}
