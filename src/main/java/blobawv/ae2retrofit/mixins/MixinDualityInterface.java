package blobawv.ae2retrofit.mixins;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ItemStack;

import appeng.helpers.DualityInterface;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.inv.IAEAppEngInventory;

import blobawv.ae2retrofit.util.ExtendedInterfaceLogic;
import blobawv.ae2retrofit.util.UpgradeSwapContext;

@Mixin(DualityInterface.class)
public abstract class MixinDualityInterface implements ExtendedInterfaceLogic {

    @Shadow(remap = false)
    @Final
    @Mutable
    private AppEngInternalInventory patterns;

    private static final int EXTENDED_PATTERN_SLOTS = 36;

    @Shadow(remap = false)
    protected abstract void updateCraftingList();

    @Override
    public void extend() {
        if (this.patterns.getSlots() >= EXTENDED_PATTERN_SLOTS) {
            return;
        }
        final AppEngInternalInventory old = this.patterns;
        final AppEngInternalInventory neu = new AppEngInternalInventory(
                (IAEAppEngInventory) (Object) this, EXTENDED_PATTERN_SLOTS);
        final int toCopy = Math.min(old.getSlots(), EXTENDED_PATTERN_SLOTS);
        for (int i = 0; i < toCopy; i++) {
            final ItemStack stack = old.getStackInSlot(i);
            if (!stack.isEmpty()) {
                neu.setStackInSlot(i, stack);
            }
        }
        this.patterns = neu;
        this.updateCraftingList();
    }

    @ModifyVariable(
            method = "updateCraftingList",
            remap = false,
            at = @At(value = "STORE")
    )
    private Boolean[] ae2retrofit$expandAccountedFor(Boolean[] original) {
        final int needed = this.patterns.getSlots();
        if (needed <= original.length) {
            return original;
        }
        final Boolean[] result = new Boolean[needed];
        Arrays.fill(result, Boolean.FALSE);
        return result;
    }

    @Inject(
            method = "addDrops",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void ae2retrofit$suppressDropsDuringUpgradeSwap(List<ItemStack> drops, CallbackInfo ci) {
        if (UpgradeSwapContext.isSwapInProgress()) {
            ci.cancel();
        }
    }
}