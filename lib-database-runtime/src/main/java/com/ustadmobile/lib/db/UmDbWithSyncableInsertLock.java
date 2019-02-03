package com.ustadmobile.lib.db;

/**
 * On SQLite we need to use a lock to avoid concurrent insertion of syncable entities. This is
 * because the triggers use a single table for inserted ids.
 */
public interface UmDbWithSyncableInsertLock {

    /**
     * Lock the database for syncable entity insert
     */
    void lockSyncableInserts();

    /**
     * Unlock the database for syncable entity insert
     */
    void unlockSyncableInserts();

}
