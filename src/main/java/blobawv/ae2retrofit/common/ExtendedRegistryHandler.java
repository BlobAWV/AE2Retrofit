package blobawv.ae2retrofit.common;

import appeng.core.Api;
import blobawv.ae2retrofit.AE2Retrofit;
import blobawv.ae2retrofit.part.ExtendedInterfacePart;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import appeng.api.config.Upgrades;
import appeng.block.AEBaseBlockItem;
import appeng.core.CreativeTab;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTileEntity;
import appeng.tile.misc.InterfaceTileEntity;
import appeng.parts.misc.InterfacePart;

import blobawv.ae2retrofit.ModCompat;
import blobawv.ae2retrofit.block.ExtendedInterfaceBlock;
import blobawv.ae2retrofit.container.ExtendedInterfaceContainer;
import blobawv.ae2retrofit.item.ItemExtendedInterfacePart;
import blobawv.ae2retrofit.item.ItemExtendedInterfaceUpgrade;
import blobawv.ae2retrofit.tile.ExtendedInterfaceTileEntity;

public class ExtendedRegistryHandler {

    public static ExtendedInterfaceBlock BLOCK;
    public static Item ITEM;
    public static TileEntityType<ExtendedInterfaceTileEntity> TILE_TYPE;
    public static Item UPGRADE_ITEM;
    public static ItemExtendedInterfacePart PART_ITEM;

    @SubscribeEvent
    public void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        BLOCK = new ExtendedInterfaceBlock();
        BLOCK.setRegistryName("ae2retrofit", "extended_interface");
        event.getRegistry().register(BLOCK);

        if (ModCompat.FLUIDCRAFT_PRESENT) {
            ExtendedDualRegistryHandler.onRegisterBlocks(event);
        }
    }

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> event) {
        ITEM = new AEBaseBlockItem(BLOCK, new Item.Properties().group(AE2Retrofit.ITEM_GROUP));
        ITEM.setRegistryName("ae2retrofit", "extended_interface");
        event.getRegistry().register(ITEM);

        PART_ITEM = new ItemExtendedInterfacePart(new Item.Properties().group(AE2Retrofit.ITEM_GROUP));
        PART_ITEM.setRegistryName("ae2retrofit", "extended_interface_part");
        event.getRegistry().register(PART_ITEM);

        if (ModCompat.FLUIDCRAFT_PRESENT) {
            ExtendedDualRegistryHandler.onRegisterItems(event);
        }

        UPGRADE_ITEM = new ItemExtendedInterfaceUpgrade(new Item.Properties().group(AE2Retrofit.ITEM_GROUP))
                .addReplacement(InterfaceTileEntity.class, BLOCK)
                .addPartReplacement(InterfacePart.class, PART_ITEM);
        if (ModCompat.FLUIDCRAFT_PRESENT) {
            ExtendedDualRegistryHandler.addUpgradeTargets((ItemExtendedInterfaceUpgrade) UPGRADE_ITEM);
        }
        UPGRADE_ITEM.setRegistryName("ae2retrofit", "extended_interface_upgrade");
        event.getRegistry().register(UPGRADE_ITEM);

        Api.instance().registries().partModels().registerModels(ExtendedInterfacePart.MODELS);

    }

    @SubscribeEvent
    public void onRegisterTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        TILE_TYPE = TileEntityType.Builder
                .create(() -> new ExtendedInterfaceTileEntity(ExtendedInterfaceTileEntity.TYPE), BLOCK)
                .build(null);
        TILE_TYPE.setRegistryName("ae2retrofit", "extended_interface");
        event.getRegistry().register(TILE_TYPE);
        ExtendedInterfaceTileEntity.TYPE = TILE_TYPE;
        BLOCK.setTileEntity(ExtendedInterfaceTileEntity.class, () -> new ExtendedInterfaceTileEntity(TILE_TYPE));

        if (ModCompat.FLUIDCRAFT_PRESENT) {
            ExtendedDualRegistryHandler.onRegisterTileEntities(event);
        }
    }

    @SubscribeEvent
    public void onRegisterContainers(RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(ExtendedInterfaceContainer.TYPE);

        if (ModCompat.FLUIDCRAFT_PRESENT) {
            ExtendedDualRegistryHandler.onRegisterContainers(event);
        }
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        AEBaseTileEntity.registerTileItem(ExtendedInterfaceTileEntity.class,
                new BlockStackSrc(BLOCK, ActivityState.Enabled));
        Upgrades.CRAFTING.registerItem(BLOCK, 1);
        Upgrades.CRAFTING.registerItem(PART_ITEM, 1);

        if (ModCompat.FLUIDCRAFT_PRESENT) {
            ExtendedDualRegistryHandler.onCommonSetup();
        }
    }
}