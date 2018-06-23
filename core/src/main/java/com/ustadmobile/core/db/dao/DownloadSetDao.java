package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmOnConflictStrategy;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
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
     * Insert or replace a DownloadSet
     *
     * @param set DownloadSet to insert. If the id is non-zero, then the given id will be replaced
     * @return The id of the inserted or replaced DownloadSet
     */
    @UmInsert(onConflict = UmOnConflictStrategy.REPLACE)
    public abstract long insertOrReplace(DownloadSet set);

    /**
     * Update all fields of the given DownloadSet
     *
     * @param set
     */
    @UmUpdate
    public abstract void update(DownloadSet set);

    /**
     * Find a DownloadSet by the primary key
     *
     * @param id the id (primary key) of the given DownloadSet
     * @return DownloadSetWithRelations representing the given DownloadSet if found, otherwise null
     */
    @UmQuery("SELECT * FROM DownloadSet WHERE id = :id")
    public abstract DownloadSet findById(int id);

    /**
     * Find a DownloadSet by the root OPDS UUID
     *
     * @param rootEntryUuid root OPDS UUID to search by
     * @return The DownloadSet matching the given root OPDS UUID, otherwise null
     */
    @UmQuery("SELECT * FROM DownloadSet WHERE rootOpdsUuid = :rootEntryUuid")
    public abstract DownloadSet findByRootEntry(String rootEntryUuid);

    /**
     * Get a UmLiveData object for the given download set
     * @param id Primary key (id) of the given downloadset
     * @return UmLiveData object for the given DownloadSet
     */
    @UmQuery("SELECT * From DownloadSet WHERE id = :id")
    public abstract UmLiveData<DownloadSet> getByIdLive(int id);

}
