package blobawv.ae2retrofit;

import net.minecraftforge.fml.ModList;

public final class ModCompat {
    private ModCompat() {}
    public static final boolean FLUIDCRAFT_PRESENT = ModList.get().isLoaded("ae2fc");
}