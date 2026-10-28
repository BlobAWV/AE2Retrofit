package blobawv.ae2retrofit.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.api.parts.IPartItem;
import appeng.core.Api;

import blobawv.ae2retrofit.part.PartExtendedDualInterface;

public class ItemExtendedDualInterfacePart extends Item implements IPartItem<PartExtendedDualInterface> {

    public ItemExtendedDualInterfacePart(Properties props) { super(props); }

    @Nullable
    @Override
    public PartExtendedDualInterface createPart(ItemStack is) {
        return new PartExtendedDualInterface(is);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        return Api.instance().partHelper().placeBus(
                context.getItem(), context.getPos(), context.getFace(),
                context.getPlayer(), context.getHand(), context.getWorld());
    }
}