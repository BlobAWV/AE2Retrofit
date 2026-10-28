package blobawv.ae2retrofit.tile;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.GridFlags;
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
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.InvOperation;

import blobawv.ae2retrofit.common.ExtendedDualRegistryHandler;
import com.glodblock.github.common.me.DualityDualInterface;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import blobawv.ae2retrofit.container.ExtendedItemDualInterfaceContainer;
import blobawv.ae2retrofit.util.ExtendedInterfaceLogic;

public class TileExtendedDualInterface extends AENetworkInvTileEntity
        implements IGridTickable, IInventoryDestination, IInterfaceHost, IPriorityHost, IFluidInterfaceHost {

    public static TileEntityType<TileExtendedDualInterface> TYPE;

    private final DualityDualInterface<TileExtendedDualInterface> duality =
            new DualityDualInterface<>(getProxy(), this);
    private boolean omniDirectional = true;

    public TileExtendedDualInterface(TileEntityType<?> type) {
        super(type);
        getProxy().setIdlePowerUsage(4D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        ((ExtendedInterfaceLogic) duality.getItemInterface()).extend();
    }

    @MENetworkEventSubscribe public void stateChange(MENetworkChannelsChanged c)  { duality.onChannelStateChange(c); }
    @MENetworkEventSubscribe public void stateChange(MENetworkPowerStatusChange c) { duality.onPowerStateChange(c); }

    public void setSide(final Direction facing) {
        if (Platform.isClient()) return;
        Direction newForward;
        if (!this.omniDirectional && this.getForward() == facing.getOpposite()) {
            newForward = facing;
        } else if (!this.omniDirectional
                && (this.getForward() == facing || this.getForward() == facing.getOpposite())) {
            newForward = facing;
            this.omniDirectional = true;
        } else if (this.omniDirectional) {
            newForward = facing.getOpposite();
            this.omniDirectional = false;
        } else {
            newForward = Platform.rotateAround(this.getForward(), facing);
        }
        if (this.omniDirectional) {
            this.setOrientation(Direction.NORTH, Direction.UP);
        } else {
            Direction newUp = (newForward == Direction.UP || newForward == Direction.DOWN)
                    ? Direction.NORTH : Direction.UP;
            this.setOrientation(newForward, newUp);
        }
        this.configureNodeSides();
        this.markForUpdate();
        this.saveChanges();
    }

    private void configureNodeSides() {
        if (this.omniDirectional) {
            this.getProxy().setValidSides(EnumSet.allOf(Direction.class));
        } else {
            this.getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        }
    }

    @Override public void getDrops(World w, BlockPos pos, List<ItemStack> drops) { duality.addDrops(drops); }
    @Override public void gridChanged() { duality.onGridChanged(); }

    @Override
    public void onReady() {
        this.configureNodeSides();
        super.onReady();
        duality.initialize();
    }

    @Override
    public CompoundNBT write(CompoundNBT data) {
        super.write(data);
        data.putBoolean("omniDirectional", this.omniDirectional);
        duality.writeToNBT(data);
        return data;
    }

    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        super.read(blockState, data);
        this.omniDirectional = data.getBoolean("omniDirectional");
        duality.readFromNBT(data);
        ((ExtendedInterfaceLogic) duality.getItemInterface()).extend();
    }

    @Override
    protected boolean readFromStream(PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final boolean oldOmni = this.omniDirectional;
        this.omniDirectional = data.readBoolean();
        return oldOmni != this.omniDirectional || c;
    }

    @Override
    protected void writeToStream(PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.omniDirectional);
    }

    @Nonnull @Override public AECableType getCableConnectionType(@Nonnull AEPartLocation dir) { return AECableType.SMART; }
    @Override public DimensionalCoord getLocation() { return new DimensionalCoord(this); }
    @Override public boolean canInsert(ItemStack stack) { return duality.canInsertItem(stack); }
    @Override public IItemHandler getInventoryByName(String name) { return duality.getItemInventoryByName(name); }
    @Nonnull @Override public TickingRequest getTickingRequest(@Nonnull IGridNode node) { return duality.getTickingRequest(node); }
    @Nonnull @Override public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticks) { return duality.onTick(node, ticks); }
    @Nonnull @Override public IItemHandler getInternalInventory() { return duality.getInternalItemInventory(); }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        duality.onItemInventoryChange(inv, slot, mc, removed, added);
    }

    @Override public DualityInterface getInterfaceDuality() { return duality.getItemInterface(); }
    @Override public DualityFluidInterface getDualityFluidInterface() { return duality.getFluidInterface(); }
    @Override public EnumSet<Direction> getTargets() { return this.omniDirectional ? EnumSet.allOf(Direction.class) : EnumSet.of(this.getForward()); }
    @Override public TileEntity getTileEntity() { return this; }
    @Override public IConfigManager getConfigManager() { return duality.getConfigManager(); }
    @Override public boolean pushPattern(ICraftingPatternDetails d, CraftingInventory t) { return duality.pushPattern(d, t); }
    @Override public boolean isBusy() { return duality.isCraftingBusy(); }
    @Override public void provideCrafting(ICraftingProviderHelper tracker) { duality.provideCrafting(tracker); }
    @Override public int getInstalledUpgrades(Upgrades u) { return duality.getInstalledUpgrades(u); }
    @Override public ImmutableSet<ICraftingLink> getRequestedJobs() { return duality.getRequestCraftingJobs(); }
    @Override public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) { return duality.injectCraftedItems(link, items, mode); }
    @Override public void jobStateChange(ICraftingLink link) { duality.onCraftingJobStateChange(link); }
    @Override public int getPriority() { return duality.getPriority(); }
    @Override public void setPriority(int newValue) { duality.setPriority(newValue); }
    public boolean isOmniDirectional() { return this.omniDirectional; }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        LazyOptional<T> capInst = duality.getCapability(cap, facing);
        return capInst.isPresent() ? capInst : super.getCapability(cap, facing);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(ExtendedDualRegistryHandler.DUAL_BLOCK);
    }

    @Override
    public ContainerType<?> getContainerType() {
        return ExtendedItemDualInterfaceContainer.TYPE;
    }

    @Override
    public CompoundNBT downloadSettings(SettingsFrom from) {
        CompoundNBT pre = super.downloadSettings(from);
        CompoundNBT tag = pre == null ? new CompoundNBT() : pre;
        tag.put("pattern", this.duality.downloadSettings(from));
        return tag.isEmpty() ? null : tag;
    }

    @Override
    public void uploadSettings(SettingsFrom from, CompoundNBT compound) {
        super.uploadSettings(from, compound);
        if (compound.contains("pattern")) {
            this.duality.uploadSettings(from, compound.getCompound("pattern"));
        }
    }
}