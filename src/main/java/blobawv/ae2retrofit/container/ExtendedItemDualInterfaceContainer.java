package blobawv.ae2retrofit.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.guisync.GuiSync;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.util.Platform;

import com.glodblock.github.coreutil.ExtendedInterface;
import com.glodblock.github.interfaces.ConfigData;
import com.glodblock.github.util.ConfigSet;

public class ExtendedItemDualInterfaceContainer extends ExtendedInterfaceContainer implements ConfigData {

    public static final ContainerType<ExtendedItemDualInterfaceContainer> TYPE = ContainerTypeBuilder
            .create(ExtendedItemDualInterfaceContainer::new, IInterfaceHost.class)
            .build("extended_dual_item_interface");

    @GuiSync(95) public boolean fluidPacket = false;
    @GuiSync(96) public boolean allowSplitting = true;
    @GuiSync(97) public int blockModeEx = 0;

    private final ConfigSet exConfig = new ConfigSet();
    private final DualityInterface dualityInterfaceCopy;

    public ExtendedItemDualInterfaceContainer(int id, PlayerInventory ip, IInterfaceHost te) {
        super(TYPE, id, ip, te);
        this.dualityInterfaceCopy = te.getInterfaceDuality();
        this.exConfig
                .addConfig("fluidPacket", v -> {
                    this.fluidPacket = (boolean) v;
                    ((ExtendedInterface) dualityInterfaceCopy).setFluidPacketMode((boolean) v);
                }, () -> this.fluidPacket)
                .addConfig("allowSplitting", v -> {
                    this.allowSplitting = (boolean) v;
                    ((ExtendedInterface) dualityInterfaceCopy).setSplittingMode((boolean) v);
                }, () -> this.allowSplitting)
                .addConfig("blockModeEx", v -> {
                    this.blockModeEx = (int) v;
                    ((ExtendedInterface) dualityInterfaceCopy).setExtendedBlockMode((int) v);
                }, () -> this.blockModeEx);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            final ExtendedInterface eif = (ExtendedInterface) dualityInterfaceCopy;
            if (fluidPacket != eif.getFluidPacketMode()) fluidPacket = eif.getFluidPacketMode();
            if (allowSplitting != eif.getSplittingMode()) allowSplitting = eif.getSplittingMode();
            if (blockModeEx != eif.getExtendedBlockMode()) blockModeEx = eif.getExtendedBlockMode();
        }
    }

    @Override public void set(String id, Object value) { this.exConfig.setConfig(id, value); }
    @Override public Object get(String id) { return this.exConfig.getConfig(id); }
}