package org.echocat.jsu;

import org.echocat.jsu.Generator.Value;
import org.junit.Test;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsInstanceOf.isInstanceOf;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class SpliteratorUtilsUnitTest {

    @Test
    public void until() throws Exception {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final Predicate<Long> predicate = candidate -> candidate < 100;

        final Spliterator<Long> actual = SpliteratorUtils.until(source, predicate);

        assertThat(actual, isInstanceOf(Until.class));

        assertThat(((Until<Long>) actual).source(), isSameAs(source));
        assertThat(((Until<Long>) actual).predicate(), isSameAs(predicate));
    }

    @Test
    public void batchWithBatchSizeSupplier() throws Exception {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final Supplier<Integer> batchSizeSupplier = () -> 10;

        final Spliterator<List<Long>> actual = SpliteratorUtils.batch(source, batchSizeSupplier);

        assertThat(actual, isInstanceOf(Batch.class));

        assertThat(((Batch<Long>) actual).source(), isSameAs(source));
        assertThat(((Batch<Long>) actual).batchSize(), isSameAs(batchSizeSupplier));
    }

    @Test
    public void batchWithBatchSize() throws Exception {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final int batchSize = 10;

        final Spliterator<List<Long>> actual = SpliteratorUtils.batch(source, batchSize);

        assertThat(actual, isInstanceOf(Batch.class));

        assertThat(((Batch<Long>) actual).source(), isSameAs(source));
        assertThat(((Batch<Long>) actual).batchSize().get(), isEqualTo(batchSize));
    }

    @Test
    public void generate() throws Exception {
        final Generator<Integer> generator = Value::end;

        final Spliterator<Integer> actual = SpliteratorUtils.generate(generator);

        assertThat(actual, isInstanceOf(ContinuingGenerator.class));

        assertThat(((ContinuingGenerator<Integer>) actual).generator(), isSameAs(generator));
    }

    @Test
    public void constructor() throws Exception {
        new SpliteratorUtils();
    }

}