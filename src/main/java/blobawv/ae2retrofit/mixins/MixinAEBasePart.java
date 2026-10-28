package blobawv.ae2retrofit.mixins;

import net.minecraft.nbt.CompoundNBT;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.me.helpers.AENetworkProxy;
import appeng.parts.AEBasePart;

/**
 * Skips {@code AENetworkProxy.readFromNBT} during the extended-interface part
 * swap. {@code cable.addPart(...)} has already initialized the new part's grid
 * node via {@code addToWorld()}, so reloading it from the old part's NBT throws
 * "Loading data after part of a grid, this is invalid". The marker key is set
 * on the swap-local NBT only; regular chunk loads and client updates never
 * carry it, so their grid node loading is unaffected.
 */
@Mixin(AEBasePart.class)
public abstract class MixinAEBasePart {

    @Redirect(
            method = "readFromNBT",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/me/helpers/AENetworkProxy;readFromNBT(Lnet/minecraft/nbt/CompoundNBT;)V"
            ),
            remap = false
    )
    private void ae2retrofit$skipProxyReloadDuringSwap(AENetworkProxy proxy, CompoundNBT data) {
        if (!data.getBoolean("ae2retrofit_swap")) {
            proxy.readFromNBT(data);
        }
    }
}