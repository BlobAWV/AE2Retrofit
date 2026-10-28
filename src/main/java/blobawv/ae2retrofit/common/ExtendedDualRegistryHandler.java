package blobawv.ae2retrofit.common;

import blobawv.ae2retrofit.AE2Retrofit;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;

import appeng.block.AEBaseBlockItem;
import appeng.api.config.Upgrades;
import appeng.core.CreativeTab;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTileEntity;
import appeng.core.Api;

import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.tile.TileDualInterface;

import blobawv.ae2retrofit.block.ExtendedDualInterfaceBlock;
import blobawv.ae2retrofit.container.ExtendedItemDualInterfaceContainer;
import blobawv.ae2retrofit.item.ItemExtendedDualInterfacePart;
import blobawv.ae2retrofit.item.ItemExtendedInterfaceUpgrade;
import blobawv.ae2retrofit.tile.TileExtendedDualInterface;
import blobawv.ae2retrofit.part.PartExtendedDualInterface;

public final class ExtendedDualRegistryHandler {

    private ExtendedDualRegistryHandler() {}

    public static ExtendedDualInterfaceBlock DUAL_BLOCK;
    public static Item DUAL_ITEM;
    public static TileEntityType<TileExtendedDualInterface> DUAL_TILE_TYPE;
    public static Item DUAL_PART_ITEM;

    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        DUAL_BLOCK = new ExtendedDualInterfaceBlock();
        DUAL_BLOCK.setRegistryName("ae2retrofit", "extended_dual_interface");
        event.getRegistry().register(DUAL_BLOCK);
    }

    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        DUAL_ITEM = new AEBaseBlockItem(DUAL_BLOCK, new Item.Properties().group(AE2Retrofit.ITEM_GROUP));
        DUAL_ITEM.setRegistryName("ae2retrofit", "extended_dual_interface");
        event.getRegistry().register(DUAL_ITEM);

        DUAL_PART_ITEM = new ItemExtendedDualInterfacePart(new Item.Properties().group(AE2Retrofit.ITEM_GROUP));
        DUAL_PART_ITEM.setRegistryName("ae2retrofit", "extended_dual_interface_part");
        event.getRegistry().register(DUAL_PART_ITEM);

        Api.instance().registries().partModels().registerModels(PartExtendedDualInterface.MODELS);
    }

    public static void onRegisterTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        DUAL_TILE_TYPE = TileEntityType.Builder
                .create(() -> new TileExtendedDualInterface(TileExtendedDualInterface.TYPE), DUAL_BLOCK)
                .build(null);
        DUAL_TILE_TYPE.setRegistryName("ae2retrofit", "extended_dual_interface");
        event.getRegistry().register(DUAL_TILE_TYPE);
        TileExtendedDualInterface.TYPE = DUAL_TILE_TYPE;
        DUAL_BLOCK.setTileEntity(TileExtendedDualInterface.class, () -> new TileExtendedDualInterface(DUAL_TILE_TYPE));
    }

    public static void onRegisterContainers(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(ExtendedItemDualInterfaceContainer.TYPE);
    }

    public static void addUpgradeTargets(ItemExtendedInterfaceUpgrade item) {
        item.addReplacement(TileDualInterface.class, DUAL_BLOCK)
                .addPartReplacement(PartDualInterface.class, DUAL_PART_ITEM);
    }

    public static void onCommonSetup() {
        AEBaseTileEntity.registerTileItem(TileExtendedDualInterface.class,
                new BlockStackSrc(DUAL_BLOCK, ActivityState.Enabled));
        Upgrades.CRAFTING.registerItem(DUAL_BLOCK, 1);
        Upgrades.CRAFTING.registerItem(DUAL_PART_ITEM, 1);
    }
}