package org.echocat.jsu;

import org.echocat.jsu.Generator.Value;
import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.echocat.jsu.Generator.Value.end;
import static org.echocat.jsu.StreamUtils.*;
import static org.echocat.unittest.utils.matchers.HasSameSizeAs.hasSameSizeAs;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IterableMatchers.containsOnlyElementsThat;
import static org.echocat.unittest.utils.matchers.ThrowsException.throwsException;
import static org.junit.Assert.assertThat;

@SuppressWarnings({"RedundantCast", "PointlessArithmeticExpression"})
public class StreamUtilsUnitTest {

    @Test
    public void takeWhileSimple() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> compareTo = new ArrayList<>();
        final List<Long> actual = takeWhile(generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 20000) {
                return end();
            }
            compareTo.add(current);
            return Value.valueOf(current);
        }), candidate -> candidate < 10000L).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(compareTo, hasSize(10001)); //Because value was already created and the until happens after it...
        assertThat(actual, isEqualTo(compareTo.subList(0, 10000)));
    }

    @Test
    public void takeWhileCouldBeLimitedBefore() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> expected = new ArrayList<>();
        final List<Long> actual = takeWhile(generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 20000) {
                return end();
            }
            expected.add(current);
            return Value.valueOf(current);
        }), candidate -> candidate < 10000L).limit(100).collect(toList());
        assertThat(actual, hasSize(100));
        assertThat(expected, hasSize(100));
        assertThat(actual, isEqualTo(expected));
    }

    @Test
    public void takeWhileCouldBeLimitedAfter() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> expected = new ArrayList<>();
        final List<Long> actual = takeWhile(generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 20000) {
                return end();
            }
            expected.add(current);
            return Value.valueOf(current);
        }), candidate -> candidate < 10000L).limit(30000).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(expected, hasSize(10001));
        assertThat(actual, isEqualTo(expected.subList(0, 10000)));
    }

    @Test
    public void batchWithFullBatches() throws Exception {
        final AtomicLong compareToSerial = new AtomicLong();
        final Stream<Long> baseStream = givenStreamOfSize(100 * 10);
        final List<List<Long>> actual = batch(baseStream, 10)
            .collect(toList());
        assertThat(actual, hasSize(100));
        assertThat(actual, containsOnlyElementsThat(hasSize(10)));

        for (final List<Long> actualBatch : actual) {
            for (final Long actualElement : actualBatch) {
                final Long expectedElement = compareToSerial.getAndIncrement();
                assertThat(actualElement, isEqualTo(expectedElement));
            }
        }
    }

    @Test
    public void batchWithOnlyOneHalfBatch() throws Exception {
        final Stream<Long> baseStream = givenStreamOfSize(1 * 5);
        final List<List<Long>> actual = batch(baseStream, 10).collect(toList());
        assertThat(actual, hasSize(1));
        assertThat(actual.get(0), isEqualTo(asList(0L, 1L, 2L, 3L, 4L)));
    }

    @Test
    public void batchWithOneFullAndOneHalfBatch() throws Exception {
        final Stream<Long> baseStream = givenStreamOfSize(10 + 5);
        final List<List<Long>> actual = batch(baseStream, 10).collect(toList());
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), isEqualTo(asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)));
        assertThat(actual.get(1), isEqualTo(asList(10L, 11L, 12L, 13L, 14L)));
    }

    @Test
    public void generateSimple() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> expected = new ArrayList<>();
        final List<Long> actual = generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 10000) {
                return end();
            }
            expected.add(current);
            return Value.valueOf(current);
        }).collect(toList());
        assertThat(actual, hasSameSizeAs(expected));
        assertThat(actual, isEqualTo(expected));
    }

    @Test
    public void generateCouldBeLimited() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> expected = new ArrayList<>();
        final List<Long> actual = generate(() -> {
            final long current = serial.getAndIncrement();
            expected.add(current);
            return current;
        }).limit(10000).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(expected, hasSize(10000));
        assertThat(actual, isEqualTo(expected));
    }

    @Test
    public void constructor() throws Exception {
        new StreamUtils();
    }

    @Nonnull
    protected static Stream<Long> givenEndlessStream() {
        final AtomicLong serial = new AtomicLong();
        return generate(() -> (Long) serial.getAndIncrement());
    }

    @Nonnull
    protected static Stream<Long> givenStreamOfSize(@Nonnegative long numberOfElements) {
        return givenEndlessStream()
            .limit(numberOfElements);
    }

}