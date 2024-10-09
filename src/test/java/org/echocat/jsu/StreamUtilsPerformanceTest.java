package org.echocat.jsu;

import static java.lang.Long.MAX_VALUE;
import static java.lang.System.gc;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static org.echocat.jsu.StreamUtils.*;
import static org.echocat.units4j.bytes.ByteCount.valueOf;
import static org.echocat.unittest.utils.matchers.CompareTo.isLessThan;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.management.MemoryMXBean;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.echocat.units4j.bytes.ByteCount;
import org.junit.jupiter.api.Test;

public class StreamUtilsPerformanceTest {

    private static final MemoryMXBean MEMORY_MX_BEAN = getMemoryMXBean();
    private static final long POLLUTION_TEST_RUNS = 100000000;
    private static final ByteCount POTENTIAL_LONGS_IN_MEMORY_ON_POLLUTION_TESTS = ByteCount.valueOf(POLLUTION_TEST_RUNS).multiply(8).divide(4);

    @Test
    void takeWhileDoesNotPolluteMemory() {
        final var expectedSerial = new AtomicLong();
        //noinspection ConstantValue
        final var stream = takeWhile(generate(endless()), candidate -> candidate <= MAX_VALUE)
            .limit(POLLUTION_TEST_RUNS);

        stream.forEach(actual -> {
            final long expected = expectedSerial.getAndIncrement();
            assertThat(actual, isEqualTo(expected));
            if (actual % 100000 == 0) {
                assertHeapIsNotPolluted(POTENTIAL_LONGS_IN_MEMORY_ON_POLLUTION_TESTS);
            }
        });
    }

    @Test
    void generateDoesNotPolluteMemory() {
        final var expectedSerial = new AtomicLong();
        final var stream = generate(endless())
            .limit(POLLUTION_TEST_RUNS);

        stream.forEach(actual -> {
            final long expected = expectedSerial.getAndIncrement();
            assertThat(actual, isEqualTo(expected));
            if (actual % 100000 == 0) {
                assertHeapIsNotPolluted(POTENTIAL_LONGS_IN_MEMORY_ON_POLLUTION_TESTS);
            }
        });
    }

    @Test
    void batchDoesNotPolluteMemory() {
        final var expectedSerial = new AtomicLong();
        final var baseStream = generate(endless())
            .limit(POLLUTION_TEST_RUNS);
        final var stream = batch(baseStream, 10);

        stream.forEach(actualBatch -> {
            for (final Long actual : actualBatch) {
                final long expected = expectedSerial.getAndIncrement();
                assertThat(actual, isEqualTo(expected));
                if (actual % 100000 == 0) {
                    assertHeapIsNotPolluted(POTENTIAL_LONGS_IN_MEMORY_ON_POLLUTION_TESTS);
                }
            }
        });
    }

    @Nonnull
    @Nonnegative
    private static ByteCount getHeapSafe() {
        //noinspection CallToSystemGC
        gc();
        return valueOf(MEMORY_MX_BEAN.getHeapMemoryUsage().getUsed());
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertHeapIsNotPolluted(@Nonnull @Nonnegative ByteCount maximumUsedMemory) {
        final var actualUsage = getHeapSafe();
        assertThat(actualUsage, isLessThan(maximumUsedMemory));
    }

    @Nonnull
    private static Generator<Long> endless() {
        final var serial = new AtomicLong();
        return () -> Optional.of(serial.getAndIncrement());
    }

}