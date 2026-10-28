package blobawv.ae2retrofit.client;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.Icon;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigButtonPacket;

import blobawv.ae2retrofit.container.ExtendedInterfaceContainer;

public class ExtendedInterfaceScreen<T extends ExtendedInterfaceContainer> extends UpgradeableScreen<T> {

    private final SettingToggleButton<YesNo> blockMode;
    private final ToggleButton interfaceMode;

    public ExtendedInterfaceScreen(T container, PlayerInventory inv, ITextComponent title, ScreenStyle style) {
        super(container, inv, title, style);

        if (this.shouldAddOpenPriorityWidget()) {
            widgets.addOpenPriorityButton();
        }

        this.blockMode = new ServerSettingToggleButton<>(Settings.BLOCK, YesNo.NO);
        this.addToLeftToolbar(this.blockMode);

        this.interfaceMode = new ToggleButton(Icon.INTERFACE_TERMINAL_SHOW, Icon.INTERFACE_TERMINAL_HIDE,
                GuiText.InterfaceTerminal.text(), GuiText.InterfaceTerminalHint.text(),
                btn -> selectNextInterfaceMode());
        this.addToLeftToolbar(this.interfaceMode);
    }

    protected boolean shouldAddOpenPriorityWidget() {
        return true;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        this.blockMode.set(this.container.getBlockingMode());
        this.interfaceMode.setState(this.container.getInterfaceTerminalMode() == YesNo.YES);
    }

    private void selectNextInterfaceMode() {
        final boolean backwards = isHandlingRightClick();
        NetworkHandler.instance().sendToServer(new ConfigButtonPacket(Settings.INTERFACE_TERMINAL, backwards));
    }
}