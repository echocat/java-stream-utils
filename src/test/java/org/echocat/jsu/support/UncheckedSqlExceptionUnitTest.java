package org.echocat.jsu.support;

import java.sql.SQLException;

import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UncheckedSqlExceptionUnitTest {

    @Test
    void constructorWithCauseOnly() {
        final var cause = new SQLException("test");

        final var actual = new UncheckedSqlException(cause);

        assertThat(actual.getCause(), isSameAs(cause));
        assertThat(actual.getMessage(), isEqualTo(SQLException.class.getName() + ": " + cause.getMessage()));
    }

    @Test
    void constructorWithMessageAndCause() {
        final var cause = new SQLException("test");

        final var actual = new UncheckedSqlException("myMessage", cause);

        assertThat(actual.getCause(), isSameAs(cause));
        assertThat(actual.getMessage(), isEqualTo("myMessage"));
    }

}