package org.echocat.jsu;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class ContinuingSupplier<T> extends AbstractSpliterator<T> {

    @Nonnull
    private final Generator<? extends T> generator;

    public ContinuingSupplier(@Nonnull Generator<? extends T> generator) {
        super(Long.MAX_VALUE, ORDERED | IMMUTABLE);
        this.generator = generator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        final var next = generator().generate();
        if (next.isEmpty()) {
            return false;
        }
        action.accept(next.get());
        return true;
    }

    @Nonnull
    protected Generator<? extends T> generator() {
        return generator;
    }

}
