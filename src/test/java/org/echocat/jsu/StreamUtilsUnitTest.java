package org.echocat.jsu;

import org.echocat.jsu.Generator.Value;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.stream.Collectors.toList;
import static org.echocat.jsu.Generator.Value.end;
import static org.echocat.jsu.StreamUtils.generate;
import static org.echocat.jsu.StreamUtils.until;
import static org.echocat.unittest.utils.matchers.HasSameSizeAs.hasSameSizeAs;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.junit.Assert.assertThat;

public class StreamUtilsUnitTest {

    @Test
    public void untilSimple() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> expected = new ArrayList<>();
        final List<Long> actual = until(generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 20000) {
                return end();
            }
            expected.add(current);
            return Value.valueOf(current);
        }), candidate -> candidate < 10000L).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(expected, hasSize(10001));
        assertThat(actual, isEqualTo(expected.subList(0, 10000)));
    }

    @Test
    public void untilCouldBeLimitedBefore() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> expected = new ArrayList<>();
        final List<Long> actual = until(generate(() -> {
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
    public void untilCouldBeLimitedAfter() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> expected = new ArrayList<>();
        final List<Long> actual = until(generate(() -> {
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
            return Value.valueOf(current);
        }).limit(10000).collect(toList());
        assertThat(actual, hasSize(10000));
        assertThat(expected, hasSize(10000));
        assertThat(actual, isEqualTo(expected));
    }

}