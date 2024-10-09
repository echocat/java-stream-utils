package org.echocat.jsu;

import java.util.Optional;
import javax.annotation.Nonnull;

@FunctionalInterface
public interface Generator<T> {

    @Nonnull
    Optional<T> generate();

}
