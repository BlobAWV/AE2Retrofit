package blobawv.ae2retrofit.tile;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

import appeng.tile.misc.InterfaceTileEntity;

import blobawv.ae2retrofit.common.ExtendedRegistryHandler;
import blobawv.ae2retrofit.container.ExtendedInterfaceContainer;
import blobawv.ae2retrofit.util.ExtendedInterfaceLogic;

public class ExtendedInterfaceTileEntity extends InterfaceTileEntity {

    public static TileEntityType<ExtendedInterfaceTileEntity> TYPE;

    public ExtendedInterfaceTileEntity(TileEntityType<?> type) {
        super(type);
        ((ExtendedInterfaceLogic) this.getInterfaceDuality()).extend();
    }

    @Override
    public ContainerType<?> getContainerType() {
        return ExtendedInterfaceContainer.TYPE;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(ExtendedRegistryHandler.ITEM);
    }

    @Override
    public void read(BlockState blockState, CompoundNBT data) {
        super.read(blockState, data);
        ((ExtendedInterfaceLogic) this.getInterfaceDuality()).extend();
    }
}