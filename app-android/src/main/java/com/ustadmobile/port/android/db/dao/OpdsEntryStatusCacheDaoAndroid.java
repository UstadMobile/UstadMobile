package com.ustadmobile.port.android.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.ustadmobile.core.db.dao.OpdsEntryStatusCacheDao;
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
    @Query("SELECT * From OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract OpdsEntryStatusCache findByEntryId(String entryId);

    @Override
    @Query("SELECT OpdsEntry.entryId " +
            " FROM OpdsEntry LEFT JOIN OpdsEntryStatusCache ON OpdsEntry.entryId = OpdsEntryStatusCache.statusEntryId " +
            " WHERE OpdsEntry.entryId IN (:entryIds) AND OpdsEntryStatusCache.statusEntryId IS NULL")
    public abstract List<String> findEntryIdsNotPresent(List<String> entryIds);

    @Override
    @Query("UPDATE OpdsEntryStatusCache " +
            "SET " +
            "totalSize = totalSize + :deltaTotalSize, " +
            "entriesWithContainer = entriesWithContainer + :deltaNumEntriesWithContainer " +
            "WHERE statusCacheUid IN \n" +
            " (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    public abstract void handleOpdsEntryLoadedUpdate(String entryId, int deltaTotalSize, int deltaNumEntriesWithContainer);

    @Override
    @Transaction
    public void handleDownloadJobQueued(int downloadJobId) {
        super.handleDownloadJobQueued(downloadJobId);
    }

    @Query("Update OpdsEntryStatusCache \n" +
            "SET \n" +
            "\ttotalSize = totalSize + (\n" +
            "\t\tSELECT \n" +
            "\t\t\t(DownloadJobItem.downloadLength - OpdsEntryStatusCache.acquisitionLinkLength) AS deltaTotalSize \n" +
            "\t\tFROM\n" +
            "\t\t\tDownloadJobItem LEFT JOIN OpdsEntryStatusCache ON DownloadJobItem.entryId = OpdsEntryStatusCache.statusEntryId\n" +
            "\t\tWHERE \n" +
            "\t\t\tDownloadJobItem.id = :downloadJobId\n" +
            "\t)," +
            "containersDownloadPending = containersDownloadPending + :deltaContainersDownloadPending\n" +
            "WHERE statusCacheUid IN\n" +
            "\t (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = (SELECT entryId FROM DownloadJobItem WHERE id = :downloadJobId)))\n" +
            "AND (SELECT acquisitionStatus FROM OpdsEntryStatusCache WHERE OpdsEntryStatusCache.statusEntryId = (SELECT entryId FROM DownloadJobItem WHERE id = :downloadJobId)) = 0")
    @Override
    public abstract void updateOnDownloadJobItemQueued(int downloadJobId, int deltaContainersDownloadPending);

    @Query("UPDATE OpdsEntryStatusCache \n" +
            "SET \n" +
            "acquisitionStatus = :acquisitionStatus \n" +
            "WHERE OpdsEntryStatusCache.statusCacheUid = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = (SELECT entryId FROM DownloadJobItem WHERE id = :downloadJobId))")
    @Override
    protected abstract void updateAcquisitionStatus(int downloadJobId, int acquisitionStatus);



    @Query("UPDATE OpdsEntryStatusCache\n" +
            "SET\n" +
            "sumActiveDownloadsBytesSoFar = sumActiveDownloadsBytesSoFar + :deltaDownloadedBytesSoFar\n" +
            "WHERE statusCacheUid IN \n" +
            "(SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    @Override
    public abstract void updateSumActiveBytesDownloadedSoFarByEntryId(String entryId, int deltaDownloadedBytesSoFar);

    @Query("UPDATE OpdsEntryStatusCache\n" +
            "SET\n" +
            "sumActiveDownloadsBytesSoFar = sumActiveDownloadsBytesSoFar + :deltaActiveDownloadsBytesSoFar,\n" +
            "containersDownloadPending = containersDownloadPending + :deltaContainersDownloadPending,\n" +
            "sumContainersDownloadedSize = sumContainersDownloadedSize + :deltaContainersDownloadedSize,\n" +
            "containersDownloaded = containersDownloaded + :deltaContainersDownloaded,\n" +
            "totalSize = totalSize + :deltaTotalSize\n" +
            "WHERE statusCacheUid IN\n" +
            "(SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    @Override
    public abstract void updateOnContainerAcquired(String entryId, long deltaActiveDownloadsBytesSoFar, int deltaContainersDownloadPending, long deltaContainersDownloadedSize, long deltaContainersDownloaded, long deltaTotalSize);
}
