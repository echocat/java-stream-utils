package org.echocat.jsu;

import static java.util.stream.Collectors.toList;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

public class TakeWhileUnitTest {

    @Test
    void simple() {
        final var source = givenListWithNLongs(2000);
        final var actual = new TakeWhile<>(source.spliterator(), candidate -> candidate < 1000);
        final var actualList = asList(actual);
        assertThat(actualList, hasSize(1000));
        assertThat(actualList, isEqualTo(source.subList(0, 1000)));
    }

    @Test
    void endsBeforePredicateIsReached() {
        final var source = givenListWithNLongs(500);
        final var actual = new TakeWhile<>(source.spliterator(), candidate -> candidate < 1000);
        final var actualList = asList(actual);
        assertThat(actualList, hasSize(500));
        assertThat(actualList, isEqualTo(source));
    }

    @Test
    void constructor() {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final Predicate<Long> predicate = candidate -> candidate < 100;

        final var actual = new TakeWhile<>(source, predicate);

        assertThat(actual.source(), isSameAs(source));
        assertThat(actual.predicate(), isSameAs(predicate));
    }

    @Nonnull
    protected static List<Long> givenListWithNLongs(@Nonnegative int count) {
        final var serial = new AtomicLong();
        return Stream.generate(serial::getAndIncrement)
            .limit(count)
            .collect(toList());
    }

    @Nonnull
    protected static <T> List<T> asList(@Nonnull Spliterator<T> source) {
        final var result = new ArrayList<T>();
        //noinspection StatementWithEmptyBody
        while (source.tryAdvance(result::add)) {}
        return result;
    }

}