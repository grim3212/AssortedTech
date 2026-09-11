package com.grim3212.assorted.tech.gametest;

import com.grim3212.assorted.lib.util.NBTHelper;
import com.grim3212.assorted.tech.common.block.TechBlocks;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * What a headless server cannot see: how a bridge item is drawn, and a spike's tooltip as Fabric
 * builds it. Fabric only, as NeoForge has no client gametest; run with
 * {@code ./gradlew :fabric:runClientGameTest}.
 */
public class TechClientGameTests implements FabricClientGameTest {

    private static final Identifier GOLD = Identifier.withDefaultNamespace("block/gold_block");

    @Override
    public void runTest(ClientGameTestContext context) {
        // Inside a world: an ItemStack cannot be made on the title screen, because an item's default
        // components are only bound once a world's registries have loaded.
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            context.runOnClient(client -> {
                // A bridge item holding gold is drawn from the gold block, so that is also what its
                // particles are made of. The particle comes off the same model data the quads do.
                ItemStack filled = new ItemStack(TechBlocks.BRIDGE.get());
                NBTHelper.putTag(filled, "stored_state", NbtUtils.writeBlockState(Blocks.GOLD_BLOCK.defaultBlockState()));
                Identifier filledParticle = particle(client, filled);
                if (!GOLD.equals(filledParticle)) {
                    throw new AssertionError("a bridge item holding gold is drawn with " + filledParticle + ", not " + GOLD);
                }

                // So the check above can only pass by reading the stack.
                Identifier emptyParticle = particle(client, new ItemStack(TechBlocks.BRIDGE.get()));
                if (GOLD.equals(emptyParticle)) {
                    throw new AssertionError("an empty bridge item is drawn with gold");
                }

                // Fabric only adds component tooltip lines on the client.
                ItemStack spike = new ItemStack(TechBlocks.SPIKES.get(0).get());
                List<String> spikeTooltip = spike.getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL).stream()
                        .map(line -> line.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : line.getString())
                        .toList();
                if (!spikeTooltip.contains("tooltip.spike.damage")) {
                    throw new AssertionError("a spike's tooltip is " + spikeTooltip);
                }
            });
        }
    }

    /** The particle sprite an item throws off, resolved exactly as a dropped item is drawn. */
    private static Identifier particle(Minecraft client, ItemStack stack) {
        ItemStackRenderState state = new ItemStackRenderState();
        client.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GROUND, null, null, 0);
        Material.Baked particle = state.pickParticleMaterial(RandomSource.create(0L));
        return particle == null ? null : particle.sprite().contents().name();
    }
}
