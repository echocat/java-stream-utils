package org.echocat.jsu;

import org.echocat.jsu.support.Callable;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.echocat.jsu.AutoCloseableUtils.doOnClose;
import static org.echocat.jsu.AutoCloseableUtils.resolveCloseMethodOf;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsNot.isNot;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameInstance;
import static org.echocat.unittest.utils.matchers.IsSameAs.sameInstance;
import static org.echocat.unittest.utils.matchers.ThrowsException.throwsException;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AutoCloseableUtilsUnitTest {

    @Test
    public void closeQuietlyClosesAutoCloseables() throws Exception {
        final AutoCloseable autoCloseable = givenAutoCloseable();

        AutoCloseableUtils.closeQuietly(autoCloseable);
        verify(autoCloseable, times(1)).close();
    }

    @Test
    public void closeQuietlySilentlyIgnoresNulls() throws Exception {
        AutoCloseableUtils.closeQuietly(null);
    }

    @Test
    public void closeQuietlySilentlyIgnoresNonAutoCloseables() throws Exception {
        AutoCloseableUtils.closeQuietly(new Object());
    }

    @Test
    public void closeQuietlySilentlyIgnoresExceptionsOfAutoCloseables() throws Exception {
        final AutoCloseable autoCloseable = givenAutoCloseable();

        doThrow(new RuntimeException("test")).when(autoCloseable).close();
        AutoCloseableUtils.closeQuietly(autoCloseable);
    }

    @Test
    public void closeQuietlyDoesNotIgnoreErrorsOfAutoCloseables() throws Exception {
        final AutoCloseable autoCloseable = givenAutoCloseable();

        doThrow(new AssertionError("test")).when(autoCloseable).close();
        assertThat(() -> AutoCloseableUtils.closeQuietly(autoCloseable), throwsException(AssertionError.class, "test"));
    }

    @Test
    public void doOnCloseForAutoCloseableWithNoRunnable() throws Exception {
        final AutoCloseable anAutoCloseable = givenAutoCloseable();

        assertThat(doOnClose(AutoCloseable.class, anAutoCloseable), isSameInstance(anAutoCloseable));
        //noinspection ConfusingArgumentToVarargsMethod
        assertThat(doOnClose(AutoCloseable.class, anAutoCloseable, null), isSameInstance(anAutoCloseable));
        //noinspection RedundantArrayCreation
        assertThat(doOnClose(AutoCloseable.class, anAutoCloseable, new Callable[0]), isSameInstance(anAutoCloseable));
    }

    @Test
    public void doOnCloseForAutoCloseableAndOneRunnable() throws Exception {
        final AutoCloseable original = givenAutoCloseable();
        final Callable callable = givenCallable();

        final AutoCloseable actual = doOnClose(AutoCloseable.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    public void doOnCloseForAutoCloseableAndTwoRunnable() throws Exception {
        final AutoCloseable original = givenAutoCloseable();
        final Callable callable1 = givenCallable();
        final Callable callable2 = givenCallable();

        final AutoCloseable actual = doOnClose(AutoCloseable.class, original, callable1, callable2);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable1, times(1)).call();
        verify(callable2, times(1)).call();
    }

    @Test
    public void doOnCloseForCloseableAndOneRunnable() throws Exception {
        final Closeable original = givenCloseable();
        final Callable callable = givenCallable();

        final Closeable actual = doOnClose(Closeable.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    public void doOnCloseForConnectionAndOneRunnable() throws Exception {
        final Connection original = givenConnection();
        final Callable callable = givenCallable();

        final Connection actual = doOnClose(Connection.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    public void doOnCloseForStatementAndOneRunnable() throws Exception {
        final Statement original = givenStatement();
        final Callable callable = givenCallable();

        final Statement actual = doOnClose(Statement.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    public void doOnCloseForResultSetAndOneRunnable() throws Exception {
        final ResultSet original = givenResultSet();
        final Callable callable = givenCallable();

        final ResultSet actual = doOnClose(ResultSet.class, original, callable);

        assertThat(actual, isNot(sameInstance(original)));

        actual.close();
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    public void doOnCloseHandlesSimpleDelegates() throws Exception {
        final ResultSet original = givenResultSet();
        final Callable callable = givenCallable();

        final ResultSet actual = doOnClose(ResultSet.class, original, callable);

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
    public void doOnCloseHandlesExceptionDelegates() throws Exception {
        final ResultSet original = givenResultSet();
        final Callable callable = givenCallable();

        final ResultSet actual = doOnClose(ResultSet.class, original, callable);

        doThrow(new SQLException("test")).when(original).getInt(666);
        assertThat(() -> actual.getInt(666), throwsException(SQLException.class, "test"));
        verify(original, times(1)).getInt(666);
    }

    @Test
    public void doOnCloseHandlesExpectedExceptionsOnCallables() throws Exception {
        final ResultSet original = givenResultSet();
        final Callable callable = givenCallable();

        final ResultSet actual = doOnClose(ResultSet.class, original, callable);

        doThrow(new SQLException("test")).when(callable).call();
        assertThat(actual::close, throwsException(SQLException.class, "test"));
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    public void doOnCloseHandlesUnexpectedExceptionsOnCallables() throws Exception {
        final ResultSet original = givenResultSet();
        final Callable callable = givenCallable();

        final ResultSet actual = doOnClose(ResultSet.class, original, callable);

        doThrow(new IOException("test")).when(callable).call();
        assertThat(actual::close, throwsException(UndeclaredThrowableException.class));
        verify(original, times(1)).close();
        verify(callable, times(1)).call();
    }

    @Test
    public void resolveCloseMethodOfHandlesMethodDoesNotExist() throws Exception {
        assertThat(() -> resolveCloseMethodOf(Object.class), throwsException(IllegalStateException.class));
    }

    @Test
    public void constructor() throws Exception {
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