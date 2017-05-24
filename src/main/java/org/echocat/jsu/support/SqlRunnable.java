package org.echocat.jsu.support;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlRunnable {

    void run() throws SQLException;

}
