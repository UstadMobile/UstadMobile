package com.ustadmobile.lib.db;

/**
 * This interface is supplied to onOpen, onCreate, and migration methods to provide a common wrapper
 * for these interface to work on the underlying database on any platform.
 */
public interface DoorDbAdapter {

    /**
     * Execute the given SQL string (not returning results)
     *
     * @param sql SQL to run
     */
    void execSql(String sql);

    /**
     * Get the type of underlying database (SQLite or Postgres)
     *
     * @see UmDbType
     * @return UmDbType constant representing the database type
     */
    int getDbType();

    /**
     * Select a single column from a single row in the database, and return the result as a String
     *
     * @param sql The SQL query to run
     *
     * @return the value of the first row of the query as a string, or null if there are no matching
     * rows
     */
    String selectSingleValue(String sql);



}
