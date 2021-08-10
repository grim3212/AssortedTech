package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.util.FanMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class FanBlock extends Block implements EntityBlock {

	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final EnumProperty<FanMode> MODE = EnumProperty.create("mode", FanMode.class);

	public FanBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(MODE, FanMode.BLOW).setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(MODE, FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FanBlockEntity(pos, state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide) {
			AssortedTech.proxy.openFanScreen((FanBlockEntity) worldIn.getBlockEntity(pos));
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighbor, boolean flag) {
		boolean isPowered = level.hasNeighborSignal(pos);
		BlockEntity ent = level.getBlockEntity(pos);

		if (ent instanceof FanBlockEntity fan) {
			if (isPowered && state.getValue(MODE) != FanMode.OFF) {
				if (fan.getMode() != FanMode.OFF) {
					fan.setOldMode(fan.getMode());
					fan.setMode(FanMode.OFF);
				}
				level.setBlock(pos, state.setValue(MODE, FanMode.OFF), 3);
			} else if (!isPowered && state.getValue(MODE) == FanMode.OFF) {
				fan.setMode(fan.getOldMode());
				level.setBlock(pos, state.setValue(MODE, fan.getOldMode()), 3);
			}
		}
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (level1, blockPos, blockState, t) -> {
			if (t instanceof FanBlockEntity fan) {
				fan.tick();
			}
		};
	}
}
