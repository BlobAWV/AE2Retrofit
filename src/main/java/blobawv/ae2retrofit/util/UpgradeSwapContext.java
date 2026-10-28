package blobawv.ae2retrofit.util;

public final class UpgradeSwapContext {

    private static final ThreadLocal<Boolean> IN_PROGRESS = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private UpgradeSwapContext() {}

    public static boolean isSwapInProgress() {
        return IN_PROGRESS.get();
    }

    public static void run(Runnable action) {
        final boolean previous = IN_PROGRESS.get();
        IN_PROGRESS.set(Boolean.TRUE);
        try {
            action.run();
        } finally {
            IN_PROGRESS.set(previous);
        }
    }
}