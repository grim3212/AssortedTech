package com.grim3212.assorted.tech.common.block.blockentity;

import java.util.List;

import com.grim3212.assorted.tech.common.block.GravityBlock;
import com.grim3212.assorted.tech.common.block.GravityDirectionalBlock;
import com.grim3212.assorted.tech.common.handler.TechConfig;
import com.grim3212.assorted.tech.common.item.TechItems;
import com.grim3212.assorted.tech.common.util.GravityType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GravityDirectionalBlockEntity extends BlockEntity {

	private boolean showRange = false;
	private int range = 1;

	public GravityDirectionalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public GravityDirectionalBlockEntity(BlockPos pos, BlockState state) {
		super(TechBlockEntityTypes.GRAVITY_DIRECTIONAL.get(), pos, state);
	}

	public boolean shouldShowRange() {
		return showRange;
	}

	public void toggleShowRange() {
		this.showRange = !this.showRange;
		this.markUpdated();
	}

	public int getRange() {
		return this.range;
	}

	public int cycleRange() {
		int newRange = this.range + 1;
		if (newRange > TechConfig.COMMON.gravityMaxRange.get())
			this.range = 1;
		else
			this.range = newRange;
		this.markUpdated();
		return this.range;
	}

	public int reverseCycleRange() {
		int newRange = this.range - 1;
		if (newRange < 1)
			this.range = TechConfig.COMMON.gravityMaxRange.get();
		else
			this.range = newRange;
		this.markUpdated();
		return this.range;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.showRange = nbt.getBoolean("ShowRange");
		this.range = nbt.getInt("Range");
	}

	@Override
	protected void saveAdditional(CompoundTag cmp) {
		super.saveAdditional(cmp);
		cmp.putBoolean("ShowRange", showRange);
		cmp.putInt("Range", range);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private void markUpdated() {
		this.setChanged();
		this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
	}

	public void tick() {
		BlockPos pos = this.getBlockPos();
		BlockState state = this.level.getBlockState(pos);

		if (state.getBlock()instanceof GravityDirectionalBlock gravityBlock) {
			if(!state.getValue(GravityBlock.POWERED)) return;
			
			GravityType type = gravityBlock.getType();
			Direction dir = state.getValue(GravityDirectionalBlock.FACING);

			int maxLength = this.range + 1;

			boolean obstructed = false;
			int traverse = 1;
			while (traverse < maxLength && !obstructed) {
				BlockPos checkPos = pos.relative(dir, traverse);
				BlockState checkState = level.getBlockState(checkPos);
				if (Block.isFaceFull(checkState.getCollisionShape(level, checkPos), dir.getOpposite())) {
					obstructed = true;
				} else {
					traverse++;
				}
			}

			int distance = obstructed ? traverse : maxLength;
			Vec3i fanPos = dir.getNormal().multiply(distance);
			AABB aabb = state.getCollisionShape(level, pos).bounds().move(pos).expandTowards(fanPos.getX(), fanPos.getY(), fanPos.getZ()).deflate(1D);

			List<? extends Entity> list = level.getEntities((Entity) null, aabb);

			if (type == GravityType.GRAVITATE) {
				double speed = TechConfig.COMMON.gravitorSpeed.get();

				list.stream().forEach((entity -> {
					if (entity instanceof Player player) {
						for (ItemStack armorStack : player.getArmorSlots()) {
							if (armorStack.getItem() == TechItems.GRAVITY_BOOTS.get()) {
								return;
							}
						}
					}
					
					if (!(entity instanceof FallingBlockEntity)) {
						entity.fallDistance = 0F;

						Vec3 mot = entity.getDeltaMovement();
						switch (dir) {
							case DOWN -> entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
							case UP -> entity.setDeltaMovement(mot.x, mot.y - speed, mot.z);
							case NORTH -> entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
							case SOUTH -> entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
							case WEST -> entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
							case EAST -> entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
						}
					}
				}));

			} else {
				boolean isSucking = type == GravityType.ATTRACT;

				double distanceModifier = TechConfig.COMMON.gravityMaxRange.get() - this.range + 1;
				double fanSpeed = isSucking ? -TechConfig.COMMON.attractRepulseSpeed.get() : TechConfig.COMMON.attractRepulseSpeed.get();
				double fanModSpeed = isSucking ? -TechConfig.COMMON.attractRepulseModSpeed.get() : TechConfig.COMMON.attractRepulseModSpeed.get();
				double speed = fanSpeed + (fanModSpeed / distanceModifier);

				list.stream().forEach((entity -> {
					if (entity instanceof Player player) {
						for (ItemStack armorStack : player.getArmorSlots()) {
							if (armorStack.getItem() == TechItems.GRAVITY_BOOTS.get()) {
								return;
							}
						}
					}
					
					if (!(entity instanceof FallingBlockEntity)) {
						if (dir == Direction.UP && type == GravityType.REPULSE) {
							entity.fallDistance = 0F;
						}

						Vec3 mot = entity.getDeltaMovement();
						switch (dir) {
							case DOWN -> entity.setDeltaMovement(mot.x, mot.y - speed, mot.z);
							case UP -> entity.setDeltaMovement(mot.x, mot.y + speed, mot.z);
							case NORTH -> entity.setDeltaMovement(mot.x, mot.y, mot.z - speed);
							case SOUTH -> entity.setDeltaMovement(mot.x, mot.y, mot.z + speed);
							case WEST -> entity.setDeltaMovement(mot.x - speed, mot.y, mot.z);
							case EAST -> entity.setDeltaMovement(mot.x + speed, mot.y, mot.z);
						}
					}
				}));
			}
		}
	}
}
