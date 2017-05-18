package org.echocat.jsu;

import org.echocat.jsu.Generator.Value;
import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.echocat.jsu.Generator.Value.end;
import static org.echocat.jsu.Generator.Value.valueOf;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.junit.Assert.assertThat;

public class ContinuingGeneratorUnitTest {

    @Test
    public void simple() throws Exception {
        final AtomicLong serial = new AtomicLong();
        final List<Long> source = givenListWithNLongs(1000);
        final Spliterator<Long> actual = SpliteratorUtils.generate(() -> {
            final long current = serial.getAndIncrement();
            if (current >= 1000) {
                return end();
            }
            return valueOf(current);
        });
        final List<Long> actualList = asList(actual);
        assertThat(actualList, hasSize(1000));
        assertThat(actualList, isEqualTo(source));
    }

    @Test
    public void constructor() throws Exception {
        final Generator<Integer> generator = Value::end;

        final ContinuingGenerator<Integer> actual = new ContinuingGenerator<>(generator);

        assertThat(actual.generator(), isSameAs(generator));
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
        while (source.tryAdvance(result::add)) {}
        return result;
    }


}