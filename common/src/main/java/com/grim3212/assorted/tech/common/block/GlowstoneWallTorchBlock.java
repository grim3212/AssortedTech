package com.grim3212.assorted.tech.common.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowstoneWallTorchBlock extends GlowstoneTorchBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public GlowstoneWallTorchBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	public String getDescriptionId() {
		return this.asItem().getDescriptionId();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos p_55783_, CollisionContext p_55784_) {
		return WallTorchBlock.getShape(state);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos p_55764_) {
		return Blocks.WALL_TORCH.canSurvive(state, level, p_55764_);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction dir, BlockState p_55774_, LevelAccessor level, BlockPos p_55776_, BlockPos p_55777_) {
		return Blocks.WALL_TORCH.updateShape(state, dir, p_55774_, level, p_55776_, p_55777_);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(context);
		return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(FACING));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (state.getValue(LIT)) {
			Direction direction = state.getValue(FACING).getOpposite();
			double d0 = 0.27D;
			double d1 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + d0 * (double) direction.getStepX();
			double d2 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D + 0.22D;
			double d3 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + d0 * (double) direction.getStepZ();
			level.addParticle(this.flameParticle, d1, d2, d3, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return Blocks.WALL_TORCH.rotate(state, rot);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return Blocks.WALL_TORCH.mirror(state, mirror);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}
}
