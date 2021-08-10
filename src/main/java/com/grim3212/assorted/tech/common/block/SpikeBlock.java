package com.grim3212.assorted.tech.common.block;

import java.util.List;
import java.util.Random;

import com.grim3212.assorted.tech.common.util.SpikeType;
import com.grim3212.assorted.tech.common.util.TechDamageSources;
import com.grim3212.assorted.tech.common.util.TechSounds;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpikeBlock extends Block {

	private static final VoxelShape UP_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
	private static final VoxelShape DOWN_SHAPE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape NORTH_SHAPE = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
	private static final VoxelShape WEST_SHAPE = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	private final SpikeType spikeType;

	public SpikeBlock(Properties props, SpikeType spikeType) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(FACING, Direction.NORTH));
		this.spikeType = spikeType;
	}

	public SpikeType getSpikeType() {
		return spikeType;
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(new TranslatableComponent("tooltip.spike.damage", new TranslatableComponent(String.valueOf(this.spikeType.getDamage())).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext collisionContext) {
		if (!state.getValue(POWERED)) {
			switch (state.getValue(FACING)) {
				case DOWN:
					return DOWN_SHAPE;
				case EAST:
					return EAST_SHAPE;
				case NORTH:
					return NORTH_SHAPE;
				case SOUTH:
					return SOUTH_SHAPE;
				case UP:
					return UP_SHAPE;
				case WEST:
					return WEST_SHAPE;
			}
		}

		return Shapes.block();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState blockstate = this.defaultBlockState();
		LevelReader iworldreader = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		Direction[] adirection = context.getNearestLookingDirections();

		for (Direction direction : adirection) {
			Direction direction1 = direction.getOpposite();
			blockstate = blockstate.setValue(FACING, direction1);
			if (blockstate.canSurvive(iworldreader, blockpos)) {
				return blockstate;
			}
		}

		return null;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState state2, LevelAccessor level, BlockPos currentPos, BlockPos pos2) {
		return facing.getOpposite() == stateIn.getValue(FACING) && !stateIn.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : stateIn;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		Direction direction = state.getValue(FACING);
		BlockPos blockpos = pos.relative(direction.getOpposite());
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return blockstate.isFaceSturdy(worldIn, blockpos, direction);
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
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState state2, boolean flag) {
		for (Direction direction : Direction.values()) {
			level.updateNeighborsAt(pos.relative(direction), this);
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state2, boolean flag) {
		if (!flag) {
			for (Direction direction : Direction.values()) {
				level.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean flag) {
		level.getBlockTicks().scheduleTick(pos, this, 2);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
		Direction dir = state.getValue(FACING);
		BlockPos poweredPos = pos.offset(dir.getOpposite().getNormal());
		if (!state.getValue(POWERED) && level.hasNeighborSignal(poweredPos)) {
			level.playSound(null, pos, TechSounds.SPIKE_DEPLOY.get(), SoundSource.BLOCKS, 0.3F, 0.6F);
			level.setBlock(pos, state.setValue(POWERED, true), 3);
		} else if (state.getValue(POWERED) && !level.hasNeighborSignal(poweredPos)) {
			level.playSound(null, pos, TechSounds.SPIKE_CLOSE.get(), SoundSource.BLOCKS, 0.3F, 0.6F);
			level.setBlock(pos, state.setValue(POWERED, false), 3);
		}
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (state.getValue(POWERED) && entity instanceof LivingEntity e) {
			e.hurt(TechDamageSources.SPIKE, this.spikeType.getDamage());
		}
	}
}
