package org.echocat.jsu.support;

import javax.annotation.Nonnull;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

public class UncheckedSqlException extends RuntimeException {

    public UncheckedSqlException(@Nonnull String message, @Nonnull SQLException cause) {
        super(requireNonNull(message), requireNonNull(cause));
    }

    public UncheckedSqlException(@Nonnull SQLException cause) {
        super(requireNonNull(cause));
    }

}
