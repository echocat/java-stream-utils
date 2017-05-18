package org.echocat.jsu.support;

import javax.annotation.Nullable;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlFunction<T, R> {

    @Nullable
    R apply(T t) throws SQLException;

}
