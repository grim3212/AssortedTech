package com.grim3212.assorted.tech.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class FlipFlopTorchBlock extends TorchBlock {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty PREV_LIT = BooleanProperty.create("prev_lit");

	public FlipFlopTorchBlock(Properties props) {
		super(props, RedstoneParticleData.REDSTONE);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(PREV_LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(LIT, PREV_LIT);
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
	public int getSignal(BlockState state, IBlockReader getter, BlockPos pos, Direction dir) {
		return state.getValue(LIT) && Direction.UP != dir ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState state, IBlockReader getter, BlockPos pos, Direction dir) {
		return dir == Direction.DOWN ? state.getSignal(getter, pos, dir) : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public void tick(BlockState state, ServerWorld level, BlockPos pos, Random rand) {
		boolean flag = this.hasNeighborSignal(level, pos, state);
		boolean prevFlag = state.getValue(PREV_LIT);

		if (flag != prevFlag) {
			if (flag) {
				if (state.getValue(LIT)) {
					level.setBlock(pos, state.setValue(LIT, false).setValue(PREV_LIT, flag), 3);
				} else {
					level.setBlock(pos, state.setValue(LIT, true).setValue(PREV_LIT, flag), 3);
				}
			} else {
				level.setBlock(pos, state.setValue(PREV_LIT, flag), 3);
			}
		} else {
			level.setBlock(pos, state.setValue(PREV_LIT, flag), 3);
		}
	}

	@Override
	public void animateTick(BlockState state, World level, BlockPos pos, Random rand) {
		if (state.getValue(LIT)) {
			double d0 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			level.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	protected boolean hasNeighborSignal(World level, BlockPos pos, BlockState state) {
		return level.hasSignal(pos.below(), Direction.DOWN);
	}
}
