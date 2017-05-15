package org.echocat.jsu;

import org.echocat.jsu.Generator.Value;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static org.echocat.jsu.Generator.Value.valueOf;

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
        return new AbstractSpliterator<T>(1, ORDERED | IMMUTABLE) {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                final Value<? extends T> value = generator.generate();
                if (value == null) {
                    return false;
                }
                action.accept(value.get());
                return true;
            }
        };
    }

    @Nonnull
    public static <T> Spliterator<T> generate(@Nonnull Supplier<? extends T> generator) {
        return generate(() -> valueOf(generator.get()));
    }

}
