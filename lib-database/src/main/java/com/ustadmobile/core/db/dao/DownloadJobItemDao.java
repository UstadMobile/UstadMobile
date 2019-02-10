package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.util.List;

/**
 * DAO for the DownloadJobItem class
 */
@UmDao
public abstract class DownloadJobItemDao {

    public static class DownloadJobItemToBeCreated {

        long downloadSetItemUid;

        long contentEntryFileUid;

        long fileSize;

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public long getDownloadSetItemUid() {
            return downloadSetItemUid;
        }

        public void setDownloadSetItemUid(long downloadSetItemUid) {
            this.downloadSetItemUid = downloadSetItemUid;
        }

        public long getContentEntryFileUid() {
            return contentEntryFileUid;
        }

        public void setContentEntryFileUid(long contentEntryFileUid) {
            this.contentEntryFileUid = contentEntryFileUid;
        }
    }

    public static class DownloadJobInfo{

        private int totalDownloadItems;

        private long totalSize;

        public int getTotalDownloadItems() {
            return totalDownloadItems;
        }

        public void setTotalDownloadItems(int totalDownloadItems) {
            this.totalDownloadItems = totalDownloadItems;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }
    }

    /**
     * Insert a list of DownloadJobItems
     *
     * @param jobRunItems List of DownloadJobItem to insert
     */
    @UmInsert
    public abstract void insert(List<DownloadJobItem> jobRunItems);

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
     * @param djiUid DownloadJobItemId to updateState (primary key)
     * @param djiStatus status property to set
     * @param downloadedSoFar downloadedSoFar property to set
     * @param downloadLength downloadLength property to set
     * @param currentSpeed currentSpeed property to set
     */
    @UmQuery("Update DownloadJobItem SET " +
            "djiStatus = :djiStatus, downloadedSoFar = :downloadedSoFar, " +
            "downloadLength = :downloadLength, currentSpeed = :currentSpeed " +
            " WHERE djiUid = :djiUid")
    public abstract void updateDownloadJobItemStatus(long djiUid, int djiStatus,
                                                     long downloadedSoFar, long downloadLength,
                                                     long currentSpeed);

    @UmQuery("UPDATE DownloadJobItem SET djiStatus = :status WHERE djiUid = :djiUid")
    public abstract void updateStatus(long djiUid, long status);

    @UmQuery("UPDATE DownloadJobItem SET djiStatus = :djiStatus WHERE djiDjUid = :djiDjUid")
    public abstract void updateStatusByJobId(long djiDjUid, int djiStatus);

    @UmQuery("UPDATE DownloadJobItem SET numAttempts = numAttempts + 1 WHERE djiUid = :djiUid")
    public abstract void incrementNumAttempts(long djiUid);

    @UmQuery("SELECT DownloadJobItem.*, DownloadSetItem.* FROM " +
            "DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid " +
            "WHERE DownloadJobItem.djiUid = :djiUid")
    public abstract DownloadJobItemWithDownloadSetItem findWithDownloadSetItemByUid(long djiUid);

    @UmQuery("SELECT djiStatus FROM DownloadJobItem WHERE djiUid = :djiUid")
    public abstract UmLiveData<Integer> getLiveStatus(long djiUid);

    @UmQuery("SELECT * FROM DownloadJobItem")
    public abstract UmLiveData<List<DownloadJobItem>> findAllLive();

    @UmQuery("SELECT * FROM DownloadJobItem")
    public abstract List<DownloadJobItem> findAll();

    @UmQuery("SELECT COUNT(*) as totalDownloadItems, SUM(downloadLength) as totalSize FROM DownloadJobItem WHERE djiDjUid =:djiDjUid")
    public abstract DownloadJobInfo getDownloadJobInfoByJobUid(long djiDjUid);

    @UmQuery("SELECT DownloadSetItem.dsiUid as downloadSetItemUid,\n" +
            "ContentEntryFile.contentEntryFileUid,\n" +
            "ContentEntryFile.fileSize\n" +
            "FROM DownloadSetItem\n" +
            "LEFT JOIN ContentEntryContentEntryFileJoin ON DownloadSetItem.dsiContentEntryUid = ContentEntryContentEntryFileJoin.cecefjContentEntryUid\n" +
            "LEFT JOIN ContentEntryFile ON ContentEntryFile.lastModified = \n" +
            "\t(SELECT MAX(lastModified) FROM ContentEntryContentEntryFileJoin AS InnerCEFJ\n" +
            "\t\tLEFT JOIN ContentEntryFile ON InnerCEFJ.cecefjContentEntryUid = ContentEntryFile.contentEntryFileUid\n" +
            "\t    WHERE InnerCEFJ.cecefjContentEntryUid = DownloadSetItem.dsiContentEntryUid)\n" +
            "WHERE DownloadSetItem.dsiDsUid = :downloadSetUid")
    public abstract List<DownloadJobItemToBeCreated> findJobItemsToBeCreatedForDownloadSet(long downloadSetUid);

    @UmQuery("SELECT * FROM DownloadJobItem WHERE djiDjUid = :djiDjUid")
    public abstract List<DownloadJobItem> findByJobUid(long djiDjUid);

    @UmQuery("DELETE FROM DownloadJobItem WHERE djiDjUid = :djiDjUid")
    public abstract int deleteByDownloadSetUid(long djiDjUid);

    @UmQuery("SELECT DownloadJobItem.*, DownloadSetItem.* FROM DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid " +
            "LEFT JOIN DownloadSet on DownloadSetItem.dsiDsUid = DownloadSet.dsUid " +
            "WHERE DownloadJobItem.djiStatus >= " + JobStatus.WAITING_MIN +
            " AND DownloadJobItem.djiStatus < " + JobStatus.RUNNING_MIN +
            " AND (((SELECT connectivityState FROM ConnectivityStatus) =  " + ConnectivityStatus.STATE_UNMETERED + ") " +
                " OR ((SELECT connectivityState FROM ConnectivityStatus) = " + ConnectivityStatus.STATE_METERED + ") " +
                " AND DownloadSet.meteredNetworkAllowed) " +
            "LIMIT 1")
    public abstract UmLiveData<List<DownloadJobItemWithDownloadSetItem>> findNextDownloadJobItems();




}
