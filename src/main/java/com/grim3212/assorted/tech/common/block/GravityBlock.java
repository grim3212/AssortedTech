package com.grim3212.assorted.tech.common.block;

import java.util.Random;

import com.grim3212.assorted.tech.common.block.blockentity.GravityBlockEntity;
import com.grim3212.assorted.tech.common.util.GravityType;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class GravityBlock extends Block implements EntityBlock {

	public static final BooleanProperty POWERED = BooleanProperty.create("powered");
	private final GravityType type;

	public GravityBlock(GravityType type, Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
		this.type = type;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GravityBlockEntity(pos, state);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean flg) {
		if (!level.isClientSide) {
			boolean flag = state.getValue(POWERED);
			if (flag != level.hasNeighborSignal(pos)) {
				if (flag) {
					level.scheduleTick(pos, this, 4);
				} else {
					level.setBlock(pos, state.cycle(POWERED), 2);
				}
			}

		}
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
		if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
			level.setBlock(pos, state.cycle(POWERED), 2);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
	}

	public GravityType getType() {
		return type;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack inHand = player.getItemInHand(hand);
		BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof GravityBlockEntity gravity) {
			if (inHand.getItem() == Items.REDSTONE_TORCH) {
				gravity.toggleShowRange();
				return InteractionResult.SUCCESS;
			} else {
				if (player.isShiftKeyDown()) {
					int newRange = gravity.reverseCycleRange();
					player.displayClientMessage(new TranslatableComponent("message.sensor.range", newRange), true);
				} else {
					int newRange = gravity.cycleRange();
					player.displayClientMessage(new TranslatableComponent("message.sensor.range", newRange), true);
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (level1, blockPos, blockState, t) -> {
			if (t instanceof GravityBlockEntity gravity) {
				gravity.tick();
			}
		};
	}
}
