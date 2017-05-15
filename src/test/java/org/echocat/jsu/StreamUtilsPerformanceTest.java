package org.echocat.jsu;

import org.echocat.units4j.bytes.ByteCount;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.lang.System.gc;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static org.echocat.jsu.StreamUtils.generate;
import static org.echocat.jsu.StreamUtils.until;
import static org.echocat.units4j.bytes.ByteCount.valueOf;
import static org.echocat.unittest.utils.matchers.CompareTo.isLessThan;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.junit.Assert.assertThat;

@Ignore
public class StreamUtilsPerformanceTest {

    private static final MemoryMXBean MEMORY_MX_BEAN = getMemoryMXBean();
    private static final long POLLUTION_TEST_RUNS = 100000000;
    private static final ByteCount POTENTIAL_LONGS_IN_MEMORY_ON_POLLUTION_TESTS = ByteCount.valueOf(POLLUTION_TEST_RUNS).multiply(8).divide(4);

    @Test
    public void untilDoesNotPolluteMemory() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final AtomicLong expectedSerial = new AtomicLong();
        final Stream<Long> stream = until(generate(serial::getAndIncrement), candidate -> candidate <= MAX_VALUE)
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
    public void generateDoesNotPolluteMemory() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final AtomicLong expectedSerial = new AtomicLong();
        final Stream<Long> stream = generate(serial::getAndIncrement)
            .limit(100000000);

        stream.forEach(actual -> {
            final long expected = expectedSerial.getAndIncrement();
            assertThat(actual, isEqualTo(expected));
            if (actual % 100000 == 0) {
                assertHeapIsNotPolluted(POTENTIAL_LONGS_IN_MEMORY_ON_POLLUTION_TESTS);
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

    private static void assertHeapIsNotPolluted(@Nonnull @Nonnegative ByteCount maximumUsedMemory) {
        final ByteCount actualUsage = getHeapSafe();
        assertThat(actualUsage, isLessThan(maximumUsedMemory));
    }

}