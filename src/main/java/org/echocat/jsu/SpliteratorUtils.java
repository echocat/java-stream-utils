package org.echocat.jsu;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public final class SpliteratorUtils {

    @Nonnull
    public static <T> Spliterator<T> takeWhile(@Nonnull Spliterator<? extends T> source, @Nonnegative @Nonnull Predicate<T> predicate) {
        return new TakeWhile<>(source, predicate);
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
        return new ContinuingSupplier<>(generator);
    }

}
