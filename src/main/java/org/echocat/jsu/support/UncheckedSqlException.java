package org.echocat.jsu.support;

import java.util.Objects;

public class UncheckedSqlException extends RuntimeException {

    public UncheckedSqlException(String message, Throwable cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public UncheckedSqlException(Throwable cause) {
        super(Objects.requireNonNull(cause));
    }

}
