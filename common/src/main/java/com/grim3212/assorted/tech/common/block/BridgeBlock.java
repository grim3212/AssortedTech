package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.lib.core.block.ExtraPropertyBlock;
import com.grim3212.assorted.lib.core.block.effects.*;
import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.tech.api.TechTags;
import com.grim3212.assorted.tech.api.util.BridgeType;
import com.grim3212.assorted.tech.client.model.BridgeClientEffects;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.api.util.TechDamageTypes;
import com.grim3212.assorted.tech.common.item.TechItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class BridgeBlock extends ExtraPropertyBlock implements EntityBlock, IBlockLandingEffects, IBlockRunningEffects, IBlockEffectSupplier {

    public static EnumProperty<BridgeType> TYPE = EnumProperty.create("type", BridgeType.class);

    public BridgeBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(TYPE, BridgeType.LASER));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return state.getValue(TYPE).isSolid() ? Shapes.block() : Shapes.empty();
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (level.getBlockState(pos).getValue(TYPE) == BridgeType.ACCEL && entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 2));
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        BridgeType type = level.getBlockState(pos).getValue(TYPE);

        if (type == BridgeType.DEATH) {
            entity.hurt(level.damageSources().source(TechDamageTypes.LASER), 4);
        } else if (type == BridgeType.GRAVITY) {

            if (entity instanceof LivingEntity livingEntity) {
                for (ItemStack stack : livingEntity.getArmorSlots()) {
                    // Don't apply lift if wearing gravity boots
                    if (stack.getItem() == TechItems.GRAVITY_BOOTS.get()) {
                        return;
                    }
                }
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

        if (state.getMaterial().isReplaceable()) {
            return true;
        }

        return state.is(TechTags.Blocks.LASER_BREAKABLES);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        BlockState stored = this.getStoredState(reader, pos);
        return !stored.isAir() ? stored.propagatesSkylightDown(reader, pos) : super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        BlockState stored = this.getStoredState(reader, pos);
        return !stored.isAir() ? stored.getVisualShape(reader, pos, context) : super.getVisualShape(state, reader, pos, context);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter reader, BlockPos pos) {
        BlockState stored = this.getStoredState(reader, pos);
        return stored.isAir() ? super.getLightBlock(state, reader, pos) : stored.getLightBlock(reader, pos);
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter reader, BlockPos pos) {
        BlockState stored = this.getStoredState(reader, pos);
        return stored.isAir() ? super.getShadeBrightness(state, reader, pos) : stored.getShadeBrightness(reader, pos);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return !this.getStoredState(level, pos).isAir() ? this.getStoredState(level, pos).getLightEmission() : state.getLightEmission();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack itemstack = new ItemStack(this);
        NBTHelper.putTag(itemstack, "stored_state", NbtUtils.writeBlockState(Blocks.AIR.defaultBlockState()));
        return itemstack;
    }

    public BlockState getStoredState(BlockGetter worldIn, BlockPos pos) {
        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof BridgeBlockEntity bridge) {
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
