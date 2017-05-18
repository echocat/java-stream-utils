package org.echocat.jsu;

import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class UntilUnitTest {

    @Test
    public void simple() throws Exception {
        final List<Long> source = givenListWithNLongs(2000);
        final Until<Long> actual = new Until<>(source.spliterator(), candidate -> candidate < 1000);
        final List<Long> actualList = asList(actual);
        assertThat(actualList, hasSize(1000));
        assertThat(actualList, isEqualTo(source.subList(0, 1000)));
    }

    @Test
    public void endsBeforePredicateIsReached() throws Exception {
        final List<Long> source = givenListWithNLongs(500);
        final Until<Long> actual = new Until<>(source.spliterator(), candidate -> candidate < 1000);
        final List<Long> actualList = asList(actual);
        assertThat(actualList, hasSize(500));
        assertThat(actualList, isEqualTo(source));
    }

    @Test
    public void constructor() throws Exception {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final Predicate<Long> predicate = candidate -> candidate < 100;

        final Until<Long> actual = new Until<>(source, predicate);

        assertThat(actual.source(), isSameAs(source));
        assertThat(actual.predicate(), isSameAs(predicate));
    }

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
        while (source.tryAdvance(result::add)) { ; }
        return result;
    }

}