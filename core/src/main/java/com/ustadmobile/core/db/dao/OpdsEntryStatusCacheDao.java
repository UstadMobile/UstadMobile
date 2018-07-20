package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ContainerFile;
import com.ustadmobile.lib.db.entities.OpdsEntryRelative;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCache;
import com.ustadmobile.lib.db.entities.OpdsEntryStatusCacheAncestor;
import com.ustadmobile.lib.db.entities.OpdsEntryWithRelations;
import com.ustadmobile.lib.db.entities.OpdsLink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
     * Find the numeric primary key for the given entryId
     *
     * @param entryId entryId to find the UID for
     *
     * @return The UID of the given entry, or null if it's not present
     */
    @UmQuery("SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract Integer findUidByEntryId(String entryId);


    /**
     * Get a live data result for the given entryId
     *
     * @param entryId Entry ID to lookup the status of
     *
     * @return LiveData object with the OpdsEntryStatusCache for the given object
     */
    @UmQuery("SELECT * From OpdsEntryStatusCache WHERE statusEntryId = :entryId")
    public abstract UmLiveData<OpdsEntryStatusCache> findByEntryIdLive(String entryId);

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
     * Get the OpdsEntryStatusCache object by the primary key
     *
     * @param statusCacheUid
     *
     * @return
     */
    @UmQuery("SELECT * FROM OpdsEntryStatusCache WHERE statusCacheUid = :statusCacheUid")
    public abstract OpdsEntryStatusCache findByStatusCacheUid(int statusCacheUid);


    /**
     * Lookup the OpdsEntryStatusCache object by a given DownloadJobItemId
     * @param downloadJobItemId id of the DownloadJobItem
     *
     * @return OpdsEntryStatusCache associated with the given download job item, or null if it doesn't exist
     */
    @UmQuery("SELECT OpdsEntryStatusCache.* FROM OpdsEntryStatusCache " +
            "            LEFT JOIN DownloadSetItem ON DownloadSetItem.entryId = OpdsEntryStatusCache.statusEntryId " +
            "            LEFT JOIN DownloadJobItem ON DownloadSetItem.id = DownloadJobItem.downloadSetItemId " +
            "            WHERE DownloadJobItem.downloadJobItemId = :downloadJobItemId")
    public abstract OpdsEntryStatusCache findByDownloadJobItemId(int downloadJobItemId);

    @UmQuery("SELECT OpdsEntryStatusCache.statusEntryId " +
            "FROM " +
            "OpdsEntryStatusCache " +
            "JOIN OpdsEntryStatusCacheAncestor ON OpdsEntryStatusCacheAncestor.opdsEntryStatusCacheId = OpdsEntryStatusCache.statusCacheUid " +
            "WHERE OpdsEntryStatusCacheAncestor.ancestorOpdsEntryStatusCacheId = " +
            "(SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :rootEntryId)")
    public abstract List<String> findAllKnownDescendantEntryIds(String rootEntryId);

    /**
     * When from OpdsRepository for loading HTTP feeds over OPDS or using the main sync arch. Update
     * the given loadedEntry, and all ancestors. This routine will determine the ancestors for the
     * each loaded entry.
     *
     * TODO: We must first find any known descendants. Then set it's sizeIncDescendants accordingly
     * (count direct children). Then we find ancestors and increment the size according to what we found.
     *
     * @param dbManager DbManager object
     * @param loadedEntries The entries that have just been loaded. These MUST have been already
     *                      committed to the OpdsEntry database.
     */
    @UmTransaction
    public void handleOpdsEntriesLoaded(UmAppDatabase dbManager, List<OpdsEntryWithRelations> loadedEntries) {
        OpdsEntryWithRelations loadedEntry;

        //determine which entries do not have a corresponding EntryStatusCache yet
        HashMap<String, OpdsEntryWithRelations> entryIdToEntryMap = new HashMap<>();
        List<OpdsEntryStatusCache> newStatusCaches = new ArrayList<>();
        List<String> loadedEntryIds = new ArrayList<>();
        for(int i = 0; i < loadedEntries.size(); i++) {
            loadedEntry = loadedEntries.get(i);
            entryIdToEntryMap.put(loadedEntry.getEntryId(), loadedEntry);
            loadedEntryIds.add(loadedEntry.getEntryId());
        }

        List<String> entryIdsToAdd = findEntryIdsNotPresent(loadedEntryIds);
        List<OpdsEntryStatusCache> oldEntryStatusCaches = findByEntryIdList(loadedEntryIds);
        HashMap<String, OpdsEntryStatusCache>  entryStatusCacheMap = new HashMap<>();

        for(OpdsEntryStatusCache oldEntryStatusCache : oldEntryStatusCaches){
            entryStatusCacheMap.put(oldEntryStatusCache.getStatusEntryId(), oldEntryStatusCache);
        }

        OpdsEntryStatusCache newEntryStatusCache;
        for(String entryId : entryIdsToAdd){
            newEntryStatusCache = new OpdsEntryStatusCache(entryId, 0);
            newStatusCaches.add(newEntryStatusCache);
            entryStatusCacheMap.put(entryId, newEntryStatusCache);
        }

        /*
         * Insert any entries that were not present yet, and for each one insert it and a
         * relationship entity to link it to all it's ancestors
         */
        if(!newStatusCaches.isEmpty()) {
            //insert new status cache objects
            insertList(newStatusCaches);

            List<OpdsEntryStatusCacheAncestor> cacheAncestors = new ArrayList<>();

            Integer opdsEntryStatusCacheId;
            Integer relativeOpdsEntryStatusCacheId;
            HashMap<String, Integer> entryIdToUidMap = new HashMap<>();


            List<OpdsEntryRelative> descendantsList = dbManager.getOpdsEntryWithRelationsDao()
                    .getDescendant_RecursiveQuery(entryIdsToAdd);
            HashMap<String, OpdsEntryStatusCache> childrenEntryStatusCacheMap = null;
            
            if(!descendantsList.isEmpty()) {
                childrenEntryStatusCacheMap = new HashMap<>();
                List<String> childrenEntryIdList = new ArrayList<>();
                for(OpdsEntryRelative descendant : descendantsList){
                    if(descendant.getDistance() == 1)
                        childrenEntryIdList.add(descendant.getRelativeEntryId());
                }

                List<OpdsEntryStatusCache> childrenEntryStatusCacheList = findByEntryIdList(childrenEntryIdList);
                for(OpdsEntryStatusCache childEntryStatusCache : childrenEntryStatusCacheList) {
                    childrenEntryStatusCacheMap.put(childEntryStatusCache.getStatusEntryId(),
                            childEntryStatusCache);
                }
            }
            
            for(OpdsEntryRelative descendant : descendantsList) {
                opdsEntryStatusCacheId = entryIdToUidMap.get(descendant.getEntryId());
                if(opdsEntryStatusCacheId == null) {
                    opdsEntryStatusCacheId = findUidByEntryId(descendant.getEntryId());
                    entryIdToUidMap.put(descendant.getEntryId(), opdsEntryStatusCacheId);
                }

                relativeOpdsEntryStatusCacheId = entryIdToUidMap.get(descendant.getRelativeEntryId());
                if(relativeOpdsEntryStatusCacheId == null){
                    relativeOpdsEntryStatusCacheId = findUidByEntryId(descendant.getRelativeEntryId());
                    entryIdToUidMap.put(descendant.getRelativeEntryId(), relativeOpdsEntryStatusCacheId);
                }

                if(descendant.getDistance() == 1) {
//                    OpdsEntryStatusCache entryStatusCache = entryStatusCacheMap.get(descendant.getEntryId());
                    OpdsEntryStatusCache entryStatusCache = findByEntryId(descendant.getEntryId());
                    OpdsEntryStatusCache childEntryStatusCache = childrenEntryStatusCacheMap.get(descendant.getRelativeEntryId());

                    entryStatusCache.setSizeIncDescendants(
                            entryStatusCache.getSizeIncDescendants() +
                            childEntryStatusCache.getSizeIncDescendants());
                    entryStatusCache.setEntriesWithContainerIncDescendants(
                            entryStatusCache.getEntriesWithContainerIncDescendants() +
                            childEntryStatusCache.getEntriesWithContainerIncDescendants());
                    entryStatusCache.setContainersDownloadedIncDescendants(
                            entryStatusCache.getContainersDownloadedIncDescendants()+
                            childEntryStatusCache.getContainersDownloadedIncDescendants());
                    entryStatusCache.setContainersDownloadPendingIncAncestors(
                            entryStatusCache.getContainersDownloadPendingIncAncestors() +
                            childEntryStatusCache.getContainersDownloadPendingIncAncestors());
                    entryStatusCache.setPendingDownloadBytesSoFarIncDescendants(
                            entryStatusCache.getPendingDownloadBytesSoFarIncDescendants() +
                            childEntryStatusCache.getPendingDownloadBytesSoFarIncDescendants());
                    entryStatusCache.setContainersDownloadedSizeIncDescendants(
                            entryStatusCache.getContainersDownloadedSizeIncDescendants() +
                            childEntryStatusCache.getContainersDownloadedSizeIncDescendants());
                    entryStatusCache.setActiveDownloadsIncAncestors(
                            entryStatusCache.getActiveDownloadsIncAncestors() +
                                    childEntryStatusCache.getActiveDownloadsIncAncestors());

                    dbManager.getOpdsEntryStatusCacheDao().update(entryStatusCache);
                }

                cacheAncestors.add(new OpdsEntryStatusCacheAncestor(relativeOpdsEntryStatusCacheId,
                        opdsEntryStatusCacheId));
            }



            List<OpdsEntryRelative> ancestorsList = dbManager.getOpdsEntryWithRelationsDao()
                    .getAncestors(entryIdsToAdd);

            for(OpdsEntryRelative ancestor : ancestorsList) {
                opdsEntryStatusCacheId = entryIdToUidMap.get(ancestor.getRelativeEntryId());
                if(opdsEntryStatusCacheId == null) {
                    opdsEntryStatusCacheId = findUidByEntryId(ancestor.getRelativeEntryId());
                    entryIdToUidMap.put(ancestor.getRelativeEntryId(), opdsEntryStatusCacheId);
                }

                relativeOpdsEntryStatusCacheId = entryIdToUidMap.get(ancestor.getEntryId());
                if(relativeOpdsEntryStatusCacheId == null) {
                    relativeOpdsEntryStatusCacheId = findUidByEntryId(ancestor.getEntryId());
                    entryIdToUidMap.put(ancestor.getEntryId(), relativeOpdsEntryStatusCacheId);
                }

                if(opdsEntryStatusCacheId != null && relativeOpdsEntryStatusCacheId != null) {
                    //Some entries (e.g. the master library list) might not have statuscacheid objects, so avoid handling them
                    cacheAncestors.add(new OpdsEntryStatusCacheAncestor(opdsEntryStatusCacheId,
                            relativeOpdsEntryStatusCacheId));
                }
            }

            dbManager.getOpdsEntryStatusCacheAncestorDao().insertAll(cacheAncestors);
        }

//        List<OpdsEntryStatusCacheAncestor> ancestorsToAdd = dbManager.getOpdsEntryStatusCacheAncestorDao()
//                .findAncestorsToAdd(loadedEntryIds);
//        dbManager.getOpdsEntryStatusCacheAncestorDao().insertAll(ancestorsToAdd);




        OpdsEntryStatusCache currentStatusCache;
        for(int i = 0; i < loadedEntries.size(); i++) {
            loadedEntry = loadedEntries.get(i);
            currentStatusCache = entryStatusCacheMap.get(loadedEntry.getEntryId());

            OpdsLink loadedLink = loadedEntry.getAcquisitionLink(null, true);

            int deltaTotalSize = 0;
            int deltaNumEntriesWithContainer = 0;
            long linkLength = loadedLink != null ? loadedLink.getLength() : 0;

            boolean isDownloadedOrDownloadPending = (currentStatusCache != null &&
                    (currentStatusCache.isEntryContainerDownloaded() || currentStatusCache.isEntryContainerDownloadPending()));

            if(loadedLink != null && !isDownloadedOrDownloadPending){
                deltaTotalSize += linkLength;
                deltaNumEntriesWithContainer++;
            }

            if(currentStatusCache != null && !isDownloadedOrDownloadPending) {
                deltaTotalSize -= currentStatusCache.getEntrySize();
                if(currentStatusCache.isEntryHasContainer())
                    deltaNumEntriesWithContainer--;
            }

            if(deltaTotalSize == 0 && deltaNumEntriesWithContainer == 0) {
                //nothing changed on this entry
                continue;
            }

            //now update this entry and all ancestors
            handleOpdsEntryLoadedUpdateIncAncestors(loadedEntry.getEntryId(), deltaTotalSize,
                    deltaNumEntriesWithContainer);
            handleOpdsEntryLoadedUpdateEntry(loadedEntry.getEntryId(), linkLength,
                    loadedLink != null);
        }
    }

    /**
     * Returns a list of entryIds that do not yet have any corresponding OpdsEntryStatusCache object.
     *
     * @param entryIds A list of all entryIds to search for. These MUST have at least one corresponding entry in the OpdsEntry table
     *
     * @return A list of all those entryIds which have an entry in the OpdsEntry table but have no corresponding OpdsEntryStatusCache entity
     */
    @UmQuery("SELECT DISTINCT OpdsEntry.entryId " +
            " FROM OpdsEntry LEFT JOIN OpdsEntryStatusCache ON OpdsEntry.entryId = OpdsEntryStatusCache.statusEntryId " +
            " WHERE OpdsEntry.entryId IN (:entryIds) AND OpdsEntryStatusCache.statusEntryId IS NULL")
    protected abstract List<String> findEntryIdsNotPresent(List<String> entryIds);

    /**
     *
     * @param entryIds
     * @return
     */
    @UmQuery("SELECT * FROM OpdsEntryStatusCache WHERE statusEntryId IN (:entryIds)")
    protected abstract List<OpdsEntryStatusCache> findByEntryIdList(List<String> entryIds);

    /**
     * Update the given EntryId with information discovered when the OpdsEntry is loaded for the first time.
     * This updates the entry and all its known ancestors.
     *
     * @param entryId OPDS entry ID
     * @param deltaSizeIncDescendants the difference between the previous total size and the new total size
     * @param deltaEntriesWithContainerIncDescendants the difference between the number of previously known containers, and now
     */
    @UmQuery("UPDATE OpdsEntryStatusCache " +
            "SET " +
            "sizeIncDescendants = sizeIncDescendants  + :deltaSizeIncDescendants, " +
            "entriesWithContainerIncDescendants = entriesWithContainerIncDescendants + :deltaEntriesWithContainerIncDescendants " +
            "WHERE statusCacheUid IN \n" +
            " (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    protected abstract void handleOpdsEntryLoadedUpdateIncAncestors(String entryId,
                                                                    int deltaSizeIncDescendants,
                                                                    int deltaEntriesWithContainerIncDescendants);

    @UmQuery("UPDATE OpdsEntryStatusCache " +
            "SET " +
            "entrySize = :size, " +
            "entryHasContainer = :hasContainer " +
            "WHERE statusEntryId = :entryId")
    protected abstract void handleOpdsEntryLoadedUpdateEntry(String entryId, long size, boolean hasContainer);


    /**
     * Run when a download job is queued, or when using the crawl manager, if the job is already queued.
     *
     * @param downloadJobItemId The id of the DownloadJobItem for the download that has been started
     */
    @UmTransaction
    public void handleDownloadJobQueued(int downloadJobItemId) {
        OpdsEntryStatusCache statusCache = findByDownloadJobItemId(downloadJobItemId);
        if(statusCache.isEntryPausedDownload() || !(statusCache.isEntryContainerDownloaded() || statusCache.isEntryContainerDownloadPending())) {
            int statusCacheUid = statusCache.getStatusCacheUid();
            int deltaContainerDownloadPending = statusCache.isEntryContainerDownloadPending() ? 0 : 1;
            int deltaPaused = statusCache.isEntryPausedDownload() ? -1 : 0;
            updateOnDownloadJobItemQueuedIncAncestors(statusCacheUid, downloadJobItemId,
                    deltaContainerDownloadPending,
                    deltaPaused);
            updateOnDownloadJobItemQueuedEntry(statusCache.getStatusCacheUid(), downloadJobItemId);
        }
    }



    /**
     * Update the OpdsEntryStatusCache as required when a download job item is queued. This can change
     * the total size (as the Download Job's download size will supercede the OPDS entry length). This
     * updates the entry and all it's known ancestors.
     *
     * @param downloadJobId The ID of the DownloadJobItem object that has been queued.
     * @param deltaContainersDownloadPending The change in the number of containers that are now pending download (generally 1)
     */
    @UmQuery("Update OpdsEntryStatusCache " +
            "SET " +
            "sizeIncDescendants = sizeIncDescendants + (" +
            "SELECT " +
            "(DownloadJobItem.downloadLength - OpdsEntryStatusCache.entrySize) AS deltaTotalSize " +
            "FROM " +
            "DownloadJobItem LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            "LEFT JOIN OpdsEntryStatusCache ON DownloadSetItem.entryId = OpdsEntryStatusCache.statusEntryId " +
            "WHERE " +
            "DownloadJobItem.downloadJobItemId = :downloadJobId " +
            "), " +
            "containersDownloadPendingIncAncestors = containersDownloadPendingIncAncestors + :deltaContainersDownloadPending , " +
            "pausedDownloadsIncAncestors = pausedDownloadsIncAncestors + :deltaPausedDownloads " +
            "WHERE statusCacheUid IN " +
            "  (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = :statusCacheUid)")
    protected abstract void updateOnDownloadJobItemQueuedIncAncestors(int statusCacheUid, int downloadJobId,
                                                                      int deltaContainersDownloadPending, int deltaPausedDownloads);

    @UmQuery("Update OpdsEntryStatusCache " +
            "SET " +
                "entrySize = (SELECT downloadLength FROM DownloadJobItem WHERE downloadJobItemId= :downloadJobId), " +
            "entryContainerDownloadPending = 1," +
            "entryPausedDownload = 0 " +
            " WHERE statusCacheUid = :statusCacheUid")
    protected abstract void updateOnDownloadJobItemQueuedEntry(int statusCacheUid, int downloadJobId);

    public void handleDownloadJobStarted(int statusCacheUid) {
        OpdsEntryStatusCache statusCache = findByStatusCacheUid(statusCacheUid);
        if(!statusCache.isEntryActiveDownload()) {
            updateOnDownloadJobStartedIncAncestors(statusCacheUid, 1);
            updateOnDownloadJobStartedEntry(statusCacheUid, true);
        }
    }

    @UmQuery("UPDATE OpdsEntryStatusCache " +
            "SET activeDownloadsIncAncestors = activeDownloadsIncAncestors + :deltaActiveDownloads " +
            "WHERE statusCacheUid IN (SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = :statusCacheUid)")
    protected abstract void updateOnDownloadJobStartedIncAncestors(int statusCacheUid, int deltaActiveDownloads);

    @UmQuery("UPDATE OpdsEntryStatusCache SET entryActiveDownload = :activeDownload " +
            "WHERE statusCacheUid = :statusCacheUid")
    protected abstract void updateOnDownloadJobStartedEntry(int statusCacheUid, boolean activeDownload);


    /**
     * This method needs to be called by the DownloadTask as a download progresses. It will update the
     * bytesSoFar for the given entry and all it's ancestors.
     *
     * @param entryStatusCacheUid The entryStatusCacheUid for the entry that is being updated.
     * @param downloadJobItemId The id of the DownloadJobItem that is downloading this entry. This
     *                          MUST have been updated in the database first, the Update statement
     *                          will use the DownloadJobItem table itself to lookup the current
     *                          progress of a download job item.
     *
     *                          //TODO: add transaction annotation
     */
    @UmTransaction
    public void handleDownloadJobProgress(int entryStatusCacheUid, int downloadJobItemId){
        updateActiveBytesDownloadedSoFarIncAncestors(entryStatusCacheUid, downloadJobItemId);
        updateActiveBytesDownloadedSoFarEntry(entryStatusCacheUid, downloadJobItemId);
    }


    @UmQuery("Update OpdsEntryStatusCache " +
            "SET " +
            "pendingDownloadBytesSoFarIncDescendants= pendingDownloadBytesSoFarIncDescendants + (" +
            "(SELECT downloadedSoFar FROM DownloadJobItem WHERE downloadJobItemId = :downloadJobItemId) - " +
            "(SELECT entryPendingDownloadBytesSoFar FROM OpdsEntryStatusCache WHERE statusCacheUid = :entryStatusCacheId))" +
            "WHERE statusCacheUid IN " +
            "(SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = :entryStatusCacheId)")
    protected abstract void updateActiveBytesDownloadedSoFarIncAncestors(int entryStatusCacheId, int downloadJobItemId);

    @UmQuery("UPDATE OpdsEntryStatusCache " +
            "SET entryPendingDownloadBytesSoFar = (SELECT downloadedSoFar FROM DownloadJobItem WHERE downloadJobItemId = :downloadJobItemId) " +
            "WHERE statusCacheUid = :entryStatusCacheId")
    protected abstract void updateActiveBytesDownloadedSoFarEntry(int entryStatusCacheId, int downloadJobItemId);


    /**
     * This method should be called when a container is found on the disk, where the entry may or
     * may not yet be known to the OpdsEntryStatusCache. If the entry is not known yet, it will be
     * created.
     *
     * @param dbManager DbManager object
     * @param entry Entry found within container (e.g. as returned by the scanner)
     * @param containerFile The ContainerFile that represents the file in which this entry was found
     */
    @UmTransaction
    public void handleContainerFoundOnDisk(UmAppDatabase dbManager, OpdsEntryWithRelations entry,
                                           ContainerFile containerFile) {
        OpdsEntryStatusCache statusCache = findByEntryId(entry.getEntryId());
        if(statusCache == null) {
            handleOpdsEntriesLoaded(dbManager, Arrays.asList(entry));
            statusCache = findByEntryId(entry.getEntryId());
        }

        handleContainerDownloadedOrDiscovered(statusCache, containerFile);
    }

    /**
     * This method needs to be called when a container has been downloaded, or discovered on disk.
     *
     * TODO: check that this download job item is in fact complete (e.g. we are not discovering something which is being updated)
     *
     * @param entryStatusCache The OpdsEntryStatusCache representing the given id.
     * @param containerFile The ContainerFile that contains the given entry.
     */
    @UmTransaction
    public void handleContainerDownloadedOrDiscovered(OpdsEntryStatusCache entryStatusCache, ContainerFile containerFile){
        long deltaPendingDownloadBytesSoFar = entryStatusCache.getEntryPendingDownloadBytesSoFar() * -1;
        int deltaContainersDownloadPending = entryStatusCache.isEntryContainerDownloadPending() ? -1 : 0;
        long deltaContainersDownloadedSize = containerFile.getFileSize() - entryStatusCache.getEntryContainerDownloadedSize();
        int deltaContainersDownloaded = entryStatusCache.isEntryContainerDownloaded() ? 0 : 1;
        long deltaSize = containerFile.getFileSize() - entryStatusCache.getEntrySize();
        int deltaActiveDownloads = entryStatusCache.isEntryActiveDownload() ? -1 : 0;
        int deltaPausedDownloads = entryStatusCache.isEntryPausedDownload() ? -1 : 0;

        updateOnContainerStatusChangedIncAncestors(entryStatusCache.getStatusEntryId(),
                deltaPendingDownloadBytesSoFar, deltaContainersDownloadPending,
                deltaActiveDownloads, deltaPausedDownloads, deltaContainersDownloadedSize,
                deltaContainersDownloaded, deltaSize);

        updateOnContainerStatusChangedEntry(entryStatusCache.getStatusCacheUid(), 0,
                false, false, false,
                containerFile.getFileSize(), true,
                containerFile.getFileSize());
    }

    /**
     * Synonymous to handleContainerDownloadedOrDiscovered(findByStatusCacheUid(entryStatusUid)...
     *
     * @param entryStatusUid The UID of the OpdsEntryStatusCache that represents the entry that has
     *                       been downloaded or discovered.
     * @param containerFile The ContainerFile object that contains the given entry.
     */
    public void handleContainerDownloadedOrDiscovered(int entryStatusUid, ContainerFile containerFile) {
        handleContainerDownloadedOrDiscovered(findByStatusCacheUid(entryStatusUid), containerFile);
    }

    /**
     * Synonymous to handleContainerDownloadedOrDiscovered(findByEnrtyId(entryId))...
     *
     * @param entryId The Entry ID of the container that has been downloaded or discovered
     * @param containerFile The containerFile object that contains the given entry
     */
    public void handleContainerDownloadedOrDiscovered(String entryId, ContainerFile containerFile) {
        handleContainerDownloadedOrDiscovered(findByEntryId(entryId), containerFile);
    }



    /**
     * Update the entry and all it's ancestors when the entry is acquired, deleted, download aborted
     * etc.
     *
     * @param entryId The entry ID to update
     * @param deltaPendingDownloadBytesSoFar The change in the number of bytes downloaded so far by active
     *                                  downloads. Generally negative by the number of bytes that have
     *                                  been downloaded and reported as updates by the the download that
     *                                  just completed.
     * @param deltacontainersDownloadPending The change in the number of containers for which a download
     *                                       is pending. Generally -1.
     * @param deltaActiveDownloads The change in the number of containers for which a download is currently
     *                             active.
     * @param deltaContainersDownloadedSize The change in the size of entries that have been downloaded,
     *                                      generally the file size of the container just downloaded.
     * @param deltaContainersDownloaded The change in the number of containers downloaded, generally +1.
     * @param deltaSize The change (if any) in the total size of known entries. Would normally
     *                       be the difference between the expected and actual download size.
     */
    @UmQuery("UPDATE OpdsEntryStatusCache\n" +
            "SET\n" +
            "pendingDownloadBytesSoFarIncDescendants = pendingDownloadBytesSoFarIncDescendants + :deltaPendingDownloadBytesSoFar,\n" +
            "containersDownloadPendingIncAncestors = containersDownloadPendingIncAncestors + :deltacontainersDownloadPending,\n" +
            "containersDownloadedSizeIncDescendants = containersDownloadedSizeIncDescendants + :deltaContainersDownloadedSize,\n" +
            "containersDownloadedIncDescendants = containersDownloadedIncDescendants + :deltaContainersDownloaded,\n" +
            "sizeIncDescendants = sizeIncDescendants + :deltaSize, " +
            "activeDownloadsIncAncestors = activeDownloadsIncAncestors + :deltaActiveDownloads, " +
            "pausedDownloadsIncAncestors = pausedDownloadsIncAncestors + :deltaPausedDownloads " +
            "WHERE statusCacheUid IN " +
            "(SELECT ancestorOpdsEntryStatusCacheId FROM OpdsEntryStatusCacheAncestor WHERE opdsEntryStatusCacheId = (SELECT statusCacheUid FROM OpdsEntryStatusCache WHERE statusEntryId = :entryId))")
    protected abstract void updateOnContainerStatusChangedIncAncestors(String entryId, long deltaPendingDownloadBytesSoFar,
                                                                       int deltacontainersDownloadPending,
                                                                       int deltaActiveDownloads,
                                                                       int deltaPausedDownloads,
                                                                       long deltaContainersDownloadedSize,
                                                                       long deltaContainersDownloaded, long deltaSize);

    /**
     * Update the entry properties when the entry is acquired, deleted, download aborted etc.
     *
     * @param statusCacheUid The UID of the OpdsEntryStatusCache object.
     * @param pendingDownloadBytesSoFar Currently pending download bytes so far.
     * @param containerDownloadPending True if the container's download is pending,
     * @param activeDownload True if the download is currently active, false otherwise
     * @param containerDownloadedSize The currently downloaded container size (0 if not downloaded,
     *                                or the file size of the container if it is downloaded).
     * @param containerDownloaded True if the container for this entry has been downloaded, false otherwise.
     */
    @UmQuery("UPDATE OpdsEntryStatusCache " +
            "SET " +
            "entryPendingDownloadBytesSoFar = :pendingDownloadBytesSoFar, " +
            "entryContainerDownloadPending = :containerDownloadPending,  " +
            "entryActiveDownload = :activeDownload, " +
            "entryPausedDownload = :pausedDownload, " +
            "entryContainerDownloadedSize = :containerDownloadedSize, " +
            "entryContainerDownloaded = :containerDownloaded," +
            "entrySize = :entrySize " +
            "WHERE statusCacheUid = :statusCacheUid")
    protected abstract void updateOnContainerStatusChangedEntry(int statusCacheUid,
                                                                long pendingDownloadBytesSoFar,
                                                                boolean containerDownloadPending,
                                                                boolean activeDownload,
                                                                boolean pausedDownload,
                                                                long containerDownloadedSize,
                                                                boolean containerDownloaded,
                                                                long entrySize);


    @UmQuery("SELECT OpdsEntryStatusCache.* FROM OpdsEntryStatusCache " +
            "LEFT JOIN ContainerFileEntry ON OpdsEntryStatusCache.statusEntryId = ContainerFileEntry.containerEntryId " +
            "WHERE " +
            "OpdsEntryStatusCache.statusEntryId IN (:entryIdsToCheck) " +
            "AND " +
            "ContainerFileEntry.containerEntryId IS NULL " +
            "AND " +
            "OpdsEntryStatusCache.entryContainerDownloaded = 1")
    protected abstract List<OpdsEntryStatusCache> findDeletedEntriesToUpdate(List<String> entryIdsToCheck);

    /**
     * This method should be called when a container file is deleted. Given a list of known entries
     * that were previously in the container, it will update those which do not have any container
     * remaining. If an entry was downloaded twice and is present in more than one file, then it will
     * be ignored until the entry no longer exists in any known downloaded container file.
     *
     * @param entryIdsInContainer List of entry ids that were known to be in the deleted container.
     */
    public void handleContainerDeleted(List<String> entryIdsInContainer) {
        for(OpdsEntryStatusCache deletedEntry : findDeletedEntriesToUpdate(entryIdsInContainer)) {
            handleContainerDeleted(deletedEntry);
        }
    }

    /**
     * This method should be called when an already downloaded entry is known to be deleted, and
     * there are no further containers that have this entry.
     *
     * @param entryStatusCache
     */
    @UmTransaction
    public void handleContainerDeleted(OpdsEntryStatusCache entryStatusCache) {
        long deltaContainersDownloadedSize = entryStatusCache.getEntryContainerDownloadedSize() * -1;
        int deltaContainersDownloaded = entryStatusCache.isEntryContainerDownloaded() ? -1 : 0;
        long deltaSize = entryStatusCache.getEntryAcquisitionLinkLength() -
                entryStatusCache.getEntryContainerDownloadedSize();
        int deltaActiveDownload = entryStatusCache.isEntryActiveDownload() ? -1 : 0;
        int deltaPausedDownloads = entryStatusCache.isEntryPausedDownload() ? -1 : 0;

        updateOnContainerStatusChangedEntry(entryStatusCache.getStatusCacheUid(),
                entryStatusCache.getEntryPendingDownloadBytesSoFar(),
                entryStatusCache.isEntryContainerDownloadPending(),
                entryStatusCache.isEntryActiveDownload(),entryStatusCache.isEntryPausedDownload(),
                0, false,
                entryStatusCache.getEntryAcquisitionLinkLength());

        updateOnContainerStatusChangedIncAncestors(entryStatusCache.getStatusEntryId(),
                0, 0, 0,
                0, deltaContainersDownloadedSize, deltaContainersDownloaded,
                deltaSize);


    }

    /**
     * Synonymous to handleContainerDeleted(findEntryById(entryId))...
     *
     * @param entryId The Entry ID of the entry that has been deleted
     */
    public void handleContainerDeleted(String entryId){
        handleContainerDeleted(findByEntryId(entryId));
    }

    /**
     * This method should be called whne an entry that was being downloaded (and for which there was
     * a corresponding call to handleDownloadJobQueued ) is no longer being downloaded - e.g. it has
     * failed permanently or been cancelled by the user
     *
     * @param entryStatusCache The OpdsEntryStatusCache representing the download that is being aborted
     */
    @UmTransaction
    public void handleContainerDownloadAborted(OpdsEntryStatusCache entryStatusCache){
        long deltaDownloadedBytesSoFar = entryStatusCache.getEntryPendingDownloadBytesSoFar() * -1;
        int deltaContainerDownloadPending = entryStatusCache.isEntryContainerDownloadPending() ? -1 : 0;
        long newEntrySize = entryStatusCache.isEntryContainerDownloaded() ? entryStatusCache.getEntrySize()
                : entryStatusCache.getEntryAcquisitionLinkLength();
        long deltaSize = newEntrySize - entryStatusCache.getEntrySize();
        int deltaActiveDownloads = entryStatusCache.isEntryActiveDownload() ? -1 : 0;
        int deltaPausedDownloads = entryStatusCache.isEntryPausedDownload() ? -1 : 0;

        updateOnContainerStatusChangedIncAncestors(entryStatusCache.getStatusEntryId(),
                deltaDownloadedBytesSoFar, deltaContainerDownloadPending, deltaActiveDownloads,
                deltaPausedDownloads, 0, 0,
                deltaSize);

        updateOnContainerStatusChangedEntry(entryStatusCache.getStatusCacheUid(), 0, false,
                false, false, entryStatusCache.getEntryContainerDownloadedSize(),
                entryStatusCache.isEntryContainerDownloaded(), newEntrySize);
    }

    /**
     * This method should be called whne an entry that was being downloaded (and for which there was
     * a corresponding call to handleDownloadJobQueued ) is no longer being downloaded - e.g. it has
     * failed permanently or been cancelled by the user
     *
     * Synonamous to handleContainerDownloadAborted(findByEntryId(entryId))
     *
     * @param entryId Entry ID of the download that has been aborted
     */
    public void handleContainerDownloadAborted(String entryId) {
        handleContainerDownloadAborted(findByEntryId(entryId));
    }


    @UmTransaction
    public void handleContainerDownloadPaused(OpdsEntryStatusCache entryStatusCache, boolean pausedByUser) {
        int deltaPausedDownloads = pausedByUser ?
                (entryStatusCache.isEntryPausedDownload() ? 0 : 1) : 0;
        int deltaActiveDownloads = entryStatusCache.isEntryActiveDownload() ? -1 : 0;
        UstadMobileSystemImpl.l(UMLog.VERBOSE, 0, "OpdsEntryStatusCacheDao: handleContainerDownloadPaused " +
            "id " + entryStatusCache.getStatusEntryId() + " pausedByUser = " + pausedByUser);
        updateOnContainerStatusChangedIncAncestors(entryStatusCache.getStatusEntryId(),
                0, 0, deltaActiveDownloads,
                deltaPausedDownloads, 0, 0, 0);

        updateOnContainerStatusChangedEntry(entryStatusCache.getStatusCacheUid(),
                entryStatusCache.getEntryPendingDownloadBytesSoFar(), entryStatusCache.isEntryContainerDownloadPending(),
                false, true, entryStatusCache.getEntryContainerDownloadedSize(),
                entryStatusCache.isEntryContainerDownloaded(), entryStatusCache.getEntrySize());
    }

    public void handleContainerDownloadPaused(String entryId) {
        handleContainerDownloadPaused(findByEntryId(entryId), true);
    }

    public void handleContainerDownloadWaitingForNetwork(String entryId) {
        handleContainerDownloadPaused(findByEntryId(entryId), false);
    }


}
