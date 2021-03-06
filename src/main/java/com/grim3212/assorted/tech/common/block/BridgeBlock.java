package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.util.BridgeType;
import com.grim3212.assorted.tech.common.util.NBTHelper;
import com.grim3212.assorted.tech.common.util.TechDamageSources;
import com.grim3212.assorted.tech.common.util.TechTags;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.CreativeModeTab;
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

public class BridgeBlock extends Block implements EntityBlock {

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
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
	}

	@Override
	public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		if (entity instanceof EnderDragon || entity instanceof WitherBoss || entity instanceof WitherSkull)
			return false;

		return true;
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
			entity.hurt(TechDamageSources.LASER, 4);
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
		return this.getStoredState(reader, pos) != Blocks.AIR.defaultBlockState() ? this.getStoredState(reader, pos).propagatesSkylightDown(reader, pos) : super.propagatesSkylightDown(state, reader, pos);
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
		return this.getStoredState(reader, pos) != Blocks.AIR.defaultBlockState() ? this.getStoredState(reader, pos).getVisualShape(reader, pos, context) : super.getVisualShape(state, reader, pos, context);
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		return this.getStoredState(world, pos) != Blocks.AIR.defaultBlockState() ? this.getStoredState(world, pos).getLightEmission(world, pos) : super.getLightEmission(state, world, pos);
	}

	@Override
	public boolean addLandingEffects(BlockState state, ServerLevel worldObj, BlockPos blockPosition, BlockState iblockstate, LivingEntity entity, int numberOfParticles) {
		BlockEntity tileentity = (BlockEntity) worldObj.getBlockEntity(blockPosition);
		if (tileentity instanceof BridgeBlockEntity bridge) {
			if (bridge.getStoredBlockState() == Blocks.AIR.defaultBlockState()) {
				return super.addLandingEffects(state, worldObj, blockPosition, iblockstate, entity, numberOfParticles);
			} else {
				worldObj.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, bridge.getStoredBlockState()), entity.getX(), entity.getY(), entity.getZ(), numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15000000596046448D);
			}
		}
		return true;
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
}
