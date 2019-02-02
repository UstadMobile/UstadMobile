package com.ustadmobile.core.db.dao;

import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;

import java.util.List;


/**
 * DAO for managing OpdsEntryStatusCache entities.
 *
 * @see OpdsEntryStatusCache
 */
@UmDao
public abstract class OpdsEntryStatusCacheDao {

    /**
     * Insert a new OpdsEntryStatusCache object
     *
     * @param status
     */
    @UmInsert
    public abstract void insert(OpdsEntryStatusCache status);

    /**
     * Insert a list of new OpdsEntryStatusCache objects
     * @param statuses
     */
    @UmInsert
    public abstract void insertList(List<OpdsEntryStatusCache> statuses);

    /**
     *
     * @param status
     */
    @UmUpdate
    public abstract void update(OpdsEntryStatusCache status);




    /**
     * Get the OpdsEntryStatusCache object for the given entryId
     *
     * @param entryId the entryId to search by
     *
     * @return OpdsEntryStatusCache if the given entry is present, null otherwise
     */
    @UmQuery("SELECT * From OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract OpdsEntryStatusCache findByEntryId(String entryId);


}
