package com.grim3212.assorted.tech.common.block;

import java.util.List;

import com.grim3212.assorted.tech.common.block.blockentity.SensorBlockEntity;
import com.grim3212.assorted.tech.common.util.SensorType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class SensorBlock extends Block {

	public static final BooleanProperty DETECTED = BooleanProperty.create("detected");
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	private final SensorType sensorType;

	public SensorBlock(Properties props, SensorType sensorType) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(DETECTED, false).setValue(FACING, Direction.NORTH));
		this.sensorType = sensorType;
	}

	public SensorType getSensorType() {
		return sensorType;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(DETECTED, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(DETECTED, false);
	}

	@Override
	public void appendHoverText(ItemStack stack, IBlockReader level, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(new TranslationTextComponent("tooltip.sensor.detects." + this.sensorType.name().toLowerCase()).withStyle(TextFormatting.GRAY));
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
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public int getSignal(BlockState state, IBlockReader getter, BlockPos pos, Direction dir) {
		return state.getValue(DETECTED) ? 15 : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
		ItemStack inHand = player.getItemInHand(hand);
		TileEntity entity = level.getBlockEntity(pos);
		if (entity instanceof SensorBlockEntity sensor) {
			if (inHand.getItem() == Items.REDSTONE_TORCH) {
				sensor.toggleShowRange();
				return ActionResultType.SUCCESS;
			} else {
				if (player.isShiftKeyDown()) {
					int newRange = sensor.reverseCycleRange();
					player.displayClientMessage(new TranslationTextComponent("message.sensor.range", newRange), true);
				} else {
					int newRange = sensor.cycleRange();
					player.displayClientMessage(new TranslationTextComponent("message.sensor.range", newRange), true);
				}
				return ActionResultType.SUCCESS;
			}
		}
		return super.use(state, level, pos, player, hand, hitResult);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SensorBlockEntity(this.sensorType);
	}
}
