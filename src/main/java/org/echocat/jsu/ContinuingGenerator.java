package org.echocat.jsu;

import org.echocat.jsu.Generator.Value;

import javax.annotation.Nonnull;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;

public class ContinuingGenerator<T> extends AbstractSpliterator<T> {

    @Nonnull
    private final Generator<? extends T> generator;

    public ContinuingGenerator(@Nonnull Generator<? extends T> generator) {
        super(Long.MAX_VALUE, ORDERED | IMMUTABLE);
        this.generator = generator;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        final Value<? extends T> value = generator().generate();
        if (value == null) {
            return false;
        }
        action.accept(value.get());
        return true;
    }

    @Nonnull
    protected Generator<? extends T> generator() {
        return generator;
    }

}
