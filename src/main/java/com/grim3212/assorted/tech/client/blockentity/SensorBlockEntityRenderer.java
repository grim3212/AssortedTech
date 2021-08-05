package com.grim3212.assorted.tech.client.blockentity;

import com.grim3212.assorted.tech.common.block.blockentity.SensorBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SensorBlockEntityRenderer implements BlockEntityRenderer<SensorBlockEntity> {

	public SensorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(SensorBlockEntity entity, float partialTicks, PoseStack stack, MultiBufferSource bufferSource, int destroyStage, int alpha) {
		if (entity.shouldShowRange()) {
			stack.pushPose();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.lineWidth(2.0F);
			RenderSystem.disableTexture();
			RenderSystem.depthMask(false);

			VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.lines());
			Direction dir = entity.getBlockState().getValue(BlockStateProperties.FACING);

			// The plus 1 is because we deflate by 1 so we don't include the block itself
			int maxLength = entity.getRange() + 1;
			

			boolean obstructed = false;
			int traverse = 1;
			while (traverse < maxLength && !obstructed) {
				BlockPos checkPos = entity.getBlockPos().relative(dir, traverse);
				BlockState checkState = entity.getLevel().getBlockState(checkPos);
				if (Block.isFaceFull(checkState.getCollisionShape(entity.getLevel(), checkPos), dir.getOpposite())) {
					obstructed = true;
				} else {
					traverse++;
				}
			}
			
			Vec3i position = dir.getNormal().multiply(obstructed ? traverse : maxLength);

			//AssortedTech.LOGGER.info("isobstructed: " + obstructed + ", " + traverse);
			
			AABB aabb = entity.getBlockState().getCollisionShape(entity.getLevel(), entity.getBlockPos()).bounds().expandTowards(position.getX(), position.getY(), position.getZ()).deflate(1D);
			VoxelShape shape = Block.box(aabb.minX * 16D, aabb.minY * 16D, aabb.minZ * 16D, aabb.maxX * 16D, aabb.maxY * 16D, aabb.maxZ * 16D);

			LevelRenderer.renderShape(stack, vertexconsumer, shape, 0.0D, 0.0D, 0.0D, obstructed ? 1.0F : 255.0F,  obstructed ? 255.0F : 1.0F,  obstructed ? 255.0F : 255.0F, 1.0F);

			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();

			stack.popPose();
		}
	}

}
