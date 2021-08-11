package com.grim3212.assorted.tech.common.block;

import java.util.List;
import java.util.Random;

import com.grim3212.assorted.tech.common.util.SpikeType;
import com.grim3212.assorted.tech.common.util.TechDamageSources;
import com.grim3212.assorted.tech.common.util.TechSounds;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SpikeBlock extends Block implements IWaterLoggable {

	private static final VoxelShape UP_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
	private static final VoxelShape DOWN_SHAPE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
	private static final VoxelShape WEST_SHAPE = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private final SpikeType spikeType;

	public SpikeBlock(Properties props, SpikeType spikeType) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
		this.spikeType = spikeType;
	}

	public SpikeType getSpikeType() {
		return spikeType;
	}

	@Override
	public void appendHoverText(ItemStack stack, IBlockReader level, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(new TranslationTextComponent("tooltip.spike.damage", new TranslationTextComponent(String.valueOf(this.spikeType.getDamage())).withStyle(TextFormatting.AQUA)).withStyle(TextFormatting.GRAY));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, FACING, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader level, BlockPos pos, ISelectionContext collisionContext) {
		if (!state.getValue(POWERED)) {
			switch (state.getValue(FACING)) {
				case DOWN:
					return DOWN_SHAPE;
				case EAST:
					return EAST_SHAPE;
				case NORTH:
					return NORTH_SHAPE;
				case SOUTH:
					return SOUTH_SHAPE;
				case UP:
					return UP_SHAPE;
				case WEST:
					return WEST_SHAPE;
			}
		}

		return VoxelShapes.block();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState blockstate = this.defaultBlockState();
		World iworldreader = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		Direction[] adirection = context.getNearestLookingDirections();
		FluidState fluidstate = context.getLevel().getFluidState(blockpos);

		for (Direction direction : adirection) {
			Direction direction1 = direction.getOpposite();
			blockstate = blockstate.setValue(FACING, direction1);
			if (blockstate.canSurvive(iworldreader, blockpos)) {
				return blockstate.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
			}
		}

		return null;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState state2, IWorld level, BlockPos currentPos, BlockPos pos2) {
		if (stateIn.getValue(WATERLOGGED)) {
			level.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return facing.getOpposite() == stateIn.getValue(FACING) && !stateIn.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction direction = state.getValue(FACING);
		BlockPos blockpos = pos.relative(direction.getOpposite());
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return blockstate.isFaceSturdy(worldIn, blockpos, direction);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public void onPlace(BlockState state, World level, BlockPos pos, BlockState state2, boolean flag) {
		for (Direction direction : Direction.values()) {
			level.updateNeighborsAt(pos.relative(direction), this);
		}
	}

	@Override
	public void onRemove(BlockState state, World level, BlockPos pos, BlockState state2, boolean flag) {
		if (!flag) {
			for (Direction direction : Direction.values()) {
				level.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos neighborPos, boolean flag) {
		level.getBlockTicks().scheduleTick(pos, this, 2);
	}

	@Override
	public void tick(BlockState state, ServerWorld level, BlockPos pos, Random rand) {
		Direction dir = state.getValue(FACING);
		BlockPos poweredPos = pos.offset(dir.getOpposite().getNormal());
		if (!state.getValue(POWERED) && level.hasNeighborSignal(poweredPos)) {
			level.playSound(null, pos, TechSounds.SPIKE_DEPLOY.get(), SoundCategory.BLOCKS, 0.3F, 0.6F);
			level.setBlock(pos, state.setValue(POWERED, true), 3);
		} else if (state.getValue(POWERED) && !level.hasNeighborSignal(poweredPos)) {
			level.playSound(null, pos, TechSounds.SPIKE_CLOSE.get(), SoundCategory.BLOCKS, 0.3F, 0.6F);
			level.setBlock(pos, state.setValue(POWERED, false), 3);
		}
	}

	@Override
	public void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
		if (state.getValue(POWERED) && entity instanceof LivingEntity) {
			LivingEntity livingEnt = (LivingEntity) entity;
			livingEnt.hurt(TechDamageSources.SPIKE, this.spikeType.getDamage());
		}
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
}
