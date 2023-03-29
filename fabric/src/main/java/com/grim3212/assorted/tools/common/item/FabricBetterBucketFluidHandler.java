package com.grim3212.assorted.tools.common.item;

import com.grim3212.assorted.tech.sounds.ToolsCommonMod;
import com.grim3212.assorted.tech.sounds.common.item.BetterBucketItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FabricBetterBucketFluidHandler implements SingleSlotStorage<FluidVariant> {
    private final ContainerItemContext context;

    public FabricBetterBucketFluidHandler(ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!(context.getItemVariant().getItem() instanceof final BetterBucketItem bucket)) return 0;

        var stack = context.getItemVariant().toStack();
        if (BetterBucketItem.isEmptyOrContains(stack, BetterBucketItem.getStringFromFluid(resource.getFluid()))) {
            int currentAmount = BetterBucketItem.getAmount(stack);
            if (currentAmount < bucket.getMaximumMillibuckets()) {

                int newMax = (int) maxAmount;
                int newAmount = currentAmount + newMax;
                int leftOver = newAmount - bucket.getMaximumMillibuckets();
                int returnAmount = newMax;

                // We weren't able to add it all but the bucket is now full
                if (leftOver >= 0) {
                    // We were able to add everything but the leftover
                    returnAmount = newMax - leftOver;
                } else {
                    // The full amount was added

                    //Validate if we are in partial mode or not
                    if (!ToolsCommonMod.COMMON_CONFIG.allowPartialBucketAmounts.get()) {
                        int bucketLeftOver = newMax % BetterBucketItem.getBucketAmount();
                        newAmount -= bucketLeftOver;

                        // We could add everything but the partial bucket amount of the leftover
                        returnAmount = newMax - bucketLeftOver;
                    }
                }

                BetterBucketItem.setAmount(stack, Math.min(newAmount, bucket.getMaximumMillibuckets()));
                ItemVariant newVariant = ItemVariant.of(stack);
                if (context.exchange(newVariant, 1, transaction) == 1) {
                    return returnAmount;
                }
            }
        }

        return 0;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        if (!(context.getItemVariant().getItem() instanceof final BetterBucketItem bucket)) return 0;

        ItemStack stack = context.getItemVariant().toStack();
        String stored = BetterBucketItem.getFluid(stack);
        if (stored.equals(BetterBucketItem.getStringFromFluid(resource.getFluid())) && !BetterBucketItem.getFluidFromString(stored).isSame(Fluids.EMPTY)) {
            int currentAmount = BetterBucketItem.getAmount(stack);
            int newMax = (int) maxAmount;
            int returnAmount = newMax;
            if (newMax > bucket.getMaximumMillibuckets()) {
                newMax = bucket.getMaximumMillibuckets();
            }

            int newAmount = currentAmount - newMax;

            // We weren't able to extract the full thing but the bucket is now empty
            if (newAmount < 0) {
                returnAmount = newMax + newAmount;
                newAmount = 0;
            } else {
                // The full amount was extracted and the bucket still has fluid

                //Validate if we are in partial mode or not
                if (!ToolsCommonMod.COMMON_CONFIG.allowPartialBucketAmounts.get()) {
                    int bucketLeftOver = newMax % BetterBucketItem.getBucketAmount();
                    int amountRemoved = Math.max(newMax - bucketLeftOver, 0);
                    newAmount = currentAmount - amountRemoved;

                    // We could extract everything but the partial bucket amount of the leftover
                    returnAmount = amountRemoved;
                }
            }

            ItemVariant newVariant;
            if (newAmount == 0 && !bucket.getBreakStack().isEmpty()) {
                newVariant = ItemVariant.of(bucket.getBreakStack().copy());
            } else {
                BetterBucketItem.setAmount(stack, newAmount);
                newVariant = ItemVariant.of(stack);
            }

            if (context.exchange(newVariant, 1, transaction) == 1) {
                return returnAmount;
            }
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        String stored = BetterBucketItem.getFluid(context.getItemVariant().toStack());
        return stored.equals(BetterBucketItem.emptyMarker());
    }

    @Override
    public FluidVariant getResource() {
        Fluid stored = BetterBucketItem.getFluidFromString(BetterBucketItem.getFluid(context.getItemVariant().toStack()));
        return FluidVariant.of(stored);
    }

    @Override
    public long getAmount() {
        return BetterBucketItem.getAmount(context.getItemVariant().toStack());
    }

    @Override
    public long getCapacity() {
        if (context.getItemVariant().getItem() instanceof BetterBucketItem bucketItem) {
            return bucketItem.getMaximumMillibuckets();
        }

        return BetterBucketItem.getBucketAmount();
    }
}
