package blobawv.ae2retrofit.mixinsupport;

import appeng.client.gui.me.interfaceterminal.InterfaceRecord;

/**
 * Lives outside the mixin package: Mixin can't load named nested classes of a mixin
 * at runtime (NoClassDefFoundError when the Interface Terminal screen draws).
 */
public final class RowSegment {
    private final InterfaceRecord record;
    private final int rowIndex;

    public RowSegment(InterfaceRecord record, int rowIndex) {
        this.record = record;
        this.rowIndex = rowIndex;
    }

    public InterfaceRecord getRecord() { return record; }
    public int getRowIndex() { return rowIndex; }
}
