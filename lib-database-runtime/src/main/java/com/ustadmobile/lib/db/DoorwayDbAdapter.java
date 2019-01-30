package com.ustadmobile.lib.db;

/**
 * This interface is supplied to onOpen, onCreate, and migration methods to provide a common wrapper
 * for these interface to work on the underlying database on any platform.
 */
public interface DoorwayDbAdapter {

    /**
     * Execute the given SQL string (not returning results)
     *
     * @param sql
     */
    void execSql(String sql);

}
