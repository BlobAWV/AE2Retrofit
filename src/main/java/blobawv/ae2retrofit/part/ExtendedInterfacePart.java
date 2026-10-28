package blobawv.ae2retrofit.part;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.parts.misc.InterfacePart;

import blobawv.ae2retrofit.common.ExtendedRegistryHandler;
import blobawv.ae2retrofit.container.ExtendedInterfaceContainer;
import blobawv.ae2retrofit.util.ExtendedInterfaceLogic;

public class ExtendedInterfacePart extends InterfacePart {

    @PartModels
    public static ResourceLocation[] MODELS = new ResourceLocation[] {
            new ResourceLocation("ae2retrofit", "part/extended_interface_base"),
            new ResourceLocation("ae2retrofit", "part/extended_interface_on"),
            new ResourceLocation("ae2retrofit", "part/extended_interface_off"),
            new ResourceLocation("ae2retrofit", "part/extended_interface_has_channel")
    };

    public static final PartModel MODELS_OFF = new PartModel(MODELS[0], MODELS[2]);
    public static final PartModel MODELS_ON = new PartModel(MODELS[0], MODELS[1]);
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODELS[0], MODELS[3]);

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) return MODELS_HAS_CHANNEL;
        if (this.isPowered()) return MODELS_ON;
        return MODELS_OFF;
    }

    public ExtendedInterfacePart(ItemStack is) {
        super(is);
        ((ExtendedInterfaceLogic) this.getInterfaceDuality()).extend();
    }

    @Override
    public void readFromNBT(CompoundNBT data) {
        super.readFromNBT(data);
        ((ExtendedInterfaceLogic) this.getInterfaceDuality()).extend();
    }

    @Override
    public boolean onPartActivate(PlayerEntity p, Hand hand, Vector3d pos) {
        if (!p.getEntityWorld().isRemote()) {
            ContainerOpener.openContainer(ExtendedInterfaceContainer.TYPE, p, ContainerLocator.forPart(this));
        }
        return true;
    }

    @Override
    public ContainerType<?> getContainerType() {
        return ExtendedInterfaceContainer.TYPE;
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(ExtendedRegistryHandler.PART_ITEM);
    }
}