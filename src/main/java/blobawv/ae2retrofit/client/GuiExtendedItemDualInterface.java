package blobawv.ae2retrofit.client;

import appeng.client.gui.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.TabButton;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;

import com.glodblock.github.client.button.BlitMap;
import com.glodblock.github.client.button.FCToggleButton;
import com.glodblock.github.client.container.ContainerFluidDualInterface;
import com.glodblock.github.client.container.ContainerFCPriority;
import com.glodblock.github.network.NetworkManager;
import com.glodblock.github.network.packets.CPacketFluidCraftBtns;

import blobawv.ae2retrofit.container.ExtendedItemDualInterfaceContainer;

public class GuiExtendedItemDualInterface extends ExtendedInterfaceScreen<ExtendedItemDualInterfaceContainer> {

    private final FCToggleButton fluidPacketBtn;
    private final FCToggleButton splittingBtn;
    private final FCToggleButton blockingBtn;

    public GuiExtendedItemDualInterface(ExtendedItemDualInterfaceContainer container, PlayerInventory inv,
                                        ITextComponent title, ScreenStyle style) {
        super(container, inv, title, style);

        ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();

        // Fluid switch tab
        ItemStack fluidIcon = Api.instance().definitions().blocks().fluidIface().maybeStack(1).orElse(ItemStack.EMPTY);
        this.widgets.add("switchInterface", new TabButton(fluidIcon, fluidIcon.getDisplayName(), itemRender, btn ->
                NetworkHandler.instance().sendToServer(new SwitchGuisPacket(ContainerFluidDualInterface.TYPE))));

        // Priority tab (replaces addOpenPriorityButton)
        this.widgets.add("priorityBtn", new TabButton(Icon.WRENCH, GuiText.Priority.text(), itemRender, btn ->
                NetworkHandler.instance().sendToServer(new SwitchGuisPacket(ContainerFCPriority.TYPE))));

        // Fluid config toggles
        this.fluidPacketBtn = new FCToggleButton(btn -> {
            FCToggleButton b = (FCToggleButton) btn;
            NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("fluidPacket", b.getActive() == 1));
        }, BlitMap.SEND_FLUID, BlitMap.SEND_PACKET);
        this.addToLeftToolbar(this.fluidPacketBtn);

        this.splittingBtn = new FCToggleButton(btn -> {
            FCToggleButton b = (FCToggleButton) btn;
            NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("allowSplitting", b.getActive() == 1));
        }, BlitMap.NOT_SPLITTING, BlitMap.SPLITTING);
        this.addToLeftToolbar(this.splittingBtn);

        this.blockingBtn = new FCToggleButton(btn -> {
            FCToggleButton b = (FCToggleButton) btn;
            NetworkManager.netHandler.sendToServer(new CPacketFluidCraftBtns("blockModeEx", b.getActive()));
        }, BlitMap.BLOCK_ALL, BlitMap.BLOCK_ITEM, BlitMap.BLOCK_FLUID);
        this.addToLeftToolbar(this.blockingBtn);
    }

    @Override
    protected boolean shouldAddOpenPriorityWidget() {
        return false;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.fluidPacketBtn.forceActive(this.container.fluidPacket ? 1 : 0);
        this.splittingBtn.forceActive(this.container.allowSplitting ? 1 : 0);
        this.blockingBtn.forceActive(this.container.blockModeEx);
        this.blockingBtn.visible = this.container.getBlockingMode() == appeng.api.config.YesNo.YES;
    }
}