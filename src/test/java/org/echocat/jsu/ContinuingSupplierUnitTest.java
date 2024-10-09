package org.echocat.jsu;

import static java.util.stream.Collectors.toList;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

public class ContinuingSupplierUnitTest {

    @Test
    void simple() {
        final AtomicLong serial = new AtomicLong();
        final List<Long> source = givenListWithNLongs(1000);
        final Spliterator<Long> actual = SpliteratorUtils.generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 1000) {
                return Optional.empty();
            }
            return Optional.of(current);
        });
        final List<Long> actualList = asList(actual);
        assertThat(actualList, hasSize(1000));
        assertThat(actualList, isEqualTo(source));
    }

    @Test
    void constructor() {
        final Generator<Integer> generator = Optional::empty;

        final var actual = new ContinuingSupplier<>(generator);

        assertThat(actual.generator(), isSameAs(generator));
    }

    @SuppressWarnings("SameParameterValue")
    @Nonnull
    protected static List<Long> givenListWithNLongs(@Nonnegative int count) {
        final AtomicLong serial = new AtomicLong();
        return Stream.generate(serial::getAndIncrement)
            .limit(count)
            .collect(toList());
    }

    @Nonnull
    protected static <T> List<T> asList(@Nonnull Spliterator<T> source) {
        final List<T> result = new ArrayList<>();
        //noinspection StatementWithEmptyBody
        while (source.tryAdvance(result::add)) {}
        return result;
    }


}