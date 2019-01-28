package com.ustadmobile.lib.db;

/**
 * This interface is supplied to onOpen, onCreate, and migration methods to provide a common wrapper
 * for these interface to work on the underlying database on any platform.
 */
public interface DoorwayDbAdapter {

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


}
