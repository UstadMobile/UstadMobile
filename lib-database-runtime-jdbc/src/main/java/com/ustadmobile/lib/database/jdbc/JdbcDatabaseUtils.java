package com.ustadmobile.lib.database.jdbc;

import com.ustadmobile.lib.db.DoorUtils;
import com.ustadmobile.lib.db.UmDbMigration;
import com.ustadmobile.lib.db.UmDbType;
import com.ustadmobile.lib.db.UmDbWithSyncableInsertLock;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Utility methods used to run databases on JDBC
 */
public class JdbcDatabaseUtils {

    public static final String PRODUCT_NAME_POSTGRES = "PostgreSQL";

    public static final String PRODUCT_NAME_SQLITE = "SQLite";

    public static final String[] SUPPORTED_DB_PRODUCT_NAMES = new String[]{PRODUCT_NAME_POSTGRES,
            PRODUCT_NAME_SQLITE};

    private static final Pattern POSTGRES_SELECT_IN_PATTERN = Pattern.compile("IN(\\s*)\\((\\s*)\\?(\\s*)\\)",
            Pattern.CASE_INSENSITIVE);

    private static final String POSTGRES_SELECT_IN_REPLACEMENT = "IN (SELECT UNNEST(?))";

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
            ResultSet tableResult = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
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
            if(closeable != null)
                closeable.close();
        }catch(Exception e) {
            //whatever it was, it was already closed (or something underlyign was closed).
            // This method is called closeQuietly
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
        List<DbChangeListenerRequest> listenerList = new ArrayList<>(listeners.values());

        for(DbChangeListenerRequest request : listenerList) {
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


    /**
     * Reformat a select query that uses a select IN .. array parameter type query. When using
     * SQLite on Android/JDBC we can use SELECT .. WHERE uid in (:arrayParameter). On Postgres,
     * this needs to be SELECT .. WHERE uid in (select unnest(:arrayParameter)).
     *
     * @param querySql Original SQL query to format
     * @return reformatted SQL query (converted using a pre-compiled regular expression)
     */
    public static String convertSelectInForPostgres(String querySql) {
        Matcher matcher = POSTGRES_SELECT_IN_PATTERN.matcher(querySql);
        querySql = matcher.replaceAll(POSTGRES_SELECT_IN_REPLACEMENT);

        return querySql;
    }

    /**
     * Check if a string list contains a given entry, ignoring case.
     *
     * Useful for checking if a list of tables contains the given table, as SQL is not supposed to
     * be case sensitive.
     *
     * @param list list of strings
     * @param str entry to look for
     * @return true if any string in the list equals (ignoring case) str, false otherwise
     */
    public static boolean listContainsStringIgnoreCase(List<String> list, String str) {
        for(String listStr : list) {
            if(listStr != null && listStr.equalsIgnoreCase(str))
                return true;
        }

        return false;
    }

    public static void setIsMasterFromJndi(UmJdbcDatabase db, String dbName,
                                           InitialContext initialContext) {
        if(db instanceof UmSyncableDatabase) {
            try {
                Object _masterDbVal = initialContext.lookup("java:/comp/env/umdb/" + dbName +
                        "/isMaster");
                if(_masterDbVal instanceof Boolean) {
                    ((UmSyncableDatabase)db).setMaster((Boolean)_masterDbVal);
                }
            }catch(NamingException e) {
                System.err.println("WARNING: could not look/set if db is master: " +
                        e.getExplanation());
            }
        }
    }

    /**
     * Lock the database for syncable entity insert if this is an SQLite database. This is called by
     * generated code.
     *
     * @param db database being used
     */
    @SuppressWarnings("unused")
    public static void lockSyncableInsertsIfSqlite(UmJdbcDatabase db) {
        if(db.getDbType() == UmDbType.TYPE_SQLITE)
            ((UmDbWithSyncableInsertLock)db).lockSyncableInserts();
    }

    /**
     * Unlock the database for syncable entity insert if this is an SQLite database. This is called
     * by generated code
     *
     * @param db database being used
     */
    @SuppressWarnings("unused")
    public static void unlockSyncableInsertsIfSqlite(UmJdbcDatabase db) {
        if(db.getDbType() == UmDbType.TYPE_SQLITE)
            ((UmDbWithSyncableInsertLock)db).unlockSyncableInserts();
    }


    /**
     * Update the sequence that is used on Postgres for generating syncable primary keys. This
     * method is called from generated code (specifically, the clearAll method). This should be
     * done whenever the syncDeviceBits are changed.
     *
     * @param tableId table id for the entity to alter the sequence for
     * @param syncDeviceBits the new value for syncdevicebits
     * @param stmt JDBC statement object
     * @throws SQLException if there is an SQLException when running the SQL
     */
    @SuppressWarnings("unused")
    public static void updatePostgresSyncablePrimaryKeySequence(int tableId,
                                                                int syncDeviceBits,
                                                                Statement stmt) throws SQLException{
        stmt.execute("ALTER SEQUENCE spk_seq_" +
                tableId + DoorUtils.generatePostgresSyncablePrimaryKeySequenceParameters(syncDeviceBits));
    }



    public static UmDbMigration pickNextMigration(int currentVersion, List<UmDbMigration> migrations) {
        List<UmDbMigration> candidateMigrations = new ArrayList<>();
        for(UmDbMigration migration : migrations){
            if(migration.getFromVersion() == currentVersion)
                candidateMigrations.add(migration);
        }

        UmDbMigration bestMigration = null;
        for(UmDbMigration migration : candidateMigrations){
            if(bestMigration == null || migration.getToVersion() > bestMigration.getToVersion())
                bestMigration = migration;
        }

        return bestMigration;
    }




}
