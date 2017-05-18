package org.echocat.jsu;

import org.junit.Test;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.echocat.unittest.utils.matchers.HasSize.hasSize;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.echocat.unittest.utils.matchers.IterableMatchers.containsOnlyElementsThat;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class BatchUnitTest {

    @Test
    public void simple() throws Exception {
        final List<Long> source = givenListWithNLongs(1000);
        final Spliterator<List<Long>> actual = new Batch<>(source.spliterator(), () -> 10);
        final List<List<Long>> actualList = asList(actual);
        assertThat(actualList, hasSize(100));
        assertThat(actualList, containsOnlyElementsThat(hasSize(10)));

        final Iterator<Long> expectedIterator = source.iterator();
        for (final List<Long> actualBatch : actualList) {
            for (final Long actualElement : actualBatch) {
                final Long expectedElement = expectedIterator.next();
                assertThat(actualElement, isEqualTo(expectedElement));
            }
        }
    }

    @Test
    public void constructor() throws Exception {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final Supplier<Integer> batchSizeSupplier = () -> 10;

        final Batch<Long> instance = new Batch<>(source, batchSizeSupplier);

        assertThat(instance.source(), isSameAs(source));
        assertThat(instance.batchSize(), isSameAs(batchSizeSupplier));
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