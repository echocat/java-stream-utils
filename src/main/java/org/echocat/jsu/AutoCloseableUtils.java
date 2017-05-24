package org.echocat.jsu;

import org.echocat.jsu.support.SqlRunnable;
import org.echocat.jsu.support.UncheckedSqlException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.reflect.Proxy.newProxyInstance;

public final class AutoCloseableUtils {

    @Nonnull
    private static final Method CLOSE_METHOD = resolveCloseMethodOf(AutoCloseable.class);
    @Nonnull
    private static final Method CLOSEABLE_CLOSE_METHOD = resolveCloseMethodOf(Closeable.class);
    @Nonnull
    private static final Method RESULT_SET_CLOSE_METHOD = resolveCloseMethodOf(ResultSet.class);
    @Nonnull
    private static final Method STATEMENT_CLOSE_METHOD = resolveCloseMethodOf(Statement.class);
    @Nonnull
    private static final Method CONNECTION_CLOSE_METHOD = resolveCloseMethodOf(Connection.class);

    public static void closeQuietly(@Nullable Object closeable) {
        try {
            if (closeable instanceof AutoCloseable) {
                ((AutoCloseable) closeable).close();
            }
        } catch (final Exception ignored) {}
    }

    @Nonnull
    public static <T extends AutoCloseable> T doOnClose(@Nonnull Class<T> type, @Nonnull T instance, @Nullable SqlRunnable... runnablesExecuteOnClose) {
        final Method specificCloseMethod = typeSpecificCloseMethodOf(instance);
        //noinspection unchecked
        return (T) newProxyInstance(type.getClassLoader(), new Class[]{type}, (proxy, method, args) -> {
            try {
                return method.invoke(instance, args);
            } finally {
                if (CLOSE_METHOD.equals(method) || method.equals(specificCloseMethod)) {
                    run(runnablesExecuteOnClose);
                }
            }
        });
    }

    @Nullable
    static Method typeSpecificCloseMethodOf(@Nonnull Object instance) {
        if (instance instanceof ResultSet) {
            return RESULT_SET_CLOSE_METHOD;
        }
        if (instance instanceof Statement) {
            return STATEMENT_CLOSE_METHOD;
        }
        if (instance instanceof Connection) {
            return CONNECTION_CLOSE_METHOD;
        }
        if (instance instanceof Closeable) {
            return CLOSEABLE_CLOSE_METHOD;
        }
        return null;
    }

    static void run(@Nullable SqlRunnable... runnablesExecuteOnClose) throws UncheckedSqlException {
        if (runnablesExecuteOnClose != null) {
            for (final SqlRunnable runnable : runnablesExecuteOnClose) {
                try {
                    runnable.run();
                } catch (final SQLException e) {
                    throw new UncheckedSqlException(e);
                }
            }
        }
    }

    @Nonnull
    private static Method resolveCloseMethodOf(@Nonnull Class<?> type) {
        try {
            return type.getMethod("close");
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException("Could not find close() method of " + type.getName() + ".", e);
        }
    }

}
