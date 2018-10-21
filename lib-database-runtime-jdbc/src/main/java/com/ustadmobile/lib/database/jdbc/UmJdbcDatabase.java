package com.ustadmobile.lib.database.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Common methods that must be implemented by JDBC database classes.
 */
public interface UmJdbcDatabase {

    /**
     * Get a JDBC connection. This should return a connection from the underlying DataSource
     *
     * @return a JDBC connection
     *
     * @throws SQLException if there is an underlying SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * Get an ExecutorService to use to run asynchronous queries
     *
     * @return a shared ExecutorService to use to run asynchronous queries
     */
    ExecutorService getExecutor();

    /**
     * Add a change listener to receive events if particular tables are changed
     *
     * @param listenerRequest Change listener request to add
     */
    void addDbChangeListener(JdbcDatabaseUtils.DbChangeListenerRequest listenerRequest);

    /**
     * Remove a change listener to stop receiving events if particular tables are changed
     *
     * @param listenerRequest
     */
    void removeDbChangeListener(JdbcDatabaseUtils.DbChangeListenerRequest listenerRequest);

    /**
     * This method is to be called by DAOs when the table has b
     * @param changedTables
     */
    void handleTablesChanged(String... changedTables);

    /**
     * Indicates whether or not the connection natively supports SQL array type. If not, then the
     * PreparedStatementArrayProxy must be used.
     *
     * @return
     */
    boolean isArraySupported();

}
