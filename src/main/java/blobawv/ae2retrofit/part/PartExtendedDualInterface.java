package blobawv.ae2retrofit.part;

import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import blobawv.ae2retrofit.common.ExtendedDualRegistryHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.parts.BasicStatePart;
import appeng.util.Platform;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;

import com.glodblock.github.common.me.DualityDualInterface;
import com.google.common.collect.ImmutableSet;

import blobawv.ae2retrofit.container.ExtendedItemDualInterfaceContainer;
import blobawv.ae2retrofit.util.ExtendedInterfaceLogic;

public class PartExtendedDualInterface extends BasicStatePart implements IGridTickable, IInventoryDestination,
        IInterfaceHost, IAEAppEngInventory, IPriorityHost, IFluidInterfaceHost {

    @PartModels
    public static ResourceLocation[] MODELS = new ResourceLocation[] {
            new ResourceLocation("ae2retrofit", "part/extended_dual_interface_base"),
            new ResourceLocation("ae2retrofit", "part/extended_dual_interface_on"),
            new ResourceLocation("ae2retrofit", "part/extended_dual_interface_off"),
            new ResourceLocation("ae2retrofit", "part/extended_dual_interface_has_channel")
    };

    public static final PartModel MODELS_OFF = new PartModel(MODELS[0], MODELS[2]);
    public static final PartModel MODELS_ON = new PartModel(MODELS[0], MODELS[1]);
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODELS[0], MODELS[3]);

    private final DualityDualInterface<PartExtendedDualInterface> duality =
            new DualityDualInterface<>(getProxy(), this);

    public PartExtendedDualInterface(final ItemStack is) {
        super(is);
        ((ExtendedInterfaceLogic) duality.getItemInterface()).extend();
    }

    @MENetworkEventSubscribe public void stateChange(MENetworkChannelsChanged c)  { duality.onChannelStateChange(c); }
    @MENetworkEventSubscribe public void stateChange(MENetworkPowerStatusChange c) { duality.onPowerStateChange(c); }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override public int getInstalledUpgrades(Upgrades u) { return duality.getInstalledUpgrades(u); }
    @Override public void gridChanged() { duality.onGridChanged(); }

    @Override
    public void readFromNBT(CompoundNBT data) {
        super.readFromNBT(data);
        duality.readFromNBT(data);
        ((ExtendedInterfaceLogic) duality.getItemInterface()).extend();
    }

    @Override public void writeToNBT(CompoundNBT data) { super.writeToNBT(data); duality.writeToNBT(data); }

    @Override
    public void addToWorld() {
        super.addToWorld();
        duality.initialize();
    }

    @Override public void getDrops(List<ItemStack> drops, boolean wrenched) { duality.addDrops(drops); }
    @Override public float getCableConnectionLength(AECableType cable) { return 4; }
    @Override public IConfigManager getConfigManager() { return duality.getConfigManager(); }
    @Override public IItemHandler getInventoryByName(String name) { return duality.getItemInventoryByName(name); }

    @Override
    public boolean onPartActivate(PlayerEntity player, Hand hand, Vector3d pos) {
        if (Platform.isServer()) {
            ContainerOpener.openContainer(ExtendedItemDualInterfaceContainer.TYPE, player, ContainerLocator.forPart(this));
        }
        return true;
    }

    @Override public boolean canInsert(ItemStack stack) { return duality.canInsertItem(stack); }
    @Nonnull @Override public TickingRequest getTickingRequest(@Nonnull IGridNode node) { return duality.getTickingRequest(node); }
    @Nonnull @Override public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticks) { return duality.onTick(node, ticks); }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        duality.onItemInventoryChange(inv, slot, mc, removed, added);
    }

    @Override public DualityInterface getInterfaceDuality() { return duality.getItemInterface(); }
    @Override public DualityFluidInterface getDualityFluidInterface() { return duality.getFluidInterface(); }
    @Override public EnumSet<Direction> getTargets() { return EnumSet.of(this.getSide().getFacing()); }
    @Override public TileEntity getTileEntity() { return super.getHost().getTile(); }
    @Override public boolean pushPattern(ICraftingPatternDetails d, CraftingInventory t) { return duality.pushPattern(d, t); }
    @Override public boolean isBusy() { return duality.isCraftingBusy(); }
    @Override public void provideCrafting(ICraftingProviderHelper tracker) { duality.provideCrafting(tracker); }
    @Override public ImmutableSet<ICraftingLink> getRequestedJobs() { return duality.getRequestCraftingJobs(); }
    @Override public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) { return duality.injectCraftedItems(link, items, mode); }
    @Override public void jobStateChange(ICraftingLink link) { duality.onCraftingJobStateChange(link); }
    @Override public int getPriority() { return duality.getPriority(); }
    @Override public void setPriority(int newValue) { duality.setPriority(newValue); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        return duality.getCapability(cap, getSide().getFacing());
    }

    @Override public ItemStack getItemStackRepresentation() { return new ItemStack(ExtendedDualRegistryHandler.DUAL_PART_ITEM); }
    @Override public ContainerType<?> getContainerType() { return ExtendedItemDualInterfaceContainer.TYPE; }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) return MODELS_HAS_CHANNEL;
        if (this.isPowered()) return MODELS_ON;
        return MODELS_OFF;
    }
}