package blobawv.ae2retrofit.mixins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.interfaceterminal.InterfaceRecord;
import appeng.client.gui.me.interfaceterminal.InterfaceSlot;
import appeng.client.gui.me.interfaceterminal.InterfaceTerminalScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.container.implementations.InterfaceTerminalContainer;

@Mixin(InterfaceTerminalScreen.class)
public abstract class MixinInterfaceTerminalScreen extends AEBaseScreen<InterfaceTerminalContainer> {

    @Shadow @Final private Scrollbar scrollbar;
    @Shadow @Final private ArrayList<Object> lines;
    @Shadow private int numLines;
    @Shadow private AETextField searchField;

    @Unique private final Map<Long, Integer> ae2retrofit$slotCounts = new HashMap<>();

    // Dummy ctor - Mixin never invokes this; the target's real ctor runs.
    private MixinInterfaceTerminalScreen() { super(null, null, null, null); }

    @Inject(method = "postInventoryUpdate", at = @At("HEAD"), remap = false)
    private void ae2retrofit$captureSlotCount(boolean clearExistingData, long inventoryId, CompoundNBT invData,
                                              CallbackInfo ci) {
        if (clearExistingData) {
            ae2retrofit$slotCounts.clear();
        } else if (invData.contains("slotCount")) {
            ae2retrofit$slotCounts.put(inventoryId, invData.getInt("slotCount"));
        }
    }

    @Redirect(
            method = "getById",
            at = @At(
                    value = "NEW",
                    target = "(JIJLnet/minecraft/util/text/ITextComponent;)Lappeng/client/gui/me/interfaceterminal/InterfaceRecord;"
            ),
            remap = false
    )
    private InterfaceRecord ae2retrofit$withSlotCount(long id, int fallbackSlots, long sortBy, ITextComponent name) {
        final Integer sc = ae2retrofit$slotCounts.get(id);
        return new InterfaceRecord(id, sc != null ? sc : fallbackSlots, sortBy, name);
    }

    @Inject(method = "refreshList", at = @At("TAIL"), remap = false)
    private void ae2retrofit$expandRows(CallbackInfo ci) {
        if (this.lines.isEmpty()) return;
        final List<Object> expanded = new ArrayList<>(this.lines.size() * 2);
        for (Object obj : this.lines) {
            if (obj instanceof InterfaceRecord) {
                final InterfaceRecord rec = (InterfaceRecord) obj;
                final int rows = Math.max(1, (rec.getInventory().getSlots() + 8) / 9);
                for (int r = 0; r < rows; r++) {
                    expanded.add(new RowSegment(rec, r));
                }
            } else {
                expanded.add(obj);
            }
        }
        this.lines.clear();
        this.lines.addAll(expanded);
        this.scrollbar.setRange(0, Math.max(0, this.lines.size() - this.numLines), 2);
    }

    @Inject(method = "drawFG", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2retrofit$drawFG(MatrixStack ms, int offsetX, int offsetY, int mouseX, int mouseY,
                                    CallbackInfo ci) {
        this.container.inventorySlots.removeIf(s -> s instanceof InterfaceSlot);
        final int textColor = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        final int scroll = scrollbar.getCurrentScroll();
        for (int i = 0; i < numLines; i++) {
            final int idx = scroll + i;
            if (idx >= lines.size()) break;
            final Object obj = lines.get(idx);
            if (obj instanceof RowSegment) {
                final RowSegment seg = (RowSegment) obj;
                final InterfaceRecord rec = seg.getRecord();
                final int start = seg.getRowIndex() * 9;
                final int end = Math.min(start + 9, rec.getInventory().getSlots());
                for (int z = start; z < end; z++) {
                    final int localZ = z - start;
                    this.container.inventorySlots.add(new InterfaceSlot(rec, z,
                            localZ * 18 + 8, (i + 1) * 18));
                }
            } else if (obj instanceof String) {
                String name = (String) obj;
                name = this.font.getLineScrollOffset(name, 155, true);
                this.font.drawString(ms, name, 8 + 2, 6 + 17 + i * 18, textColor);
            }
        }
        ci.cancel();
    }

    @Inject(method = "drawBG", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2retrofit$drawBG(MatrixStack ms, int offsetX, int offsetY, int mouseX, int mouseY,
                                    float partialTicks, CallbackInfo ci) {
        this.bindTexture("guis/interfaceterminal.png");
        // HEADER_BBOX = (0, 0, 195, 17)
        this.blit(ms, offsetX, offsetY, 0, 0, 195, 17);
        final int scroll = scrollbar.getCurrentScroll();
        int currentY = offsetY + 17;
        // FOOTER_BBOX = (0, 125, 195, 97)
        this.blit(ms, offsetX, currentY + numLines * 18, 0, 125, 195, 97);
        for (int i = 0; i < numLines; i++) {
            final boolean first = i == 0;
            final boolean last = i == numLines - 1;
            boolean isInv = false;
            final int idx = scroll + i;
            if (idx < lines.size()) {
                isInv = lines.get(idx) instanceof RowSegment;
            }
            final Rectangle2d bbox = selectRowBbox(isInv, first, last);
            this.blit(ms, offsetX, currentY, bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
            currentY += 18;
        }
        if (searchField != null) {
            searchField.render(ms, mouseX, mouseY, partialTicks);
        }
        ci.cancel();
    }

    @Unique
    private static Rectangle2d selectRowBbox(boolean isInv, boolean first, boolean last) {
        if (isInv) {
            if (first) return new Rectangle2d(0, 35, 195, 18);
            if (last)  return new Rectangle2d(0, 107, 195, 18);
            return new Rectangle2d(0, 71, 195, 18);
        }
        if (first) return new Rectangle2d(0, 17, 195, 18);
        if (last)  return new Rectangle2d(0, 89, 195, 18);
        return new Rectangle2d(0, 53, 195, 18);
    }

    public static final class RowSegment {
        private final InterfaceRecord record;
        private final int rowIndex;

        public RowSegment(InterfaceRecord record, int rowIndex) {
            this.record = record;
            this.rowIndex = rowIndex;
        }

        public InterfaceRecord getRecord() { return record; }
        public int getRowIndex() { return rowIndex; }
    }
}