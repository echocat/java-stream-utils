package org.echocat.jsu;

import static org.echocat.jsu.AutoCloseableUtils.closeQuietly;
import static org.echocat.jsu.StreamUtils.generate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import org.echocat.jsu.support.SqlFunction;
import org.echocat.jsu.support.UncheckedSqlException;

public final class JdbcUtils {

    @Nonnull
    public static Stream<ResultSet> toStream(@Nonnull ResultSet resultSet) {
        return generate(() -> {
            try {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(resultSet);
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

    @Nonnull
    public static <T> Stream<T> toStream(@Nonnull ResultSet resultSet, @Nonnull SqlFunction<ResultSet, T> mapper) {
        return toStream(resultSet, (Function<ResultSet, T>) input -> {
            try {
                return mapper.apply(input);
            } catch (final SQLException e) {
                throw new UncheckedSqlException(e);
            }
        });
    }

}
