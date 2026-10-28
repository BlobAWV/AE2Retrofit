package blobawv.ae2retrofit.mixins;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import blobawv.ae2retrofit.ModCompat;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.core.sync.BasePacket;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.parts.misc.InterfacePart;
import appeng.tile.misc.InterfaceTileEntity;


import blobawv.ae2retrofit.part.ExtendedInterfacePart;
import blobawv.ae2retrofit.tile.ExtendedInterfaceTileEntity;

@Mixin(InterfaceTerminalContainer.class)
public abstract class MixinInterfaceTerminalContainer {

    @Shadow @Final @SuppressWarnings("rawtypes") private Map diList;
    private static java.lang.reflect.Constructor<?> ae2retrofit$invTrackerCtor;

    @Redirect(
            method = {"visitInterfaceHosts", "sendFullUpdate"},
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;"
            ),
            remap = false
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private IMachineSet ae2retrofit$mergeExtendedMachines(IGrid grid, Class machineClass) {
        final IMachineSet base = grid.getMachines(machineClass);
        final Class<? extends IGridHost> extendedClass = extendedFor(machineClass);
        if (extendedClass == null) {
            return base;
        }
        return new MergedMachineSet(base, grid.getMachines(extendedClass));
    }

    @Inject(
            method = "sendFullUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;",
                    ordinal = 0
            ),
            remap = false
    )
    private void ae2retrofit$addExtendedDual(IGrid grid, Consumer<BasePacket> packetSender, CallbackInfo ci) {
        if (!ModCompat.FLUIDCRAFT_PRESENT || grid == null) {
            return;
        }
        final Class<? extends IGridHost> blockClass = loadClass("blobawv.ae2retrofit.tile.TileExtendedDualInterface");
        final Class<? extends IGridHost> partClass = loadClass("blobawv.ae2retrofit.part.PartExtendedDualInterface");
        if (blockClass != null) {
            for (IGridNode gn : grid.getMachines(blockClass)) {
                ae2retrofit$addIfInterfaceTerminal(gn);
            }
        }
        if (partClass != null) {
            for (IGridNode gn : grid.getMachines(partClass)) {
                ae2retrofit$addIfInterfaceTerminal(gn);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void ae2retrofit$addIfInterfaceTerminal(IGridNode gn) {
        if (!gn.isActive()) {
            return;
        }
        final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
        final DualityInterface dual = ih.getInterfaceDuality();
        if (dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
            this.diList.put(ih, ae2retrofit$createInvTracker(dual));
        }
    }

    private static Object ae2retrofit$createInvTracker(DualityInterface dual) {
        try {
            if (ae2retrofit$invTrackerCtor == null) {
                final Class<?> clazz = Class.forName(
                        "appeng.container.implementations.InterfaceTerminalContainer$InvTracker");
                ae2retrofit$invTrackerCtor = clazz.getDeclaredConstructor(
                        DualityInterface.class,
                        net.minecraftforge.items.IItemHandler.class,
                        net.minecraft.util.text.ITextComponent.class);
                ae2retrofit$invTrackerCtor.setAccessible(true);
            }
            return ae2retrofit$invTrackerCtor.newInstance(dual, dual.getPatterns(), dual.getTermName());
        } catch (Exception e) {
            throw new RuntimeException("ae2retrofit: failed to construct InvTracker", e);
        }
    }

    @Inject(method = "addItems", at = @At("RETURN"), remap = false)
    private void ae2retrofit$addSlotCount(CompoundNBT tag, @Coerce Object inv, int offset, int length,
                                          CallbackInfo ci) {
        if (tag.contains("slotCount")) return;
        if (!(inv instanceof MixinInvTrackerAccessor)) return;
        try {
            final IItemHandler server = ((MixinInvTrackerAccessor) inv).ae2retrofit$getServer();
            tag.putInt("slotCount", server.getSlots());
        } catch (Throwable ignored) {
        }
    }

    private static Class<? extends IGridHost> extendedFor(Class<?> vanilla) {
        if (vanilla == InterfaceTileEntity.class) return ExtendedInterfaceTileEntity.class;
        if (vanilla == InterfacePart.class)       return ExtendedInterfacePart.class;
        if (!ModCompat.FLUIDCRAFT_PRESENT)        return null;
        // Compare by name to avoid a compile-time link to AE2FC classes.
        final String name = vanilla.getName();
        if ("com.glodblock.github.common.tile.TileDualInterface".equals(name)) {
            return loadClass("blobawv.ae2retrofit.tile.TileExtendedDualInterface");
        }
        if ("com.glodblock.github.common.part.PartDualInterface".equals(name)) {
            return loadClass("blobawv.ae2retrofit.part.PartExtendedDualInterface");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends IGridHost> loadClass(String className) {
        try {
            return (Class<? extends IGridHost>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static final class MergedMachineSet extends AbstractSet<IGridNode> implements IMachineSet {
        private final List<IGridNode> all;
        private final Class<? extends IGridHost> machine;

        MergedMachineSet(IMachineSet a, IMachineSet b) {
            this.all = new ArrayList<>(a.size() + b.size());
            for (IGridNode n : a) this.all.add(n);
            for (IGridNode n : b) this.all.add(n);
            this.machine = a.getMachineClass();
        }

        @Nonnull @Override
        public Iterator<IGridNode> iterator() { return Collections.unmodifiableList(this.all).iterator(); }

        @Override
        public int size() { return this.all.size(); }

        @Nonnull @Override
        public Class<? extends IGridHost> getMachineClass() { return this.machine; }
    }
}