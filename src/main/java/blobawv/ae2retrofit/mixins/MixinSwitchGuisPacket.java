package blobawv.ae2retrofit.mixins;

import net.minecraft.inventory.container.ContainerType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import appeng.core.sync.packets.SwitchGuisPacket;

import blobawv.ae2retrofit.ModCompat;

@Mixin(SwitchGuisPacket.class)
public abstract class MixinSwitchGuisPacket {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/inventory/container/ContainerType;)V",
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    private static ContainerType<?> ae2retrofit$redirectExtendedDualSwitch(ContainerType<?> original) {
        if (!ModCompat.FLUIDCRAFT_PRESENT) {
            return original;
        }
        // All AE2FC-touching logic lives in SwitchGuisHelper (client-only, loaded lazily).
        return blobawv.ae2retrofit.client.SwitchGuisHelper.redirectSwitchGui(original);
    }
}