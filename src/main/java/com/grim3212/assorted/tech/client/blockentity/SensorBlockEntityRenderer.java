package com.grim3212.assorted.tech.client.blockentity;

import com.grim3212.assorted.tech.common.block.blockentity.SensorBlockEntity;
import com.grim3212.assorted.tech.common.util.TechUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3i;

public class SensorBlockEntityRenderer extends TileEntityRenderer<SensorBlockEntity> {

	public SensorBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public void render(SensorBlockEntity entity, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferSource, int destroyStage, int alpha) {
		if (entity.shouldShowRange()) {
			stack.pushPose();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.lineWidth(2.0F);
			RenderSystem.disableTexture();
			RenderSystem.depthMask(false);

			IVertexBuilder vertexconsumer = bufferSource.getBuffer(RenderType.lines());
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

			Vector3i position = TechUtil.multiply(dir.getNormal(), obstructed ? traverse : maxLength);
			AxisAlignedBB aabb = entity.getBlockState().getCollisionShape(entity.getLevel(), entity.getBlockPos()).bounds().expandTowards(position.getX(), position.getY(), position.getZ()).deflate(1D).inflate(0.0020000000949949026D);
			VoxelShape shape = Block.box(aabb.minX * 16D, aabb.minY * 16D, aabb.minZ * 16D, aabb.maxX * 16D, aabb.maxY * 16D, aabb.maxZ * 16D);

			WorldRenderer.renderShape(stack, vertexconsumer, shape, 0.0D, 0.0D, 0.0D, obstructed ? 1.0F : 255.0F, obstructed ? 255.0F : 1.0F, obstructed ? 255.0F : 255.0F, 1.0F);

			RenderSystem.depthMask(true);
			RenderSystem.enableTexture();
			RenderSystem.disableBlend();

			stack.popPose();
		}
	}

}
