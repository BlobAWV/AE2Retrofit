package blobawv.ae2retrofit.mixinsupport;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;

/**
 * Lives outside the mixin package: Mixin can't load named nested classes of a mixin
 * at runtime, which crashed the server with NoClassDefFoundError when an
 * Interface Terminal was opened.
 */
public final class MergedMachineSet extends AbstractSet<IGridNode> implements IMachineSet {
    private final List<IGridNode> all;
    private final Class<? extends IGridHost> machine;

    public MergedMachineSet(IMachineSet a, IMachineSet b) {
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
