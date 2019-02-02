package com.ustadmobile.core.db.dao;

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
    public abstract long insert(DownloadJobItem jobRunItem);

    /**
     * Update the main status fields for the given DownloadJobitem
     *
     * @param djiUid DownloadJobItemId to update (primary key)
     * @param djiStatus status property to set
     * @param downloadedSoFar downloadedSoFar property to set
     * @param downloadLength downloadLength property to set
     * @param currentSpeed currentSpeed property to set
     */
    @UmQuery("Update DownloadJobItem SET " +
            "djiStatus = :djiStatus, downloadedSoFar = :downloadedSoFar, " +
            "downloadLength = :downloadLength, currentSpeed = :currentSpeed " +
            " WHERE djiUid = :djiUid")
    public abstract void updateDownloadJobItemStatus(int djiUid, int djiStatus,
                                                     long downloadedSoFar, long downloadLength,
                                                     long currentSpeed);

    @UmQuery("UPDATE DownloadJobItem SET djiStatus = :status WHERE djiUid = :djiUid")
    public abstract void updateStatus(int djiUid, int status);

    @UmQuery("SELECT DownloadJobItem.*, DownloadSetItem.* FROM " +
            "DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid " +
            "WHERE DownloadJobItem.djiUid = :djiUid")
    public abstract DownloadJobItemWithDownloadSetItem findWithDownloadSetItemByUid(int djiUid);

}
