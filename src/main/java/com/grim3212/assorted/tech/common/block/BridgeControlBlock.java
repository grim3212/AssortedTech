package com.grim3212.assorted.tech.common.block;

import com.grim3212.assorted.tech.common.block.blockentity.BridgeControlBlockEntity;
import com.grim3212.assorted.tech.common.util.BridgeType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class BridgeControlBlock extends Block implements EntityBlock {

	private final BridgeType type;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");

	public BridgeControlBlock(BridgeType type, Properties props) {
		super(props.isValidSpawn((s, g, p, e) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH));
		this.type = type;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BridgeControlBlockEntity(pos, state);
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
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
		if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
			level.setBlock(pos, state.cycle(POWERED), 2);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	public BridgeType getType() {
		return type;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return (level1, blockPos, blockState, t) -> {
			if (t instanceof BridgeControlBlockEntity bridge) {
				bridge.tick();
			}
		};
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		ItemStack heldItem = player.getItemInHand(hand);

		if (blockEntity instanceof BridgeControlBlockEntity bridgeControl) {
			if (!heldItem.isEmpty() && !player.isShiftKeyDown()) {
				if (heldItem.getItem() instanceof BlockItem block) {
					if (block != null && block.getBlock() != null) {
						BlockState currentState = bridgeControl.getStoredBlockState();
						BlockState tryToSetState = block.getBlock().defaultBlockState();

						if (currentState != tryToSetState && tryToSetState.getMaterial() != Material.DECORATION) {
							bridgeControl.setStoredBlockState(tryToSetState != null ? tryToSetState : Blocks.AIR.defaultBlockState());

							// Toggle to update blocks
							level.setBlockAndUpdate(pos, state.setValue(POWERED, false));

							level.playSound(player, pos, tryToSetState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (tryToSetState.getSoundType().getVolume() + 1.0F) / 2.0F, tryToSetState.getSoundType().getPitch() * 0.8F);

							return InteractionResult.sidedSuccess(level.isClientSide);

						}
					}
				}
			}

			if (player.isShiftKeyDown()) {
				bridgeControl.setStoredBlockState(Blocks.AIR.defaultBlockState());

				// Toggle to update blocks
				level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		}

		return InteractionResult.PASS;
	}
}
