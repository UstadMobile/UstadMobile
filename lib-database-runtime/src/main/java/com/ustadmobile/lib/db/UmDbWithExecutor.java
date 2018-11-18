package com.ustadmobile.lib.db;

/**
 * Represents a database that has an executor service (e.g. Android or JDBC).
 */
public interface UmDbWithExecutor {

    /**
     * Execute the given runnable (e.g. a query or set of queries) on the executor service
     *
     * @param runnable Runnable to execute
     */
    void execute(Runnable runnable);

}
