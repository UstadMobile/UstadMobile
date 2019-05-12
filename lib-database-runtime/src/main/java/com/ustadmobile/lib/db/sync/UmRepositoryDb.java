package com.ustadmobile.lib.db.sync;

/**
 * Represents a repository version of the database. Properties are used to support returning an
 * already instantiated instance of a database as a repository, if it exists.
 */
public interface UmRepositoryDb {

    /**
     *
     * @return
     */
    String getBaseUrl();

    String getAuth();

    Object getDatabase();
}
