package org.echocat.jsu;

import static org.echocat.jsu.AutoCloseableUtils.closeQuietly;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public final class StreamUtils {

    @Nonnull
    public static <T> Stream<T> takeWhile(@Nonnull Stream<? extends T> source, @Nonnegative @Nonnull Predicate<T> predicate) {
        //noinspection unchecked
        return (Stream<T>) source.takeWhile(predicate)
            .onClose(() -> closeQuietly(source));
    }

    @Nonnull
    public static <T> Stream<List<T>> batch(@Nonnull Stream<? extends T> source, @Nonnegative @Nonnull Supplier<Integer> batchSize) {
        return StreamSupport.<List<T>>stream(SpliteratorUtils.batch(source.spliterator(), batchSize), false)
            .onClose(() -> closeQuietly(source));
    }

    @Nonnull
    public static <T> Stream<List<T>> batch(@Nonnull Stream<? extends T> source, @Nonnegative int batchSize) {
        return batch(source, () -> batchSize);
    }

    @Nonnull
    public static <T> Stream<T> generate(@Nonnull Generator<? extends T> generator) {
        return generate(generator, false);
    }

    @Nonnull
    public static <T> Stream<T> generate(@Nonnull Generator<? extends T> generator, boolean parallel) {
        return StreamSupport.<T>stream(SpliteratorUtils.generate(generator), parallel)
            .onClose(() -> AutoCloseableUtils.closeQuietly(generator));
    }

}
