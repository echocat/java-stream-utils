package org.echocat.jsu;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.echocat.jsu.StreamUtils.*;
import static org.echocat.unittest.utils.matchers.HasSameSizeAs.hasSameSizeAs;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IterableMatchers.containsOnlyElementsThat;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

@SuppressWarnings({"PointlessArithmeticExpression"})
public class StreamUtilsUnitTest {

    @Test
    void takeWhileSimple() {
        final var serial = new AtomicLong();
        final var compareTo = new ArrayList<>();
        final var actual = takeWhile(generate(() -> {
            final var current = serial.getAndIncrement();
            if (current >= 20000) {
                return Optional.empty();
            }
            compareTo.add(current);
            return Optional.of(current);
        }), candidate -> candidate < 10000L).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(compareTo, hasSize(10001)); //Because value was already created and the until happens after it...
        assertThat(actual, isEqualTo(compareTo.subList(0, 10000)));
    }

    @Test
    void takeWhileCouldBeLimitedBefore() {
        final var serial = new AtomicLong();
        final var expected = new ArrayList<>();
        final var actual = takeWhile(generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 20000) {
                return Optional.empty();
            }
            expected.add(current);
            return Optional.of(current);
        }), candidate -> candidate < 10000L).limit(100).collect(toList());
        assertThat(actual, hasSize(100));
        assertThat(expected, hasSize(100));
        assertThat(actual, isEqualTo(expected));
    }

    @Test
    void takeWhileCouldBeLimitedAfter() {
        final var serial = new AtomicLong();
        final var expected = new ArrayList<>();
        final var actual = takeWhile(generate(() -> {
            final var current = serial.getAndIncrement();
            if (current >= 20000) {
                return Optional.empty();
            }
            expected.add(current);
            return Optional.of(current);
        }), candidate -> candidate < 10000L).limit(30000).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(expected, hasSize(10001));
        assertThat(actual, isEqualTo(expected.subList(0, 10000)));
    }

    @Test
    void batchWithFullBatches() {
        final var compareToSerial = new AtomicLong();
        final var baseStream = givenStreamOfSize(100 * 10);
        final var actual = batch(baseStream, 10)
            .collect(toList());
        assertThat(actual, hasSize(100));
        assertThat(actual, containsOnlyElementsThat(hasSize(10)));

        for (final var actualBatch : actual) {
            for (final Long actualElement : actualBatch) {
                final Long expectedElement = compareToSerial.getAndIncrement();
                assertThat(actualElement, isEqualTo(expectedElement));
            }
        }
    }

    @Test
    void batchWithOnlyOneHalfBatch() {
        final var baseStream = givenStreamOfSize(1 * 5);
        final var actual = batch(baseStream, 10).collect(toList());
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0), isEqualTo(asList(0L, 1L, 2L, 3L, 4L)));
    }

    @Test
    void batchWithOneFullAndOneHalfBatch() {
        final var baseStream = givenStreamOfSize(10 + 5);
        final var actual = batch(baseStream, 10).collect(toList());
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), isEqualTo(asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)));
        assertThat(actual.get(1), isEqualTo(asList(10L, 11L, 12L, 13L, 14L)));
    }

    @Test
    void generateSimple() {
        final var serial = new AtomicLong();
        final var expected = new ArrayList<>();
        final var actual = generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 10000) {
                return Optional.empty();
            }
            expected.add(current);
            return Optional.of(current);
        }).collect(toList());
        assertThat(actual, hasSameSizeAs(expected));
        assertThat(actual, isEqualTo(expected));
    }

    @Test
    void generateCouldBeLimited() {
        final var serial = new AtomicLong();
        final var expected = new ArrayList<>();
        final var actual = generate(() -> {
            final long current = serial.getAndIncrement();
            expected.add(current);
            return Optional.of(current);
        }).limit(10000).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(expected, hasSize(10000));
        assertThat(actual, isEqualTo(expected));
    }

    @Test
    void constructor() {
        //noinspection InstantiationOfUtilityClass
        new StreamUtils();
    }

    @Nonnull
    private static Generator<Long> endless() {
        final var serial = new AtomicLong();
        return () -> Optional.of(serial.getAndIncrement());
    }

    @Nonnull
    protected static Stream<Long> givenEndlessStream() {
        return generate(endless());
    }

    @Nonnull
    protected static Stream<Long> givenStreamOfSize(@Nonnegative long numberOfElements) {
        return givenEndlessStream()
            .limit(numberOfElements);
    }

}