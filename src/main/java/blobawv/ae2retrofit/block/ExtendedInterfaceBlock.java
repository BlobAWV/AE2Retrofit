package blobawv.ae2retrofit.block;

import javax.annotation.Nullable;

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

import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.util.InteractionUtil;

import blobawv.ae2retrofit.tile.ExtendedInterfaceTileEntity;
import blobawv.ae2retrofit.container.ExtendedInterfaceContainer;

import java.util.EnumSet;

public class ExtendedInterfaceBlock extends AEBaseTileBlock<ExtendedInterfaceTileEntity> {

    private static final BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create("omnidirectional");
    private static final DirectionProperty FACING = DirectionProperty.create("facing", EnumSet.allOf(Direction.class));

    public ExtendedInterfaceBlock() {
        super(AEBaseBlock.defaultProps(Material.IRON));
        // The block definition is wired in ExtendedRegistryHandler once the
        // TileEntityType has been registered. (See onRegisterTileEntities.)
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(OMNIDIRECTIONAL, FACING);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, ExtendedInterfaceTileEntity te) {
        return currentState.with(OMNIDIRECTIONAL, te.isOmniDirectional()).with(FACING, te.getForward());
    }

    @Override
    public ActionResultType onActivated(World w, BlockPos pos, PlayerEntity p, Hand hand,
                                        @Nullable ItemStack heldItem, BlockRayTraceResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return ActionResultType.PASS;
        }
        final ExtendedInterfaceTileEntity tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (!w.isRemote()) {
                ContainerOpener.openContainer(ExtendedInterfaceContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace()));
            }
            return ActionResultType.func_233537_a_(w.isRemote());
        }
        return ActionResultType.PASS;
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(IOrientable rotatable, Direction axis) {
        if (rotatable instanceof ExtendedInterfaceTileEntity) {
            ((ExtendedInterfaceTileEntity) rotatable).setSide(axis);
        }
    }
}