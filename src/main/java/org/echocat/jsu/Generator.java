package org.echocat.jsu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@FunctionalInterface
public interface Generator<T> {

    @Nullable
    Value<T> generate();

    @FunctionalInterface
    public interface Value<T> extends Supplier<T> {

        @Nonnull
        public static <T> Value<T> valueOf(@Nullable T value) {
            return () -> value;
        }

        @Nullable
        public static <T> Value<T> end() {
            return null;
        }

    }

}
