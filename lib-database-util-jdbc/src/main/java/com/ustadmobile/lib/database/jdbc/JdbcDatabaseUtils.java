package com.ustadmobile.lib.database.jdbc;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Utility methods used to run databases on JDBC
 */
public class JdbcDatabaseUtils {

    /**
     * Represents a request to monitor specific tables on the database, and receive an event if they
     * have changed. This is
     */
    public static class DbChangeListenerRequest {

        private List<String> tableNames;

        private DbChangeListener listener;

        /**
         * Create a new DbChangeListenerRequest
         *
         * @param tableNames list of table names to monitor for changes
         * @param listener listener that will be called when changes occur
         */
        public DbChangeListenerRequest(List<String> tableNames, DbChangeListener listener) {
            this.listener = listener;
            this.tableNames = tableNames;
        }

        /**
         * Get a list of the table names this request is monitoring
         * @return a list of the table names this request is monitoring
         */
        public List<String> getTableNames() {
            return tableNames;
        }

        /**
         * Set the list of the table names this request is monitoring
         * @param tableNames  list of the table names this request is monitoring
         */
        public void setTableNames(List<String> tableNames) {
            this.tableNames = tableNames;
        }

        /**
         * Get the listener that will be called when changes occur.
         * @return the listener that will be called when changes occur.
         */
        public DbChangeListener getListener() {
            return listener;
        }

        /**
         * Set the listener that will be called when changes occur.
         * @param listener the listener that will be called when changes occur.
         */
        public void setListener(DbChangeListener listener) {
            this.listener = listener;
        }
    }

    /**
     * List the tables available from a given JDBC connection
     *
     * @param connection JDBC connection to list tables from
     * @return A string list of table names
     * @throws SQLException If an SQLException occurs
     */
    public static List<String> getTableNames(Connection connection) throws SQLException{
        List<String> tableNames = new ArrayList<>();

        try(
            ResultSet tableResult = connection.getMetaData().getTables(null, null, "%", null);
        ) {
            while(tableResult.next()){
                tableNames.add(tableResult.getString("TABLE_NAME"));
            }
        }catch(SQLException e) {
            throw e;
        }

        return tableNames;
    }

    /**
     * Close the given resource, and dump any exception
     *
     * @param closeable resource to close
     */
    public static void closeQuietly(AutoCloseable closeable) {
        try {
            closeable.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle adding the given change listener request to a map of listeners
     *
     * @param listenerRequest The listener request to be added
     * @param listeners The listener map that it should be added to
     */
    public static void addDbChangeListener(DbChangeListenerRequest listenerRequest,
                                           Map<DbChangeListener, DbChangeListenerRequest> listeners) {
        listeners.put(listenerRequest.getListener(), listenerRequest);
    }

    /**
     * Handle removing the given change listener request from a map of listeners
     *
     * @param listenerRequest The listener request to remove
     * @param listeners The listener map to remove it from
     */
    public static void removeDbChangeListener(DbChangeListenerRequest listenerRequest,
                                      Map<DbChangeListener, DbChangeListenerRequest> listeners) {
        listeners.remove(listenerRequest);
    }

    /**
     * Handle sending out events when a table has changed. This will dispatch events to those that
     * are listening for changes to any of the tables that have been changed (only).
     *
     * @param listeners Map of listeners
     * @param tablesChanged List of tables that have been changed
     */
    public static void handleTablesChanged(Map<DbChangeListener, DbChangeListenerRequest> listeners,
                                           String... tablesChanged) {
        List<String> tablesChangedList = Arrays.asList(tablesChanged);

        for(DbChangeListenerRequest request : listeners.values()) {
            if(!Collections.disjoint(tablesChangedList, request.getTableNames())){
                request.getListener().onTablesChanged(tablesChangedList);
            }
        }
    }

    /**
     * Used when calling statement.executeBatch
     *
     * @param updateTotals int array of update totals to sum
     *
     * @return sum of ints in the array
     */
    public static int sumUpdateTotals(int[] updateTotals) {
        int result = 0;
        for(int updateTotal: updateTotals) {
            result += updateTotal;
        }

        return result;
    }

    /**
     * Determine if java.sql.Array is supported by the underling DataSource
     *
     * @param dataSource DataSource to test for support
     *
     * @return true if the Java.sql.Array is supported, false otherwise
     */
    public static boolean isArraySupported(DataSource dataSource) {
        boolean supported = false;
        try (
            Connection connection = dataSource.getConnection();
        ){
            Array sqlArray = connection.createArrayOf("VARCHAR", new String[]{"hello"});
            supported = sqlArray != null;
        }catch(SQLException e) {
            System.err.println("SQL Exception checking for array support: " + e.toString());
        }

        return supported;
    }

    /**
     * Free the given java.sql.Array object
     *
     * @param array Array to free
     */
    public static void freeArrayQuietly(Array array) {
        if(array != null) {
            try {
                array.free();
            }catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
