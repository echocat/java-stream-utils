package org.echocat.jsu;

import javax.annotation.Nonnull;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TakeWhile<T> extends AbstractSpliterator<T> {

    @Nonnull
    private final Spliterator<? extends T> source;
    @Nonnull
    private final Predicate<? super T> predicate;

    public TakeWhile(
        @Nonnull Spliterator<? extends T> source,
        @Nonnull Predicate<? super T> predicate
    ) {
        super(1, ORDERED | IMMUTABLE);
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public boolean tryAdvance(@Nonnull Consumer<? super T> consumer) {
        final var reference = new AtomicReference<T>();
        if (!source().tryAdvance(reference::set)) {
            return false;
        }
        if (!predicate().test(reference.get())) {
            return false;
        }
        consumer.accept(reference.get());
        return true;
    }

    @Nonnull
    protected Spliterator<? extends T> source() {
        return source;
    }

    @Nonnull
    protected Predicate<? super T> predicate() {
        return predicate;
    }

}
