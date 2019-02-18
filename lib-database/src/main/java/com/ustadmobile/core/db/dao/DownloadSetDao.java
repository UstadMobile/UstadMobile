package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.db.entities.DownloadSet;

import java.util.List;

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


    @UmQuery("SELECT meteredNetworkAllowed FROM DownloadSet  WHERE dsUid = :dsUid")
    public abstract UmLiveData<Boolean> getLiveMeteredNetworkAllowed(long dsUid);


    @UmQuery("UPDATE DownloadSet SET meteredNetworkAllowed = :meteredNetworkAllowed WHERE dsUid = :dsUid")
    public abstract void setMeteredConnectionBySetUid(long dsUid, boolean meteredNetworkAllowed);

    @UmQuery("SELECT dsUid FROM DownloadSet WHERE dsRootContentEntryUid = :contentEntryUid")
    public abstract long findDownloadSetUidByRootContentEntryUid(long contentEntryUid);


    @UmTransaction
    public void cleanupUnused(long downloadSetUid,int status){
        deleteUnusedDownloadJobItems(downloadSetUid, status,null);
        deleteUnusedDownloadJobs(downloadSetUid,status,null);
        deleteUnusedDownloadSetItems(downloadSetUid,null);
        deleteUnusedDownloadSet(downloadSetUid,null);
    }



    @UmQuery("DELETE FROM DownloadJobItem " +
            "WHERE " +
            "djiStatus = :status AND " +
            "djiDsiUid IN (SELECT dsiUid FROM DownloadSetItem WHERE dsiDsUid = :downloadSetUid)")
    public abstract void deleteUnusedDownloadJobItems(long downloadSetUid,int status, UmCallback<Integer> callback);

    @UmQuery("DELETE FROM DownloadJob WHERE djStatus = :status AND djDsUid = :downloadSetUid")
    public abstract void deleteUnusedDownloadJobs(long downloadSetUid, int status ,UmCallback<Integer> callback);


    @UmQuery("DELETE FROM DownloadSetItem WHERE dsiDsUid = :downloadSetUid " +
            "AND NOT EXISTS(SELECT djiUid FROM DownloadJobItem WHERE djiDsiUid = DownloadSetItem.dsiUid)")
    public abstract void deleteUnusedDownloadSetItems(long downloadSetUid, UmCallback<Integer> callback);

    @UmQuery("DELETE FROM DownloadSet WHERE dsUid = :downloadSetUid AND " +
            "NOT EXISTS(SELECT dsiUid FROM DownloadSetItem WHERE dsiDsUid = :downloadSetUid)")
    public abstract void deleteUnusedDownloadSet(long downloadSetUid, UmCallback<Integer> callback);

}
