package org.echocat.jsu;

import static org.echocat.jsu.AutoCloseableUtils.doOnClose;
import static org.echocat.jsu.AutoCloseableUtils.resolveCloseMethodOf;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsNot.isNot;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameInstance;
import static org.echocat.unittest.utils.matchers.IsSameAs.sameInstance;
import static org.echocat.unittest.utils.matchers.ThrowsException.throwsException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;

import org.echocat.jsu.support.Callable;
import org.junit.jupiter.api.Test;

@SuppressWarnings("InstantiationOfUtilityClass")
public class AutoCloseableUtilsUnitTest {

    @Test
    void closeQuietlyClosesAutoCloseables() throws Exception {
        final var autoCloseable = givenAutoCloseable();

        AutoCloseableUtils.closeQuietly(autoCloseable);
        verify(autoCloseable, times(1)).close();
    }

    @Test
    void closeQuietlySilentlyIgnoresNulls() {
        AutoCloseableUtils.closeQuietly(null);
    }

    @Test
    void closeQuietlySilentlyIgnoresNonAutoCloseables() {
        AutoCloseableUtils.closeQuietly(new Object());
    }

    @Test
    void closeQuietlySilentlyIgnoresExceptionsOfAutoCloseables() throws Exception {
        final var autoCloseable = givenAutoCloseable();

        doThrow(new RuntimeException("test")).when(autoCloseable).close();
        AutoCloseableUtils.closeQuietly(autoCloseable);
    }

    @Test
    void closeQuietlyDoesNotIgnoreErrorsOfAutoCloseables() throws Exception {
        final var autoCloseable = givenAutoCloseable();

        doThrow(new AssertionError("test")).when(autoCloseable).close();
        assertThat(() -> AutoCloseableUtils.closeQuietly(autoCloseable), throwsException(AssertionError.class, "test"));
    }

    @Test
    void doOnCloseForAutoCloseableWithNoRunnable() {
        final var anAutoCloseable = givenAutoCloseable();

        assertThat(doOnClose(AutoCloseable.class, anAutoCloseable), isSameInstance(anAutoCloseable));
        //noinspection ConfusingArgumentToVarargsMethod
        assertThat(doOnClose(AutoCloseable.class, anAutoCloseable, null), isSameInstance(anAutoCloseable));
        //noinspection RedundantArrayCreation
        assertThat(doOnClose(AutoCloseable.class, anAutoCloseable, new Callable[0]), isSameInstance(anAutoCloseable));
    }

    @Test
    void doOnCloseForAutoCloseableAndOneRunnable() throws Exception {
        final var original = givenAutoCloseable();
        final var callable = givenCallable();

        final var actual = doOnClose(AutoCloseable.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    void doOnCloseForAutoCloseableAndTwoRunnable() throws Exception {
        final var original = givenAutoCloseable();
        final var callable1 = givenCallable();
        final var callable2 = givenCallable();

        final var actual = doOnClose(AutoCloseable.class, original, callable1, callable2);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable1, times(1)).call();
        verify(callable2, times(1)).call();
    }

    @Test
    void doOnCloseForCloseableAndOneRunnable() throws Exception {
        final var original = givenCloseable();
        final var callable = givenCallable();

        final var actual = doOnClose(Closeable.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    void doOnCloseForConnectionAndOneRunnable() throws Exception {
        final var original = givenConnection();
        final var callable = givenCallable();

        final var actual = doOnClose(Connection.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    void doOnCloseForStatementAndOneRunnable() throws Exception {
        final var original = givenStatement();
        final var callable = givenCallable();

        final var actual = doOnClose(Statement.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    void doOnCloseForResultSetAndOneRunnable() throws Exception {
        final var original = givenResultSet();
        final var callable = givenCallable();

        final var actual = doOnClose(ResultSet.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    void doOnCloseHandlesSimpleDelegates() throws Exception {
        final var original = givenResultSet();
        final var callable = givenCallable();

        final var actual = doOnClose(ResultSet.class, original, callable);

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();

        doReturn(111).when(original).getInt(666);
        assertThat(actual.getInt(666), isEqualTo(111));
        verify(original, times(1)).getInt(666);

        actual.setFetchSize(666);
        verify(original, times(1)).setFetchSize(666);
    }

    @Test
    void doOnCloseHandlesExceptionDelegates() throws Exception {
        final var original = givenResultSet();
        final var callable = givenCallable();

        final var actual = doOnClose(ResultSet.class, original, callable);

        doThrow(new SQLException("test")).when(original).getInt(666);
        assertThat(() -> actual.getInt(666), throwsException(SQLException.class, "test"));
        verify(original, times(1)).getInt(666);
    }

    @Test
    void doOnCloseHandlesExpectedExceptionsOnCallables() throws Exception {
        final var original = givenResultSet();
        final var callable = givenCallable();

        final var actual = doOnClose(ResultSet.class, original, callable);

        doThrow(new SQLException("test")).when(callable).call();
        assertThat(actual::close, throwsException(SQLException.class, "test"));
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    void doOnCloseHandlesUnexpectedExceptionsOnCallables() throws Exception {
        final var original = givenResultSet();
        final var callable = givenCallable();

        final var actual = doOnClose(ResultSet.class, original, callable);

        doThrow(new IOException("test")).when(callable).call();
        assertThat(actual::close, throwsException(UndeclaredThrowableException.class));
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    void resolveCloseMethodOfHandlesMethodDoesNotExist() {
        assertThat(() -> resolveCloseMethodOf(Object.class), throwsException(IllegalStateException.class));
    }

    @Test
    void constructor() {
        new AutoCloseableUtils();
    }

    @Nonnull
    protected static Statement givenStatement() {
        return mock(Statement.class);
    }

    @Nonnull
    protected static Connection givenConnection() {
        return mock(Connection.class);
    }

    @Nonnull
    protected static ResultSet givenResultSet() {
        return mock(ResultSet.class);
    }

    @Nonnull
    protected static Closeable givenCloseable() {
        return mock(Closeable.class);
    }

    @Nonnull
    protected static AutoCloseable givenAutoCloseable() {
        return mock(AutoCloseable.class);
    }

    @Nonnull
    protected static Callable givenCallable() {
        return mock(Callable.class);
    }

}