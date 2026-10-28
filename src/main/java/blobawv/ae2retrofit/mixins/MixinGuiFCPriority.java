package blobawv.ae2retrofit.mixins;

import blobawv.ae2retrofit.common.ExtendedDualRegistryHandler;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.glodblock.github.client.GuiFCPriority;
import com.glodblock.github.client.container.ContainerFCPriority;

import blobawv.ae2retrofit.part.PartExtendedDualInterface;
import blobawv.ae2retrofit.tile.TileExtendedDualInterface;

@Mixin(GuiFCPriority.class)
public abstract class MixinGuiFCPriority {

    private static final java.lang.reflect.Field CONTAINER_FIELD = ae2retrofit$findContainerField();

    private static java.lang.reflect.Field ae2retrofit$findContainerField() {
        Class<?> c = ContainerScreen.class;
        while (c != null && c != Object.class) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                if (Container.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        throw new ExceptionInInitializerError(
                "[ae2retrofit] Could not locate a Container-typed field in the "
                        + "ContainerScreen hierarchy");
    }

    @ModifyVariable(
            method = "<init>",
            at = @At(value = "STORE"),
            ordinal = 0,
            remap = false
    )
    private ItemStack ae2retrofit$modifyDualIcon(ItemStack original) {
        final Object c;
        try {
            c = CONTAINER_FIELD.get(this);
        } catch (IllegalAccessException e) {
            return original;
        }
        if (!(c instanceof ContainerFCPriority)) {
            return original;
        }
        final Object host = ((ContainerFCPriority) c).getPriorityHost();
        if (host instanceof TileExtendedDualInterface) {
            return new ItemStack(ExtendedDualRegistryHandler.DUAL_ITEM);
        }
        if (host instanceof PartExtendedDualInterface) {
            return new ItemStack(ExtendedDualRegistryHandler.DUAL_PART_ITEM);
        }
        return original;
    }
}