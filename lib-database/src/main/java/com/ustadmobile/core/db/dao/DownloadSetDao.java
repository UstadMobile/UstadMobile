package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadSet;

/**
 * DAO for the DownloadSet entity.
 */
@UmDao
public abstract class DownloadSetDao {

    /**
     * Insert a new DownloadSet
     *
     * @param set DownloadSet to insert
     * @return id (primary key) of the inserted object
     */
    @UmInsert
    public abstract long insert(DownloadSet set);

    /**
     * Find a DownloadSet by the primary key
     *
     * @param dsUid the id (primary key) of the given DownloadSet
     * @return DownloadSetWithRelations representing the given DownloadSet if found, otherwise null
     */
    @UmQuery("SELECT * FROM DownloadSet WHERE dsUid = :dsUid")
    public abstract DownloadSet findByUid(int dsUid);



}
