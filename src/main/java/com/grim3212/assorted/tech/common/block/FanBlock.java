package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.AssortedTech;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.util.FanMode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class FanBlock extends Block {

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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
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
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isClientSide) {
			AssortedTech.proxy.openFanScreen((FanBlockEntity) worldIn.getBlockEntity(pos));
			return ActionResultType.SUCCESS;
		} else {
			return ActionResultType.SUCCESS;
		}
	}

	@Override
	public void neighborChanged(BlockState state, World level, BlockPos pos, Block block, BlockPos neighbor, boolean flag) {
		boolean isPowered = level.hasNeighborSignal(pos);
		TileEntity ent = level.getBlockEntity(pos);

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
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new FanBlockEntity();
	}
}
