package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.networkmanager.NetworkTask;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.util.List;

/**
 * DAO for the DownloadJobItem class
 */
@UmDao
public abstract class DownloadJobItemDao {

    /**
     * Insert a list of DownloadJobItems
     *
     * @param jobRunItems List of DownloadJobItem to insert
     */
    @UmInsert
    public abstract void insertList(List<DownloadJobItem> jobRunItems);

    /**
     * Insert a single DownloadJobItem
     *
     * @param jobRunItem DownloadJobItem to insert
     */
    @UmInsert
    public abstract void insert(DownloadJobItem jobRunItem);

    /**
     * Update the main status fields for the given DownloadJobitem
     *
     * @param downloadJobItemId DownloadJobItemId to update (primary key)
     * @param status status property to set
     * @param downloadedSoFar downloadedSoFar property to set
     * @param downloadLength downloadLength property to set
     * @param currentSpeed currentSpeed property to set
     */
    @UmQuery("Update DownloadJobItem SET " +
            "status = :status, downloadedSoFar = :downloadedSoFar, " +
            "downloadLength = :downloadLength, currentSpeed = :currentSpeed " +
            " WHERE downloadJobItemId = :downloadJobItemId")
    public abstract void updateDownloadJobItemStatus(int downloadJobItemId, int status,
                                                     long downloadedSoFar, long downloadLength,
                                                     long currentSpeed);


    /**
     * Find all DownloadJobItems that belong to a specific DownloadJob
     *
     * @param downloadJobId DownloadJobId (primary key) of the DownloadJob
     *
     * @return A List of DownloadJobItem that are part of the given DownloadJob
     */
    @UmQuery("SELECT * FROM DownloadJobItem WHERE downloadJobId = :downloadJobId")
    public abstract List<DownloadJobItem> findAllByDownloadJobId(int downloadJobId);


    /**
     * Updates the key status fields of the given DownloadJobItem (only the status fields as per
     * {@link #updateDownloadJobItemStatus(int, int, long, long, long)}.
     *
     * @param item The DownloadJobItem to update
     */
    public void updateDownloadJobItemStatus(DownloadJobItem item) {
        updateDownloadJobItemStatus(item.getDownloadJobItemId(), item.getStatus(), item.getDownloadedSoFar(),
                item.getDownloadLength(), item.getCurrentSpeed());
    }

    @UmQuery("UPDATE DownloadJobItem SET status = :status WHERE downloadJobItemId = :downloadJobItemId")
    public abstract void updateStatus(int downloadJobItemId, int status);


    /**
     * Get a UmLiveData object for the DownloadJobItem for a specific entryId within a specific status range
     *
     * @param entryId OPDS EntryId
     * @param statusFrom Minimum status property value
     * @param statusTo Maximum status property value
     * @return UmLiveData object for the first DownloadJobItem matching the arguments
     */
    @UmQuery("SELECT * FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE DownloadSetItem.entryId = :entryId AND DownloadJobItem.status BETWEEN :statusFrom AND :statusTo ")
    public abstract UmLiveData<DownloadJobItemWithDownloadSetItem> findDownloadJobItemByEntryIdAndStatusRangeLive(String entryId,
                                                                                               int statusFrom,
                                                                                               int statusTo);


    /**
     * Get a List of DownloadJobItems for a specific entryId within a specific status range
     *
     * @param entryId OPDS EntryId
     * @param statusFrom Minimum status property value
     * @param statusTo Maximum status property value
     * @return List of DownloadJobItem  matching the arguments.
     */
    @UmQuery("SELECT DownloadJobItem.* FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE DownloadSetItem.entryId = :entryId AND DownloadJobItem.status BETWEEN :statusFrom AND :statusTo ")
    public abstract List<DownloadJobItem> findDownloadJobItemByEntryIdAndStatusRange(String entryId,
                                                                                     int statusFrom,
                                                                                     int statusTo);

    /**
     * Get a list of the download job items that are part of the given download job id.
     *
     * @param downloadJobId
     *
     * @return
     */
    @UmQuery("SELECT DownloadJobItem.*, DownloadSetItem.* FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE DownloadJobItem.downloadJobId = :downloadJobId ")
    public abstract List<DownloadJobItemWithDownloadSetItem> findAllWithDownloadSet(int downloadJobId);

