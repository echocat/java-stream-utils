package org.echocat.jsu;

import org.junit.Test;

import static org.echocat.unittest.utils.matchers.ThrowsException.throwsException;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AutoCloseableUtilsUnitTest {

    @Test
    public void closeQuietlyClosesAutoCloseables() throws Exception {
        final AutoCloseable autoCloseable = mock(AutoCloseable.class);
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
        final AutoCloseable autoCloseable = mock(AutoCloseable.class);
        doThrow(new RuntimeException("test")).when(autoCloseable).close();
        AutoCloseableUtils.closeQuietly(autoCloseable);
    }

    @Test
    public void closeQuietlyDoesNotIgnoreErrorsOfAutoCloseables() throws Exception {
        final AutoCloseable autoCloseable = mock(AutoCloseable.class);
        doThrow(new AssertionError("test")).when(autoCloseable).close();
        assertThat(() -> AutoCloseableUtils.closeQuietly(autoCloseable), throwsException(AssertionError.class, "test"));
    }

    @Test
    public void constructor() throws Exception {
        new AutoCloseableUtils();
    }

}