package org.echocat.jsu;

import org.echocat.jsu.support.UncheckedSqlException;

import javax.annotation.Nonnull;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.echocat.jsu.Generator.Value.end;
import static org.echocat.jsu.Generator.Value.valueOf;
import static org.echocat.jsu.StreamUtils.closeQuietly;
import static org.echocat.jsu.StreamUtils.generate;

public final class JdbcUtils {

    @Nonnull
    public static Stream<ResultSet> query(@Nonnull DataSource dataSource, @Nonnull Function<Connection, ResultSet> query) {
        boolean success = false;
        try {
            final Connection connection = dataSource.getConnection();
            try {
                final ResultSet resultSet = query.apply(connection);
                try {
                    final Stream<ResultSet> result = toStream(resultSet)
                        .onClose(() -> closeQuietly(connection));
                    success = true;
                    return result;
                } finally {
                    if (!success) {
                        closeQuietly(resultSet);
                    }
                }
            } finally {
                if (!success) {
                    closeQuietly(connection);
                }
            }
        } catch (final SQLException e) {
            throw new UncheckedSqlException(e);
        }
    }

    @Nonnull
    public static Stream<ResultSet> toStream(@Nonnull ResultSet resultSet) {
        return generate(() -> {
            try {
                if (!resultSet.next()) {
                    return end();
                }
                return valueOf(resultSet);
            } catch (final SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }).onClose(() -> closeQuietly(resultSet));
    }

    @Nonnull
    public static <T> Stream<T> toStream(@Nonnull ResultSet resultSet, @Nonnull Function<ResultSet, T> mapper) {
        return toStream(resultSet)
            .map(mapper);
    }

}
