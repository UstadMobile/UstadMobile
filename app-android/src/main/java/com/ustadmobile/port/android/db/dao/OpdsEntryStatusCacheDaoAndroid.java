package com.ustadmobile.port.android.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheDao;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;

import java.util.List;

/**
 * Created by mike on 3/24/18.
 */
@Dao
public abstract class OpdsEntryStatusCacheDaoAndroid extends OpdsEntryStatusCacheDao {

    @Insert
    @Override
    public abstract void insert(OpdsEntryStatusCache status);

    @Override
    @Insert
    public abstract void insertList(List<OpdsEntryStatusCache> statuses);

    @Override
    @Query("SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract Integer findUidByEntryId(String entryId);

    @Override
    public UmLiveData<OpdsEntryStatusCache> findByEntryIdLive(String entryId) {
        return new UmLiveDataAndroid<>(findByEntryIdLive_RoomImpl(entryId));
    }

    @Query("SELECT * From OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract LiveData<OpdsEntryStatusCache> findByEntryIdLive_RoomImpl(String entryId);

    @Override
    @Query("SELECT * From OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract OpdsEntryStatusCache findByEntryId(String entryId);

    @Override
    @Query("SELECT * FROM OpdsEntryStatusCache WHERE statusCacheUid = :statusCacheUid")
    public abstract OpdsEntryStatusCache findByStatusCacheUid(int statusCacheUid);

    @Override
    @Query("SELECT OpdsEntryStatusCache.* FROM OpdsEntryStatusCache " +
            "            LEFT JOIN DownloadJobItem ON DownloadJobItem.entryId = OpdsEntryStatusCache.statusEntryId " +
            "            WHERE DownloadJobItem.id = :downloadJobItemId")
    public abstract OpdsEntryStatusCache findByDownloadJobItemId(int downloadJobItemId);

    @Override
    @Query("SELECT DISTINCT OpdsEntry.entryId " +
            " FROM OpdsEntry LEFT JOIN OpdsEntryStatusCache ON OpdsEntry.entryId = OpdsEntryStatusCache.statusEntryId " +
            " WHERE OpdsEntry.entryId IN (:entryIds) AND OpdsEntryStatusCache.statusEntryId IS NULL")
    public abstract List<String> findEntryIdsNotPresent(List<String> entryIds);


    /**
     *
     * @param entryIds
     * @return
     */
    @Override
    @Query("SELECT * FROM OpdsEntryStatusCache WHERE statusEntryId IN (:entryIds)")
    public abstract List<OpdsEntryStatusCache> findByEntryIdList(List<String> entryIds);

    @Override
    @Query("UPDATE OpdsEntryStatusCache " +
            "SET " +
            "sizeIncDescendants = sizeIncDescendants  + :deltaSizeIncDescendants, " +
            "entriesWithContainerIncDescendants = entriesWithContainerIncDescendants + :deltaEntriesWithContainerIncDescendants " +
            "WHERE statusCacheUid IN \n" +
            " (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    public abstract void handleOpdsEntryLoadedUpdateIncAncestors(String entryId,
                                                                 int deltaSizeIncDescendants,
                                                                 int deltaEntriesWithContainerIncDescendants);

    @Override
    @Query("UPDATE OpdsEntryStatusCache " +
            "SET " +
            "entrySize = :size, " +
            "entryHasContainer = :hasContainer " +
            "WHERE statusEntryId = :entryId")
    public abstract void handleOpdsEntryLoadedUpdateEntry(String entryId, long size, boolean hasContainer);


    @Override
    @Transaction
    public void handleDownloadJobQueued(int downloadJobId) {
        super.handleDownloadJobQueued(downloadJobId);
    }

    @Override
    @Query("Update OpdsEntryStatusCache " +
            "SET " +
            "sizeIncDescendants = sizeIncDescendants+ (" +
            "SELECT " +
                "(DownloadJobItem.downloadLength - OpdsEntryStatusCache.entrySize) AS deltaTotalSize " +
                "FROM " +
                "DownloadJobItem LEFT JOIN OpdsEntryStatusCache ON DownloadJobItem.entryId = OpdsEntryStatusCache.statusEntryId " +
                "WHERE " +
                "DownloadJobItem.id = :downloadJobId " +
            ")," +
            "containersDownloadPendingIncAncestors = containersDownloadPendingIncAncestors + :deltaContainersDownloadPending " +
            "WHERE statusCacheUid IN " +
            "  (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = :statusCacheUid)")
    protected abstract void updateOnDownloadJobItemQueuedIncAncestors(int statusCacheUid, int downloadJobId, int deltaContainersDownloadPending);

    @Override
    @Query("Update OpdsEntryStatusCache " +
            "SET " +
            "entrySize = (SELECT downloadLength FROM DownloadJobItem WHERE id = :downloadJobId), " +
            "entryContainerDownloadPending = 1 " +
            " WHERE statusCacheUid = :statusCacheUid")
    protected abstract void updateOnDownloadJobItemQueuedEntry(int statusCacheUid, int downloadJobId);

    @Transaction
    public void handleDownloadJobProgress(int entryStatusCacheUid, int downloadJobItemId){
        super.handleDownloadJobProgress(entryStatusCacheUid, downloadJobItemId);
    }

    @Override
    @Query("Update OpdsEntryStatusCache " +
            "SET " +
            "pendingDownloadBytesSoFarIncDescendants= pendingDownloadBytesSoFarIncDescendants + (" +
            "(SELECT downloadedSoFar FROM DownloadJobItem WHERE id = :downloadJobItemId) - " +
            "(SELECT entryPendingDownloadBytesSoFar FROM OpdsEntryStatusCache WHERE statusCacheUid = :entryStatusCacheId))" +
            "WHERE statusCacheUid IN " +
            "(SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = :entryStatusCacheId)")
    public abstract void updateActiveBytesDownloadedSoFarIncAncestors(int entryStatusCacheId, int downloadJobItemId);

    @Query("UPDATE OpdsEntryStatusCache " +
            "SET entryPendingDownloadBytesSoFar = (SELECT downloadedSoFar FROM DownloadJobItem WHERE id = :downloadJobItemId) " +
            "WHERE statusCacheUid = :entryStatusCacheId")
    public abstract void updateActiveBytesDownloadedSoFarEntry(int entryStatusCacheId, int downloadJobItemId);



    @Transaction
    public void handleContainerDownloadedOrDiscovered(OpdsEntryStatusCache entryStatusCache, ContainerFile containerFile) {
        super.handleContainerDownloadedOrDiscovered(entryStatusCache, containerFile);
    }


    @Query("UPDATE OpdsEntryStatusCache\n" +
            "SET\n" +
            "pendingDownloadBytesSoFarIncDescendants = pendingDownloadBytesSoFarIncDescendants + :deltaPendingDownloadBytesSoFar,\n" +
            "containersDownloadPendingIncAncestors = containersDownloadPendingIncAncestors + :deltacontainersDownloadPending,\n" +
            "containersDownloadedSizeIncDescendants = containersDownloadedSizeIncDescendants + :deltaContainersDownloadedSize,\n" +
            "containersDownloadedIncDescendants = containersDownloadedIncDescendants + :deltaContainersDownloaded,\n" +
            "sizeIncDescendants = sizeIncDescendants + :deltaSize\n" +
            "WHERE statusCacheUid IN\n" +
            "(SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    @Override
    public abstract void updateOnContainerStatusChangedIncAncestors(String entryId, long deltaPendingDownloadBytesSoFar,
                                                                    int deltacontainersDownloadPending,
                                                                    long deltaContainersDownloadedSize,
                                                                    long deltaContainersDownloaded, long deltaSize);


    @Query("UPDATE OpdsEntryStatusCache " +
            "SET " +
            "entryPendingDownloadBytesSoFar = :pendingDownloadBytesSoFar, " +
            "entryContainerDownloadPending = :containerDownloadPending,  " +
            "entryContainerDownloadedSize = :containerDownloadedSize, " +
            "entryContainerDownloaded = :containerDownloaded," +
            "entrySize = :containerDownloadedSize " +
            "WHERE statusCacheUid = :statusCacheUid")
    @Override
    public abstract void updateOnContainerStatusChangedEntry(int statusCacheUid,
                                                             long pendingDownloadBytesSoFar,
                                                             boolean containerDownloadPending,
                                                             long containerDownloadedSize,
                                                             boolean containerDownloaded);
}
