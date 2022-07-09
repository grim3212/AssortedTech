package com.grim3212.assorted.tech.client.proxy;

import com.grim3212.assorted.tech.client.blockentity.GravityBlockEntityRenderer;
import com.grim3212.assorted.tech.client.blockentity.GravityDirectionalBlockEntityRenderer;
import com.grim3212.assorted.tech.client.blockentity.SensorBlockEntityRenderer;
import com.grim3212.assorted.tech.client.model.BridgeModel;
import com.grim3212.assorted.tech.client.screen.AlarmScreen;
import com.grim3212.assorted.tech.client.screen.FanScreen;
import com.grim3212.assorted.tech.common.block.BridgeBlock;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import com.grim3212.assorted.tech.common.block.blockentity.AlarmBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.BridgeBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.FanBlockEntity;
import com.grim3212.assorted.tech.common.block.blockentity.TechBlockEntityTypes;
import com.grim3212.assorted.tech.common.proxy.IProxy;
import com.grim3212.assorted.tech.common.util.NBTHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientProxy implements IProxy {

	@Override
	public void starting() {
		final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::setupClient);
		modBus.addListener(this::loadComplete);
		modBus.addListener(this::registerLoaders);
	}

	private void registerLoaders(final ModelEvent.RegisterGeometryLoaders event) {
		event.register("bridge", BridgeModel.Loader.INSTANCE);
	}

	private void setupClient(final FMLClientSetupEvent event) {
		BlockEntityRenderers.register(TechBlockEntityTypes.SENSOR.get(), SensorBlockEntityRenderer::new);
		BlockEntityRenderers.register(TechBlockEntityTypes.GRAVITY.get(), GravityBlockEntityRenderer::new);
		BlockEntityRenderers.register(TechBlockEntityTypes.GRAVITY_DIRECTIONAL.get(), GravityDirectionalBlockEntityRenderer::new);
	}

	@Override
	public void openFanScreen(FanBlockEntity fan) {
		Minecraft.getInstance().setScreen(new FanScreen(fan));
	}

	@Override
	public void openAlarmScreen(AlarmBlockEntity fan) {
		Minecraft.getInstance().setScreen(new AlarmScreen(fan));
	}

	public void loadComplete(final FMLLoadCompleteEvent event) {
		event.enqueueWork(() -> {
			ItemColors items = Minecraft.getInstance().getItemColors();
			BlockColors blocks = Minecraft.getInstance().getBlockColors();

			blocks.register(new BlockColor() {
				@Override
				public int getColor(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, int tint) {
					if (pos != null) {
						BlockEntity te = worldIn.getBlockEntity(pos);
						if (te != null && te instanceof BridgeBlockEntity bridge) {

							if (bridge.getStoredBlockState() != Blocks.AIR.defaultBlockState()) {
								return Minecraft.getInstance().getBlockColors().getColor(bridge.getStoredBlockState(), worldIn, pos, tint);
							}

							return state.getValue(BridgeBlock.TYPE).getRenderColor();
						}
					}
					return 16777215;
				}
			}, TechBlocks.BRIDGE.get());

			items.register(new ItemColor() {
				@Override
				public int getColor(ItemStack stack, int tint) {
					if (stack != null && stack.hasTag()) {
						if (stack.getTag().contains("stored_state")) {
							BlockState stored = NbtUtils.readBlockState(NBTHelper.getTag(stack, "stored_state"));
							ItemStack colorStack = new ItemStack(stored.getBlock());
							if (colorStack.getItem() != null) {
								return Minecraft.getInstance().getItemColors().getColor(colorStack, tint);
							}
						}
					}
					return 16777215;
				}
			}, TechBlocks.BRIDGE.get());
		});
	}
}
