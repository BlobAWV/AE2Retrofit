package blobawv.ae2retrofit.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.EnumSet;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.util.Platform;

import blobawv.ae2retrofit.container.ExtendedItemDualInterfaceContainer;
import blobawv.ae2retrofit.tile.TileExtendedDualInterface;

public class ExtendedDualInterfaceBlock extends AEBaseTileBlock<TileExtendedDualInterface> {

    private static final BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create("omnidirectional");
    private static final DirectionProperty FACING = DirectionProperty.create("facing", EnumSet.allOf(Direction.class));

    public ExtendedDualInterfaceBlock() {
        super(AEBaseBlock.defaultProps(Material.IRON));
        // TE factory is wired in ExtendedRegistryHandler once TileExtendedDualInterface.TYPE exists.
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(OMNIDIRECTIONAL, FACING);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, TileExtendedDualInterface te) {
        return currentState.with(OMNIDIRECTIONAL, te.isOmniDirectional()).with(FACING, te.getForward());
    }

    @Override
    public ActionResultType onActivated(World w, BlockPos pos, PlayerEntity p, Hand hand,
                                        @Nullable ItemStack heldItem, BlockRayTraceResult hit) {
        if (p.isSneaking()) {
            return ActionResultType.PASS;
        }
        final TileExtendedDualInterface tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(ExtendedItemDualInterfaceContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace()));
            }
            return ActionResultType.func_233537_a_(w.isRemote);
        }
        return ActionResultType.PASS;
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(final IOrientable rotatable, final Direction axis) {
        if (rotatable instanceof TileExtendedDualInterface) {
            ((TileExtendedDualInterface) rotatable).setSide(axis);
        }
    }
}