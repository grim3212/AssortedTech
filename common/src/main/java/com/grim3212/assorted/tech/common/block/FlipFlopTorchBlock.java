package com.grim3212.assorted.tech.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TorchBlock} now takes a {@code SimpleParticleType} rather than any {@code ParticleOptions},
 * and {@code DustParticleOptions.REDSTONE} is not one, so this extends {@link BaseTorchBlock} - the
 * shared standing-torch shape and support rules - and draws its own flame particle exactly the way
 * vanilla's {@code RedstoneTorchBlock} does.
 */
public class FlipFlopTorchBlock extends BaseTorchBlock {

	public static final MapCodec<FlipFlopTorchBlock> CODEC = simpleCodec(FlipFlopTorchBlock::new);

	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	public static final BooleanProperty PREV_LIT = BooleanProperty.create("prev_lit");

	public FlipFlopTorchBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(PREV_LIT, false));
	}

	@Override
	protected MapCodec<? extends BaseTorchBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(LIT, PREV_LIT);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean movedByPiston) {
		for (Direction direction : Direction.values()) {
			level.updateNeighborsAt(pos.relative(direction), this);
		}
	}

	/**
	 * Replaces {@code onRemove}. 26.x only calls this for a real removal, so the "did the block
	 * actually change" guard the old method needed is gone.
	 */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		if (!movedByPiston) {
			for (Direction direction : Direction.values()) {
				level.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
		level.scheduleTick(pos, this, 2);
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction dir) {
		return state.getValue(LIT) && Direction.UP != dir ? 15 : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction dir) {
		return dir == Direction.DOWN ? state.getSignal(getter, pos, dir) : 0;
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
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
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
		if (state.getValue(LIT)) {
			double d0 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			level.addParticle(DustParticleOptions.REDSTONE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	protected boolean hasNeighborSignal(Level level, BlockPos pos, BlockState state) {
		return level.hasSignal(pos.below(), Direction.DOWN);
	}
}
