package org.echocat.jsu;

import javax.annotation.Nullable;

public final class AutoCloseableUtils {

    public static void closeQuietly(@Nullable Object closeable) {
        try {
            if (closeable instanceof AutoCloseable) {
                ((AutoCloseable) closeable).close();
            }
        } catch (final Exception ignored) {}
    }

}
