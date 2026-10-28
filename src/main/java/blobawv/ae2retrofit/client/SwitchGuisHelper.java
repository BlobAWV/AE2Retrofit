package blobawv.ae2retrofit.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.gui.AEBaseScreen;
import appeng.container.AEBaseContainer;

import com.glodblock.github.client.container.ContainerFluidDualInterface;
import com.glodblock.github.client.container.ContainerItemDualInterface;

import blobawv.ae2retrofit.container.ExtendedItemDualInterfaceContainer;
import blobawv.ae2retrofit.part.PartExtendedDualInterface;
import blobawv.ae2retrofit.tile.TileExtendedDualInterface;

@OnlyIn(Dist.CLIENT)
public final class SwitchGuisHelper {
    private SwitchGuisHelper() {}

    public static ContainerType<?> redirectSwitchGui(ContainerType<?> original) {
        if (original != ContainerItemDualInterface.TYPE) return original;
        final Screen current = Minecraft.getInstance().currentScreen;
        if (!(current instanceof AEBaseScreen)) return original;
        final Container c = ((AEBaseScreen<?>) current).getContainer();
        if (!(c instanceof ContainerFluidDualInterface)) return original;
        if (!(c instanceof AEBaseContainer)) return original;
        final Object target = ((AEBaseContainer) c).getTarget();
        if (target instanceof TileExtendedDualInterface || target instanceof PartExtendedDualInterface) {
            return ExtendedItemDualInterfaceContainer.TYPE;
        }
        return original;
    }
}