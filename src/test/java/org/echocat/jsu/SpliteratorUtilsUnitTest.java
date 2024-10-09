package org.echocat.jsu;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsInstanceOf.isInstanceOf;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class SpliteratorUtilsUnitTest {

    @Test
    void takeWhile() {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final Predicate<Long> predicate = candidate -> candidate < 100;

        final var actual = SpliteratorUtils.takeWhile(source, predicate);

        assertThat(actual, isInstanceOf(TakeWhile.class));

        assertThat(((TakeWhile<Long>) actual).source(), isSameAs(source));
        assertThat(((TakeWhile<Long>) actual).predicate(), isSameAs(predicate));
    }

    @Test
    void batchWithBatchSizeSupplier() {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final Supplier<Integer> batchSizeSupplier = () -> 10;

        final var actual = SpliteratorUtils.batch(source, batchSizeSupplier);

        assertThat(actual, isInstanceOf(Batch.class));

        assertThat(((Batch<Long>) actual).source(), isSameAs(source));
        assertThat(((Batch<Long>) actual).batchSize(), isSameAs(batchSizeSupplier));
    }

    @Test
    void batchWithBatchSize() {
        //noinspection unchecked
        final Spliterator<Long> source = mock(Spliterator.class);
        final int batchSize = 10;

        final var actual = SpliteratorUtils.batch(source, batchSize);

        assertThat(actual, isInstanceOf(Batch.class));

        assertThat(((Batch<Long>) actual).source(), isSameAs(source));
        assertThat(((Batch<Long>) actual).batchSize().get(), isEqualTo(batchSize));
    }

    @Test
    void generate() {
        final Generator<Integer> generator = Optional::empty;

        final var actual = SpliteratorUtils.generate(generator);

        assertThat(actual, isInstanceOf(ContinuingSupplier.class));

        assertThat(((ContinuingSupplier<Integer>) actual).generator(), isSameAs(generator));
    }

    @Test
    public void constructor() {
        //noinspection InstantiationOfUtilityClass
        new SpliteratorUtils();
    }

}