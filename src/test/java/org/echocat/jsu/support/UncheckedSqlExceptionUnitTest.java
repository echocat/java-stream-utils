package org.echocat.jsu.support;

import org.junit.Test;

import java.sql.SQLException;

import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.junit.Assert.assertThat;

public class UncheckedSqlExceptionUnitTest {

    @Test
    public void constructorWithCauseOnly() throws Exception {
        final SQLException cause = new SQLException("test");

        final UncheckedSqlException actual = new UncheckedSqlException(cause);

        assertThat(actual.getCause(), isSameAs(cause));
        assertThat(actual.getMessage(), isEqualTo(SQLException.class.getName() + ": " + cause.getMessage()));
    }

    @Test
    public void constructorWithMessageAndCause() throws Exception {
        final SQLException cause = new SQLException("test");

        final UncheckedSqlException actual = new UncheckedSqlException("myMessage", cause);

        assertThat(actual.getCause(), isSameAs(cause));
        assertThat(actual.getMessage(), isEqualTo("myMessage"));
    }

}