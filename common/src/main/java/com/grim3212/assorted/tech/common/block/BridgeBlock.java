package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.lib.core.block.ExtraPropertyBlock;
import com.grim3212.assorted.lib.core.block.IBlockLightDampening;
import com.grim3212.assorted.lib.core.block.IBlockLightEmission;
import com.grim3212.assorted.lib.core.block.effects.*;
import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.tech.api.TechTags;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.api.util.TechDamageTypes;
import com.grim3212.assorted.tech.client.model.BridgeClientEffects;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * A bridge stands in for the block it stores: its light, shade, visual shape and effects come from
 * that block. Vanilla bakes light dampening into the block state, so the stored block's is carried
 * in {@link #LIGHT_DAMPENING}, set by the block entity when its block changes; vanilla then
 * relights, recomputes the sky column and sends the state to every client on its own. A bridge is
 * always a whole block, so every one takes it, as in 1.20.1.
 */
public class BridgeBlock extends ExtraPropertyBlock implements EntityBlock, IBlockLightDampening, IBlockLandingEffects, IBlockRunningEffects, IBlockEffectSupplier {

    public static EnumProperty<BridgeType> TYPE = EnumProperty.create("type", BridgeType.class);
    public static final IntegerProperty LIGHT_DAMPENING = IntegerProperty.create("light_dampening", 0, 15);

    /**
     * An empty bridge draws as a laser you can see through, so it passes light like glass: none
     * stopped, and the sky column carries on through it. Vanilla would bake 1 for a
     * {@code noOcclusion()} cube, which stopped the sky column at it.
     */
    public static final int EMPTY_DAMPENING = 0;

    public BridgeBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, BridgeType.LASER).setValue(LIGHT_DAMPENING, EMPTY_DAMPENING));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(TYPE, LIGHT_DAMPENING);
    }

    /** The state {@code bridge} should have while standing in for {@code stored}. */
    public static BlockState withStoredDampening(BlockState bridge, BlockState stored) {
        return bridge.setValue(LIGHT_DAMPENING, stored.isAir() ? EMPTY_DAMPENING : stored.getLightDampening());
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return state.getValue(LIGHT_DAMPENING);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getValue(LIGHT_DAMPENING) == 0;
    }

    /** The library's per-position answers; the state already carries the dampening. */
    @Override
    public int getLightDampening(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return state.getLightDampening();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        BlockState stored = this.getStoredState(blockGetter, pos);
        return stored.isAir() ? state.propagatesSkylightDown() : stored.propagatesSkylightDown();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return state.getValue(TYPE).isSolid() ? Shapes.block() : Shapes.empty();
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.getBlockState(pos).getValue(TYPE) == BridgeType.ACCEL && entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 10, 2));
        }
    }

    /** The effect applier and {@code isPrecise} are ignored, as vanilla's own hazard blocks do. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        BridgeType type = level.getBlockState(pos).getValue(TYPE);

        if (type == BridgeType.DEATH) {
            if (level instanceof ServerLevel serverLevel) {
                DamageSource laser = TechDamageTypes.source(serverLevel, TechDamageTypes.LASER);
                entity.hurtServer(serverLevel, laser, 4);
            }
        } else if (type == BridgeType.GRAVITY) {

            // Don't apply lift if wearing gravity boots
            if (entity instanceof LivingEntity livingEntity && livingEntity.getItemBySlot(EquipmentSlot.FEET).getItem() == TechItems.GRAVITY_BOOTS.get()) {
                return;
            }

            BlockEntity te = level.getBlockEntity(pos);

            if (te instanceof BridgeBlockEntity bridge) {
                double d = 0.40000000000000002D;
                Direction facing = bridge.getFacing();
                int offset = facing.getAxisDirection().getStep();
                Vec3 movement = entity.getDeltaMovement();

                double movX = movement.x;
                double movY = movement.y;
                double movZ = movement.z;

                if (facing.getAxis() == Direction.Axis.X) {

                    movX += (double) offset * d;
                    if (offset > 0.0F && movX > 1.0D) {
                        movX = 1.0D;
                    }
                    if (offset < 0.0F && movX < -1D) {
                        movX = -1D;
                    }
                    if (movY < 0.0D) {
                        movY = 0.0D;
                    } else {
                        movY *= 0.5D;
                    }
                    movZ *= 0.5D;
                } else if (facing.getAxis() == Direction.Axis.Y) {
                    movY += (double) facing.getAxisDirection().getStep() * d;
                    if (offset > 0.0F && movY > 1.0D) {
                        movY = 1.0D;
                    }
                    if (offset < 0.0F && movY < -1D) {
                        movY = -1D;
                    }
                    movX *= 0.5D;
                    movZ *= 0.5D;
                } else if (facing.getAxis() == Direction.Axis.Z) {
                    movZ += (double) facing.getAxisDirection().getStep() * d;
                    if (offset > 0.0F && movZ > 1.0D) {
                        movZ = 1.0D;
                    }
                    if (offset < 0.0F && movZ < -1D) {
                        movZ = -1D;
                    }
                    movX *= 0.5D;
                    if (movY < 0.0D) {
                        movY = 0.0D;
                    } else {
                        movY *= 0.5D;
                    }
                }
                entity.fallDistance = 0.0F;
                entity.setDeltaMovement(new Vec3(movX, movY, movZ));
            }
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BridgeBlockEntity(pos, state);
    }

    public static boolean canLaserBreak(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.canBeReplaced()) {
            return true;
        }

        return state.is(TechTags.Blocks.LASER_BREAKABLES);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        BlockState stored = this.getStoredState(reader, pos);
        return !stored.isAir() ? stored.getVisualShape(reader, pos, context) : super.getVisualShape(state, reader, pos, context);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter reader, BlockPos pos) {
        BlockState stored = this.getStoredState(reader, pos);
        return stored.isAir() ? super.getShadeBrightness(state, reader, pos) : stored.getShadeBrightness(reader, pos);
    }

    /** Read once: the light engines ask this per node, from their own thread. */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState stored = this.getStoredState(level, pos);
        return stored.isAir() ? state.getLightEmission() : stored.getLightEmission();
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack itemstack = new ItemStack(this);
        NBTHelper.putTag(itemstack, "stored_state", NbtUtils.writeBlockState(Blocks.AIR.defaultBlockState()));
        return itemstack;
    }

    /** Read the way the light engine reads it - from any thread - since that is who asks for the emission. */
    public BlockState getStoredState(BlockGetter worldIn, BlockPos pos) {
        if (IBlockLightEmission.blockEntityAt(worldIn, pos) instanceof BridgeBlockEntity bridge) {
            return bridge.getStoredBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public Supplier<IBlockClientEffects> getClientEffects() {
        return BridgeClientEffects::new;
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return ServerEffectUtils.addLandingEffects(this.getStoredState(level, pos), level, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return ServerEffectUtils.addRunningEffects(this.getStoredState(level, pos), level, entity);
    }
}
