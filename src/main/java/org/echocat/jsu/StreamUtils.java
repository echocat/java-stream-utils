package org.echocat.jsu;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static org.echocat.jsu.Generator.Value.valueOf;

public final class StreamUtils {

    @Nonnull
    public static <T> Stream<T> until(@Nonnull Stream<? extends T> source, @Nonnegative @Nonnull Predicate<T> predicate) {
        return stream(SpliteratorUtils.until(source.spliterator(), predicate), false)
            .onClose(() -> closeQuietly(source));
    }

    @Nonnull
    public static <T> Stream<List<T>> batch(@Nonnull Stream<? extends T> source, @Nonnegative @Nonnull Supplier<Integer> batchSize) {
        //noinspection unchecked,rawtypes
        return (Stream) stream(SpliteratorUtils.batch(source.spliterator(), batchSize), false)
            .onClose(() -> closeQuietly(source));
    }

    @Nonnull
    public static <T> Stream<List<T>> batch(@Nonnull Stream<? extends T> source, @Nonnegative int batchSize) {
        return batch(source, () -> batchSize);
    }

    @Nonnull
    public static <T> Stream<T> generate(@Nonnull Generator<? extends T> generator) {
        //noinspection unchecked
        return (Stream<T>) stream(SpliteratorUtils.generate(generator), false)
            .onClose(() -> closeQuietly(generator));
    }

    @Nonnull
    public static <T> Stream<T> generate(@Nonnull Supplier<? extends T> supplier) {
        return generate(() -> valueOf(supplier.get()));
    }

    public static void closeQuietly(@Nullable Object closeable) {
        try {
            if (closeable instanceof AutoCloseable) {
                ((AutoCloseable) closeable).close();
            }
        } catch (final Exception ignored) {}
    }

}
