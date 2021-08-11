package com.grim3212.assorted.tech.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class GlowstoneWallTorchBlock extends GlowstoneTorchBlock {

	public static final DirectionProperty FACING = HorizontalBlock.FACING;

	public GlowstoneWallTorchBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public String getDescriptionId() {
		return this.asItem().getDescriptionId();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader getter, BlockPos p_55783_, ISelectionContext p_55784_) {
		return WallTorchBlock.getShape(state);
	}

	@Override
	public boolean canSurvive(BlockState state, IWorldReader level, BlockPos p_55764_) {
		return Blocks.WALL_TORCH.canSurvive(state, level, p_55764_);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction dir, BlockState p_55774_, IWorld level, BlockPos p_55776_, BlockPos p_55777_) {
		return Blocks.WALL_TORCH.updateShape(state, dir, p_55774_, level, p_55776_, p_55777_);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(context);
		return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(FACING));
	}

	@Override
	public void animateTick(BlockState state, World level, BlockPos pos, Random rand) {
		if (state.getValue(LIT)) {
			Direction direction = state.getValue(FACING).getOpposite();
			double d0 = 0.27D;
			double d1 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepX();
			double d2 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D + 0.22D;
			double d3 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D + 0.27D * (double) direction.getStepZ();
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
}