    /**
     * Find the DownloadJobItem for the given entryId that was most recently completed
     *
     * @param entryId EntryID to search by
     *
     * @return The most recently completed DownloadJobItem (as per the timeFinished proeprty)
     */
    @UmQuery("SELECT DownloadJobItem.* FROM DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            "WHERE DownloadSetItem.entryId = :entryId " +
            "ORDER BY DownloadJobItem.timeFinished DESC")
    public abstract DownloadJobItem findLastFinishedJobItem(String entryId);


    /**
     * Get a DownloadJobItemWithDownloadSetItem (using a join) for the given DownloadJobId (used
     * to find the next item to download)
     *
     * @param downloadJobId The downloadJobItemId of the given DownloadJob (primary key)
     * @param statusFrom Minimum status to find
     * @param statusTo Maximum status to find
     * @return The first matching DownloadJobItem, with it's DownloadSetItem as an embedded object.
     */
    @UmQuery("SELECT * FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE downloadJobId = :downloadJobId AND DownloadJobItem.status BETWEEN :statusFrom AND :statusTo LIMIT 1 ")
    public abstract DownloadJobItemWithDownloadSetItem findNextByDownloadJobAndStatusRange(int downloadJobId, int statusFrom,
                                                                                           int statusTo);

    @UmQuery("SELECT * FROM DownloadJobItem " +
            " LEFT JOIN DownloadSetItem ON DownloadJobItem.downloadSetItemId = DownloadSetItem.id " +
            " WHERE downloadJobId = :downloadJobId AND DownloadJobItem.status BETWEEN :statusFrom AND :statusTo")
    public abstract List<DownloadJobItemWithDownloadSetItem> findByDownloadJobAndStatusRange(int downloadJobId, int statusFrom, int statusTo);

    /**
     * Get an int array for all the downloadJobItemIds for all the DownloadJobItem objects that are in
     * a DownloadJob
     *
     * @param downloadJobId Primary key of the given DownloadJob
     *
     * @return Array of ints with the primary key of all DownloadJobItem objects in the given DownloadJob
     */
    @UmQuery("SELECT downloadJobItemId FROM DownloadJobItem WHERE downloadJobId = :downloadJobId")
    public abstract int[] findAllIdsByDownloadJob(int downloadJobId);

    @UmQuery("SELECT * FROM DownloadJobItem WHERE downloadJobItemId = :downloadJobItemId")
    public abstract DownloadJobItem findById(int downloadJobItemId);

    /**
     * Unpause any DownloadJobItems that have been marked as paused. This is used when a job is
     * queud, in case it was previously paused.
     *
     * @param downloadJobId id of the downloadjob that is being started
     */
    @UmQuery("UPDATE DownloadJobItem SET status = " + NetworkTask.STATUS_QUEUED +
            " WHERE status < " + NetworkTask.STATUS_WAITING_MIN + " AND " +
            " downloadJobId = :downloadJobId")
    public abstract void updateUnpauseItemsByDownloadJob(int downloadJobId);

    /**
     * Update the DownloadJobItem when the destination file it is going to be saved to is known. This
     * is used when a download is canceled and the download is not completed (e.g. the file should
     * be deleted, but there is no ContainerFile entity yet).
     *
     * @param downloadJobItemId DownloadJobItem id
     * @param destinationFile The path to the destination file this item is being saved into
     */
    @UmQuery("UPDATE DownloadJobItem SET destinationFile = :destinationFile " +
            "WHERE downloadJobItemId = :downloadJobItemId")
    public abstract void updateDestinationFile(int downloadJobItemId, String destinationFile);

    /**
     * Update the number of attempts for a given DownloadJobItem.
     *
     * @param downloadJobItemId primary key of the DownloadJob to update
     * @param numAttempts value for numAttempts field
     */
    @UmQuery("UPDATE DownloadJobItem SET numAttempts = :numAttempts " +
            "WHERE downloadJobItemId = :downloadJobItemId")
    public abstract void updateNumAttempts(int downloadJobItemId, int numAttempts);

}
