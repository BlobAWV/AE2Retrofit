package blobawv.ae2retrofit.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.parts.IPart;
import appeng.api.parts.SelectedPart;
import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.tile.networking.CableBusTileEntity;

import blobawv.ae2retrofit.util.UpgradeSwapContext;

public class ItemExtendedInterfaceUpgrade extends Item {

    private final Map<Class<? extends TileEntity>, Block> blockReplacements = new HashMap<>();
    private final Map<Class<? extends IPart>, Item> partReplacements = new HashMap<>();

    public ItemExtendedInterfaceUpgrade(Properties properties) {
        super(properties);
    }

    public ItemExtendedInterfaceUpgrade addReplacement(Class<? extends TileEntity> sourceTE, Block targetBlock) {
        this.blockReplacements.put(sourceTE, targetBlock);
        return this;
    }

    public ItemExtendedInterfaceUpgrade addPartReplacement(Class<? extends IPart> sourcePart, Item targetItem) {
        this.partReplacements.put(sourcePart, targetItem);
        return this;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        final World world = context.getWorld();
        if (world.isRemote()) {
            return ActionResultType.SUCCESS;
        }

        final BlockPos pos = context.getPos();
        final TileEntity tile = world.getTileEntity(pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }

        if (tile instanceof CableBusTileEntity) {
            return this.tryReplacePart(context, world, (CableBusTileEntity) tile);
        }
        return this.tryReplaceBlock(context, world, pos, tile);
    }

    private ActionResultType tryReplaceBlock(ItemUseContext context, World world, BlockPos pos, TileEntity oldTE) {
        final Block targetBlock = this.blockReplacements.get(oldTE.getClass());
        if (targetBlock == null) {
            return ActionResultType.PASS;
        }

        final CompoundNBT oldNBT = oldTE.write(new CompoundNBT());
        final BlockState newState = targetBlock.getDefaultState();
        UpgradeSwapContext.run(() -> world.setBlockState(pos, newState, 3));

        final TileEntity newTE = world.getTileEntity(pos);
        if (newTE == null) {
            return ActionResultType.PASS;
        }
        newTE.read(newState, oldNBT);
        newTE.markDirty();
        if (newTE instanceof appeng.tile.AEBaseTileEntity) {
            ((appeng.tile.AEBaseTileEntity) newTE).markForUpdate();
        }

        if (targetBlock instanceof AEBaseTileBlock) {
            final BlockState syncedState = ((AEBaseTileBlock<?>) targetBlock).getTileEntityBlockState(newState, newTE);
            if (syncedState != newState) {
                world.setBlockState(pos, syncedState, 3);
            }
        }

        this.consumeIfSurvival(context);
        return ActionResultType.SUCCESS;
    }

    private ActionResultType tryReplacePart(ItemUseContext context, World world, CableBusTileEntity cable) {
        AEPartLocation targetSide = this.detectPartSideByHit(context, cable);

        if (targetSide == null) {
            targetSide = this.detectUniqueMatchingPart(cable);
        }

        if (targetSide == null) {
            return ActionResultType.PASS;
        }

        final IPart oldPart = cable.getPart(targetSide);
        if (oldPart == null) {
            return ActionResultType.PASS;
        }

        final Item targetItem = this.partReplacements.get(oldPart.getClass());
        if (targetItem == null) {
            return ActionResultType.PASS;
        }

        final CompoundNBT contents = new CompoundNBT();
        oldPart.writeToNBT(contents);

        cable.removePart(targetSide, true);

        final AEPartLocation placedSide = cable.addPart(
                new ItemStack(targetItem),
                targetSide,
                context.getPlayer(),
                context.getHand()
        );

        if (placedSide == null) {
            return ActionResultType.PASS;
        }

        contents.putBoolean("ae2retrofit_swap", true);
        final IPart newPart = cable.getPart(placedSide);
        if (newPart != null) {
            newPart.readFromNBT(contents);
        }
        contents.remove("ae2retrofit_swap");

        cable.markForUpdate();
        cable.markDirty();
        this.consumeIfSurvival(context);
        return ActionResultType.SUCCESS;
    }

    @Nullable
    private AEPartLocation detectPartSideByHit(ItemUseContext context, CableBusTileEntity cable) {
        final BlockPos cablePos = cable.getPos();
        final Vector3d worldHit = context.getHitVec();
        final Vector3d localHit = worldHit.subtract(cablePos.getX(), cablePos.getY(), cablePos.getZ());

        for (Vector3d candidate : new Vector3d[] { worldHit, localHit }) {
            SelectedPart selected = cable.selectPart(candidate);
            if (selected != null && selected.part != null
                    && this.partReplacements.containsKey(selected.part.getClass())) {
                return selected.side;
            }
        }
        return null;
    }

    @Nullable
    private AEPartLocation detectUniqueMatchingPart(CableBusTileEntity cable) {
        AEPartLocation found = null;
        for (AEPartLocation side : AEPartLocation.SIDE_LOCATIONS) {
            IPart p = cable.getPart(side);
            if (p != null && this.partReplacements.containsKey(p.getClass())) {
                if (found != null) {
                    return null; // More than one — ambiguous
                }
                found = side;
            }
        }
        return found;
    }

    private void consumeIfSurvival(ItemUseContext context) {
        final PlayerEntity player = context.getPlayer();
        if (player == null || !player.abilities.isCreativeMode) {
            context.getItem().shrink(1);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip,
                               ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("item.ae2retrofit.extended_interface_upgrade.tooltip")
                .mergeStyle(TextFormatting.GRAY));
    }
}