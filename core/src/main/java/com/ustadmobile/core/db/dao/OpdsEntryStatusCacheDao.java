package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.DbManager;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.OpdsEntryAncestor;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * DAO for managing OpdsEntryStatusCache entities.
 *
 * @see OpdsEntryStatusCache
 */
public abstract class OpdsEntryStatusCacheDao {

    @UmInsert
    public abstract void insert(OpdsEntryStatusCache status);

    public abstract void insertList(List<OpdsEntryStatusCache> statuses);

    /**
     * Find the numeric primary key for the given entryId
     *
     * @param entryId entryId to find the UID for
     *
     * @return The UID of the given entry, or null if it's not present
     */
    @UmQuery("SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract Integer findUidByEntryId(String entryId);

    /**
     * Get the OpdsEntryStatusCache object for the given entryId
     *
     * @param entryId the entryId to search by
     *
     * @return OpdsEntryStatusCache if the given entry is present, null otherwise
     */
    @UmQuery("SELECT * From OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract OpdsEntryStatusCache findByEntryId(String entryId);

    /**
     * When from OpdsRepository for loading HTTP feeds over OPDS or using the main sync arch. Update
     * the given loadedEntry, and all ancestors. This routine will determine the ancestors for the
     * each loaded entry.
     *
     * @param dbManager DbManager object
     * @param loadedEntries - these MUST have been already committed to the database
     * @param oldEntries if this entry has already been loaded, this should be it's previous state.
     *                   This is used to calculate the difference (e.g. size change).
     */
    public void handleOpdsEntriesLoaded(DbManager dbManager, List<OpdsEntryWithRelations> loadedEntries,
                                        List<OpdsEntryWithRelations> oldEntries) {
        OpdsEntryWithRelations loadedEntry;
        OpdsEntryWithRelations oldEntry;

        //determine which entries do not have a corresponding EntryStatusCache yet
        HashMap<String, OpdsEntryWithRelations> entryIdToEntryMap = new HashMap<>();
        List<OpdsEntryStatusCache> newStatusCaches = new ArrayList<>();
        List<String> newStatusCachesEntryIds = new ArrayList<>();
        for(int i = 0; i < loadedEntries.size(); i++) {
            loadedEntry = loadedEntries.get(i);
            entryIdToEntryMap.put(loadedEntry.getEntryId(), loadedEntry);
            if(oldEntries == null || oldEntries.get(i) == null) {
                newStatusCachesEntryIds.add(loadedEntry.getEntryId());
            }
        }

        newStatusCachesEntryIds = findEntryIdsNotPresent(newStatusCachesEntryIds);
        for(String entryId : newStatusCachesEntryIds){
            OpdsEntryWithRelations newStatusCacheEntry = entryIdToEntryMap.get(entryId);
            OpdsLink link = newStatusCacheEntry.getAcquisitionLink(null, true);
            newStatusCaches.add(new OpdsEntryStatusCache(entryId, link != null ? link.getLength() : 0));
        }

        if(!newStatusCaches.isEmpty()) {
            //insert new status cache objects
            insertList(newStatusCaches);

            List<OpdsEntryAncestor> ancestorsList = dbManager.getOpdsEntryWithRelationsDao()
                    .getAncestors(newStatusCachesEntryIds);
            HashMap<String, Integer> entryIdToUidMap = new HashMap<>();
            List<OpdsEntryStatusCacheAncestor> cacheAncestors = new ArrayList<>();

            Integer opdsEntryStatusCacheId;
            Integer ancestorOpdsEntryStatusCacheId;
            for(OpdsEntryAncestor ancestor : ancestorsList) {
                opdsEntryStatusCacheId = entryIdToUidMap.get(ancestor.getDescendantId());
                if(opdsEntryStatusCacheId == null) {
                    opdsEntryStatusCacheId = findUidByEntryId(ancestor.getDescendantId());
                    entryIdToUidMap.put(ancestor.getDescendantId(), opdsEntryStatusCacheId);
                }

                ancestorOpdsEntryStatusCacheId = entryIdToUidMap.get(ancestor.getEntryId());
                if(ancestorOpdsEntryStatusCacheId == null) {
                    ancestorOpdsEntryStatusCacheId = findUidByEntryId(ancestor.getEntryId());
                    entryIdToUidMap.put(ancestor.getEntryId(), ancestorOpdsEntryStatusCacheId);
                }

                cacheAncestors.add(new OpdsEntryStatusCacheAncestor(opdsEntryStatusCacheId,
                        ancestorOpdsEntryStatusCacheId));
            }

            dbManager.getOpdsEntryStatusCacheAncestorDao().insertAll(cacheAncestors);
        }


        for(int i = 0; i < loadedEntries.size(); i++) {
            loadedEntry = loadedEntries.get(i);
            oldEntry = oldEntries != null ? oldEntries.get(i) : null;
            OpdsLink loadedLink = loadedEntry.getAcquisitionLink(null, true);
            OpdsLink oldEntryLink = (oldEntry != null) ? oldEntry.getAcquisitionLink(null, false) : null;

            int deltaTotalSize = 0;
            int deltaNumEntriesWithContainer = 0;
            if(loadedLink != null){
                deltaTotalSize += loadedLink.getLength();
                deltaNumEntriesWithContainer++;
            }

            if(oldEntryLink != null) {
                deltaTotalSize -= oldEntryLink.getLength();
                deltaNumEntriesWithContainer--;
            }

            if(deltaTotalSize == 0 && deltaNumEntriesWithContainer == 0) {
                //nothing changed on this entry
                continue;
            }

            //now update this entry and all ancestors
            handleOpdsEntryLoadedUpdate(loadedEntry.getEntryId(), deltaTotalSize,
                    deltaNumEntriesWithContainer);
        }
    }

    /**
     * Returns a list of entryIds that do not yet have any corresponding OpdsEntryStatusCache object.
     *
     * @param entryIds A list of all entryIds to search for. These MUST have at least one corresponding entry in the OpdsEntry table
     *
     * @return A list of all those entryIds which have an entry in the OpdsEntry table but have no corresponding OpdsEntryStatusCache entity
     */
    @UmQuery("SELECT OpdsEntry.entryId " +
            " FROM OpdsEntry LEFT JOIN OpdsEntryStatusCache ON OpdsEntry.entryId = OpdsEntryStatusCache.statusEntryId " +
            " WHERE OpdsEntry.entryId IN (:entryIds) AND OpdsEntryStatusCache.statusEntryId IS NULL")
    public abstract List<String> findEntryIdsNotPresent(List<String> entryIds);

    /**
     * Update the given EntryId with information discovered when the OpdsEntry is loaded for the first time.
     * This updates the entry and all its known ancestors.
     *
     * @param entryId OPDS entry ID
     * @param deltaTotalSize the difference between the previous total size and the new total size
     * @param deltaEntriesWithContainer the difference between the number of previously known containers, and now
     */
    @UmQuery("UPDATE OpdsEntryStatusCache \n" +
            "SET" +
            "totalSize = totalSize + :deltaTotalSize " +
            "entriesWithContainer = entriesWithContainer + :deltaEntriesWithContainer" +
            "WHERE statusCacheUid IN \n" +
            " (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    protected abstract void handleOpdsEntryLoadedUpdate(String entryId, int deltaTotalSize, int deltaEntriesWithContainer);


    /**
     * Run when a download job is queued, or when using the crawl manager, if the job is already queued.
     *
     * @param downloadJobId
     */
    public void handleDownloadJobQueued(int downloadJobId) {
        updateOnDownloadJobItemQueued(downloadJobId, 1);
        updateAcquisitionStatus(downloadJobId, OpdsEntryStatusCache.ACQUISITION_STATUS_ACQUIRED);
    }

    /**
     * Update the OpdsEntryStatusCache as required when a download job item is queued. This can change
     * the total size (as the Download Job's download size will supercede the OPDS entry length). This
     * updates the entry and all it's known ancestors.
     *
     * @param downloadJobId The ID of the DownloadJobItem object that has been queued.
     * @param deltaContainersDownloadPending The change in the number of containers that are now pending download (generally 1)
     */
    @UmQuery("Update OpdsEntryStatusCache \n" +
            "SET \n" +
            "\ttotalSize = totalSize + (\n" +
            "\t\tSELECT \n" +
            "\t\t\t(DownloadJobItem.downloadLength - OpdsEntryStatusCache.acquisitionLinkLength) AS deltaTotalSize \n" +
            "\t\tFROM\n" +
            "\t\t\tDownloadJobItem LEFT JOIN OpdsEntryStatusCache ON DownloadJobItem.entryId = OpdsEntryStatusCache.statusEntryId\n" +
            "\t\tWHERE \n" +
            "\t\t\tDownloadJobItem.id = :downloadJobId\n" +
            "\t)\n" +
            "WHERE statusCacheUid IN\n" +
            "\t (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = (SELECT entryId FROM DownloadJobItem WHERE id = :downloadJobId)))\n" +
            "AND (SELECT acquisitionStatus FROM OpdsEntryStatusCache WHERE OpdsEntryStatusCache.statusEntryId = (SELECT entryId FROM DownloadJobItem WHERE id = :downloadJobId)) = 0")
    protected abstract void updateOnDownloadJobItemQueued(int downloadJobId, int deltaContainersDownloadPending);

    /**
     * Update the acquisitionStatus of the given OpdsEntryStatusCache object for the given ID of a
     * DownloadJobItem
     *
     * @param downloadJobId The ID of the DownloadJobItem that has been queued
     * @param acquisitionStatus the acquisition status to set
     */
    @UmQuery("UPDATE OpdsEntryStatusCache \n" +
            "SET \n" +
            "acquisitionStatus = :acquisitionStatus \n" +
            "WHERE OpdsEntryStatusCache.statusCacheUid = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = (SELECT entryId FROM DownloadJobItem WHERE id = :downloadJobId))")
    protected abstract void updateAcquisitionStatus(int downloadJobId, int acquisitionStatus);


    /**
     * Update the number of bytes downloaded so far for a given entry and all its known ancestors.
     * Generally executed when a download job item has marked current progress.
     *
     * @param entryId The entry ID to update
     * @param deltaDownloadedBytesSoFar The change in the number of bytes downloaded so far for the
     *                                  given entry.
     */
    @UmQuery("UPDATE OpdsEntryStatusCache\n" +
            "SET\n" +
            "sumActiveDownloadsBytesSoFar = sumActiveDownloadsBytesSoFar + :deltaDownloadedBytesSoFar\n" +
            "WHERE statusCacheUid IN \n" +
            "(SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    public abstract void updateSumActiveBytesDownloadedSoFarByEntryId(String entryId, int deltaDownloadedBytesSoFar);


    /**
     * Update the entry and all it's ancestors when the entry is acquired.
     *
     * @param entryId The entry ID to update
     * @param deltaDownloadedBytesSoFar The change in the number of bytes downloaded so far by active
     *                                  downloads. Generally negative by the number of bytes that have
     *                                  been downloaded and reported as updates by the the download that
     *                                  just completed.
     * @param deltaContainersDownloadPending The change in the number of containers for which a download
     *                                       is pending. Generally -1.
     * @param deltaContainersDownloadedSize The change in the size of entries that have been downloaded,
     *                                      generally the file size of the container just downloaded.
     * @param deltaContainersDownloaded The change in the number of containers downloaded, generally +1.
     * @param deltaTotalSize The change (if any) in the total size of known entries. Would normally
     *                       be the difference between the expected and actual download size.
     */
    public abstract void updateOnContainerAcquired(String entryId, long deltaDownloadedBytesSoFar,
                                                       int deltaContainersDownloadPending,
                                                        long deltaContainersDownloadedSize,
                                                        long deltaContainersDownloaded,
                                                        long deltaTotalSize);

}
