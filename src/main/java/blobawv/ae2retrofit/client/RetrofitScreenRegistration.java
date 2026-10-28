package blobawv.ae2retrofit.client;

import java.io.FileNotFoundException;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.container.AEBaseContainer;

import blobawv.ae2retrofit.ModCompat;
import blobawv.ae2retrofit.container.ExtendedInterfaceContainer;

@OnlyIn(Dist.CLIENT)
public final class RetrofitScreenRegistration {

    private RetrofitScreenRegistration() {}

    public static void register() {
        register(ExtendedInterfaceContainer.TYPE,
                ExtendedInterfaceScreen<ExtendedInterfaceContainer>::new,
                "/screens/extended_interface.json");

        if (ModCompat.FLUIDCRAFT_PRESENT) {
            registerDual();
        }
    }

    private static void registerDual() {
        register(blobawv.ae2retrofit.container.ExtendedItemDualInterfaceContainer.TYPE,
                GuiExtendedItemDualInterface::new,
                "/screens/extended_dual_item_interface.json");
    }

    @FunctionalInterface
    public interface StyledScreenFactory<M extends AEBaseContainer, U extends AEBaseScreen<M>> {
        U create(M container, PlayerInventory inv, ITextComponent title, ScreenStyle style);
    }

    private static <M extends AEBaseContainer, U extends AEBaseScreen<M>> void register(
            ContainerType<M> type, StyledScreenFactory<M, U> factory, String stylePath) {
        ScreenManager.<M, U>registerFactory(type, (container, playerInv, title) -> {
            ScreenStyle style;
            try {
                style = StyleManager.loadStyleDoc(stylePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath + ": " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath, e);
            }
            return factory.create(container, playerInv, title, style);
        });
    }
}