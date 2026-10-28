package blobawv.ae2retrofit.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

import appeng.api.parts.IPartItem;
import appeng.core.Api;

import blobawv.ae2retrofit.part.ExtendedInterfacePart;

public class ItemExtendedInterfacePart extends Item implements IPartItem<ExtendedInterfacePart> {

    public ItemExtendedInterfacePart(Properties props) {
        super(props);
    }

    @Nullable
    @Override
    public ExtendedInterfacePart createPart(ItemStack is) {
        return new ExtendedInterfacePart(is);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        return Api.instance().partHelper().placeBus(
                context.getItem(),
                context.getPos(),
                context.getFace(),
                context.getPlayer(),
                context.getHand(),
                context.getWorld()
        );
    }
}