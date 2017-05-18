package org.echocat.jsu;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class SpliteratorUtils {

    @Nonnull
    public static <T> Spliterator<T> until(@Nonnull Spliterator<? extends T> source, @Nonnegative @Nonnull Predicate<T> predicate) {
        return new Until<>(source, predicate);
    }

    @Nonnull
    public static <T> Spliterator<List<T>> batch(@Nonnull Spliterator<? extends T> source, @Nonnegative @Nonnull Supplier<Integer> batchSize) {
        return new Batch<>(source, batchSize);
    }

    @Nonnull
    public static <T> Spliterator<List<T>> batch(@Nonnull Spliterator<? extends T> source, @Nonnegative int batchSize) {
        return batch(source, () -> batchSize);
    }

    @Nonnull
    public static <T> Spliterator<T> generate(@Nonnull Generator<? extends T> generator) {
        return new ContinuingGenerator<>(generator);
    }

}
