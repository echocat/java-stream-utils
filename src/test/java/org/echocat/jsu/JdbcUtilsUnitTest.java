package org.echocat.jsu;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.echocat.jsu.JdbcUtils.toStream;
import static org.echocat.unittest.utils.matchers.IsEqualTo.isEqualTo;
import static org.echocat.unittest.utils.matchers.IsSameAs.isSameAs;
import static org.echocat.unittest.utils.matchers.ThrowsException.throwsException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.echocat.jsu.support.SqlFunction;
import org.echocat.jsu.support.UncheckedSqlException;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"InstantiationOfUtilityClass", "ResultOfMethodCallIgnored"})
public class JdbcUtilsUnitTest {

    @Test
    void toStreamSimple() throws Exception {
        final var serial = new AtomicInteger();
        final var resultSet = mock(ResultSet.class);
        doAnswer(invocationOnMock -> serial.getAndIncrement() < 2).when(resultSet).next();

        final var actual = toStream(resultSet);
        final var contents = actual.collect(toList());

        verify(resultSet, times(3)).next();
        assertThat(contents, isEqualTo(asList(resultSet, resultSet)));

        verify(resultSet, times(0)).close();
        actual.close();
        verify(resultSet, times(1)).close();
    }

    @Test
    void toStreamHaveToHandleSqlException() throws Exception {
        final var serial = new AtomicInteger();
        final var resultSet = mock(ResultSet.class);
        doAnswer(invocationOnMock -> {
            if (serial.getAndIncrement() < 2) {
                return true;
            }
            throw new SQLException("test");
        }).when(resultSet).next();

        assertThat(() -> toStream(resultSet)
            .collect(toList()), throwsException(UncheckedSqlException.class, SQLException.class.getName() + ": test"));
    }

    @Test
    void toStreamDoesNotHandleNonSqlExceptions() throws Exception {
        final var serial = new AtomicInteger();
        final var resultSet = mock(ResultSet.class);
        doAnswer(invocationOnMock -> {
            if (serial.getAndIncrement() < 2) {
                return true;
            }
            throw new RuntimeException("test");
        }).when(resultSet).next();

        assertThat(() -> toStream(resultSet)
            .collect(toList()), throwsException(RuntimeException.class, "test"));
    }

    @Test
    void toStreamWithMapper() throws Exception {
        final var serial = new AtomicInteger();
        final var resultSet = mock(ResultSet.class);
        doReturn(true).when(resultSet).next();

        final List<Integer> actual = toStream(resultSet, (SqlFunction<ResultSet, Integer>) current -> {
            assertThat(current, isSameAs(resultSet));
            return serial.getAndIncrement();
        })
            .limit(10)
            .collect(toList());

        assertThat(actual, isEqualTo(asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    void toStreamWithMapperHandlesSqlException() throws Exception {
        final var resultSet = mock(ResultSet.class);
        doReturn(true).when(resultSet).next();

        assertThat(() -> toStream(resultSet, (SqlFunction<ResultSet, Integer>) current -> {
            throw new SQLException("test");
        }).collect(toList()), throwsException(UncheckedSqlException.class, SQLException.class.getName() + ": test"));
    }

    @Test
    void toStreamWithMapperDoesNotHandleNonSqlExceptions() throws Exception {
        final var resultSet = mock(ResultSet.class);
        doReturn(true).when(resultSet).next();

        assertThat(() -> toStream(resultSet, (SqlFunction<ResultSet, Integer>) current -> {
            throw new RuntimeException("test");
        }).collect(toList()), throwsException(RuntimeException.class, "test"));
    }

    @Test
    public void constructor() {
        new JdbcUtils();
    }

}