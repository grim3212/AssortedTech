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

public class GlowstoneTorchBlock extends TorchBlock {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public GlowstoneTorchBlock(Properties props) {
		super(props, RedstoneParticleData.REDSTONE);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(LIT);
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
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
		return state.getValue(LIT) ? 15 : 0;
	}

	@Override
	public void tick(BlockState state, ServerWorld level, BlockPos pos, Random rand) {
		if (!state.getValue(LIT) && level.hasNeighborSignal(pos)) {
			level.setBlock(pos, state.setValue(LIT, true), 3);
		} else if (state.getValue(LIT) && !level.hasNeighborSignal(pos)) {
			level.setBlock(pos, state.setValue(LIT, false), 3);
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
}
