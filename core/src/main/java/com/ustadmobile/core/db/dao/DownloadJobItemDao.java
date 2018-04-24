package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.util.List;

/**
 * DAO for the DownloadJobItem class
 */
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


    /**
     * Get a UmLiveData object for the DownloadJobItem for a specific entryId within a specific status range
     *
     * @param entryId OPDS EntryId
     * @param statusFrom Minimum status property value
     * @param statusTo Maximum status property value
     * @return UmLiveData object for the first DownloadJobItem matching the arguments
     */
    @UmQuery("Select * FROM DownloadJobItemRun WHERE entryId = :entryId AND status BETWEEN :statusFrom AND :statusTo")
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
    @UmQuery("Select * FROM DownloadJobItemRun WHERE entryId = :entryId AND status BETWEEN :statusFrom AND :statusTo")
    public abstract List<DownloadJobItem> findDownloadJobItemByEntryIdAndStatusRange(String entryId,
                                                                                     int statusFrom,
                                                                                     int statusTo);

    /**
     * Find the DownloadJobItem for the given entryId that was most recently completed
     *
     * @param entryId EntryID to search by
     *
     * @return The most recently completed DownloadJobItem (as per the timeFinished proeprty)
     */
    @UmQuery("SELECT * From DownloadJobItem WHERE entryId = ")
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
    @UmQuery("SELECT * FROM DownloadJobItemRun WHERE downloadJobId = :downloadJobId AND status BETWEEN :statusFrom AND :statusTo LIMIT 1")
    public abstract DownloadJobItemWithDownloadSetItem findByDownloadJobAndStatusRange(int downloadJobId, int statusFrom,
                                                                                       int statusTo);


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

}
