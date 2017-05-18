package org.echocat.jsu;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Batch<T> extends AbstractSpliterator<List<T>> {

    @Nonnull
    private final Spliterator<? extends T> source;
    @Nonnull
    @Nonnegative
    private final Supplier<Integer> batchSize;

    public Batch(
        @Nonnull Spliterator<? extends T> source,
        @Nonnegative @Nonnull Supplier<Integer> batchSize
    ) {
        super(1, ORDERED | IMMUTABLE);
        this.source = source;
        this.batchSize = batchSize;
    }

    @Override
    public boolean tryAdvance(@Nonnull Consumer<? super List<T>> consumer) {
        final int batchSize = batchSize().get();
        final List<T> batch = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            if (!source().tryAdvance(batch::add)) {
                if (!batch.isEmpty()) {
                    consumer.accept(batch);
                }
                return false;
            }
        }
        consumer.accept(batch);
        return true;
    }

    @Nonnull
    protected Spliterator<? extends T> source() {
        return source;
    }

    @Nonnull
    protected Supplier<Integer> batchSize() {
        return batchSize;
    }

}
