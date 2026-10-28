package blobawv.ae2retrofit.mixins;

import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.container.implementations.InterfaceTerminalContainer$InvTracker")
public interface MixinInvTrackerAccessor {
    @Accessor("server")
    IItemHandler ae2retrofit$getServer();
}