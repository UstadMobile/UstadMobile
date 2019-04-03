package com.ustadmobile.core.db.dao;

import com.ustadmobile.core.db.JobStatus;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.entities.ConnectivityStatus;
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DAO for the DownloadJobItem class
 */
@UmDao
public abstract class DownloadJobItemDao {

    public static class DownloadJobItemToBeCreated{

        long downloadSetItemUid;

        long containerUid;

        long fileSize;

        long contentEntryUid;

        public long getDownloadSetItemUid() {
            return downloadSetItemUid;
        }

        public void setDownloadSetItemUid(long downloadSetItemUid) {
            this.downloadSetItemUid = downloadSetItemUid;
        }

        public long getContainerUid() {
            return containerUid;
        }

        public void setContainerUid(long containerUid) {
            this.containerUid = containerUid;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public long getContentEntryUid() {
            return contentEntryUid;
        }

        public void setContentEntryUid(long contentEntryUid) {
            this.contentEntryUid = contentEntryUid;
        }
    }

    public static class DownloadJobItemToBeCreated2 {

        long cepcjUid;

        long contentEntryUid;

        long containerUid;

        long fileSize;

        long parentEntryUid;

        public long getContentEntryUid() {
            return contentEntryUid;
        }

        public void setContentEntryUid(long contentEntryUid) {
            this.contentEntryUid = contentEntryUid;
        }

        public long getContainerUid() {
            return containerUid;
        }

        public void setContainerUid(long containerUid) {
            this.containerUid = containerUid;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public long getParentEntryUid() {
            return parentEntryUid;
        }

        public void setParentEntryUid(long parentEntryUid) {
            this.parentEntryUid = parentEntryUid;
        }

        public long getCepcjUid() {
            return cepcjUid;
        }

        public void setCepcjUid(long cepcjUid) {
            this.cepcjUid = cepcjUid;
        }
    }


    /**
     * Insert a list of DownloadJobItems
     *
     * @param jobRunItems List of DownloadJobItem to insert
     */
    @UmInsert
    public abstract void insert(List<DownloadJobItem> jobRunItems);

    @UmTransaction
    public void insertListAndSetIds(List<DownloadJobItem> jobItems) {
        for(DownloadJobItem item : jobItems) {
            item.setDjiUid(insert(item));
        }
    }

    @UmTransaction
    public void updateDownloadJobItemsProgress(List<DownloadJobItemStatus> statusList) {
        for(DownloadJobItemStatus status : statusList) {
           updateDownloadJobItemProgress(status.getJobItemUid(), status.getBytesSoFar(),
                   status.getTotalBytes(), status.getState());
        }
    }

    @UmQuery("UPDATE DownloadJobItem SET downloadedSoFar = :bytesSoFar, " +
            "downloadLength = :totalLength," +
            "djiStatus = :status WHERE djiUid = :djiUid")
    public abstract void updateDownloadJobItemProgress(int djiUid, long bytesSoFar, long totalLength,
                                                       int status);



    @UmQuery("SELECT DownloadJobItem.*, DownloadSetItem.* FROM DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid " +
            "WHERE DownloadJobItem.djiUid = :djiUid")
    public abstract DownloadJobItemWithDownloadSetItem findByUidWithDownloadSetItem(long djiUid);

    @UmUpdate
    public abstract int update(DownloadJob downloadJob);

    @UmUpdate
    public abstract void updateList(List<DownloadJobItem> downloadJobList);

    /**
     * Insert a single DownloadJobItem
     *
     * @param jobRunItem DownloadJobItem to insert
     */
    @UmInsert
    public abstract long insert(DownloadJobItem jobRunItem);

    @UmQuery("DELETE FROM DownloadJobItem")
    public abstract void deleteAll(UmCallback<Void> callback);

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
    protected abstract void updateDownloadJobItemStatusIm(long djiUid, int djiStatus,
                                                     long downloadedSoFar, long downloadLength,
                                                     long currentSpeed);

    public  void updateDownloadJobItemStatus(long djiUid, int djiStatus,
                                                     long downloadedSoFar, long downloadLength,
                                                     long currentSpeed) {
        System.out.println("updateDownloadJobItemStatus " + djiUid + " -> " + djiStatus);
        updateDownloadJobItemStatusIm(djiUid, djiStatus, downloadedSoFar, downloadLength,
                currentSpeed);
    }

    @UmQuery("UPDATE DownloadJobItem SET downloadedSoFar = :downloadedSoFar, " +
            "currentSpeed = :currentSpeed " +
            "WHERE djiUid = :djiUid")
    public abstract void updateDownloadJobItemProgress(long djiUid, long downloadedSoFar,
                                                       long currentSpeed);


    @UmQuery("UPDATE DownloadJobItem SET djiStatus = :status WHERE djiUid = :djiUid")
    protected abstract void updateItemStatusInt(long djiUid, long status);

    public void updateStatus(long djiUid, long status){
        System.out.println("DownloadJob #" +djiUid+ " updating status to " + status);
        updateItemStatusInt(djiUid,status);
    }


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

    @UmQuery("SELECT COUNT(*) FROM DownloadJobItem WHERE djiDjUid =:djiDjUid")
    public abstract int  getTotalDownloadJobItems(long djiDjUid);

    @UmQuery("SELECT DownloadSetItem.dsiUid as downloadSetItemUid, Container.fileSize, \n" +
            "Container.containerContentEntryUid as contentEntryUid , Container.containerUid\n" +
            "FROM DownloadSetItem LEFT JOIN Container \n" +
            "ON Container.containerUid = (SELECT Container.containerUid FROM Container \n" +
            "WHERE containerContentEntryUid =  DownloadSetItem.dsiContentEntryUid ORDER BY lastModified  DESC LIMIT 1) \n" +
            "WHERE  Container.fileSize != 0 AND DownloadSetItem.dsiDsUid = :downloadSetUid")
    public abstract List<DownloadJobItemToBeCreated> findJobItemsToBeCreatedDownloadSet(long downloadSetUid);

    @UmQuery("SELECT destinationFile FROM DownloadJobItem WHERE djiUid != 0 AND djiDsiUid IN(:djiDsiUids)")
    public abstract List<String> getDestinationFiles(List<Long> djiDsiUids);


    @UmQuery("SELECT DownloadJobItem.*, DownloadSetItem.* FROM DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid " +
            "LEFT JOIN DownloadSet on DownloadSetItem.dsiDsUid = DownloadSet.dsUid " +
            "WHERE " +
            " DownloadJobItem.djiContainerUid != 0 " +
            " AND DownloadJobItem.djiStatus >= " + JobStatus.WAITING_MIN +
            " AND DownloadJobItem.djiStatus < " + JobStatus.RUNNING_MIN +
            " AND (((SELECT connectivityState FROM ConnectivityStatus) =  " + ConnectivityStatus.STATE_UNMETERED + ") " +
                " OR ((SELECT connectivityState FROM ConnectivityStatus) = " + ConnectivityStatus.STATE_METERED + ") " +
                " AND DownloadSet.meteredNetworkAllowed) " +
            "LIMIT 1")
    public abstract UmLiveData<List<DownloadJobItemWithDownloadSetItem>> findNextDownloadJobItems();

    @UmQuery("SELECT DownloadJobItem.*, DownloadSetItem.* FROM DownloadJobItem " +
            "LEFT JOIN DownloadSetItem ON DownloadJobItem.djiDsiUid = DownloadSetItem.dsiUid " +
            "WHERE DownloadSetItem.dsiContentEntryUid = :contentEntryUid " +
            "ORDER BY DownloadJobItem.timeStarted DESC LIMIT 1")
    public abstract DownloadJobItemWithDownloadSetItem findByContentEntryUid(long contentEntryUid);


    @UmQuery("SELECT * " +
            "FROM DownloadJobItem " +
            "WHERE djiContentEntryUid = :contentEntryUid " +
            "ORDER BY DownloadJobItem.timeStarted DESC LIMIT 1")
    public abstract DownloadJobItem findByContentEntryUid2(long contentEntryUid);


    @UmQuery("SELECT DownloadJobItem.* " +
            "FROM DownloadJobItem " +
            "WHERE DownloadJobItem.djiUid IN (:contentEntryUids) " +
            "ORDER BY DownloadJobItem.timeStarted DESC LIMIT 1")
    public abstract List<DownloadJobItem> findByDjiUidsList(List<Long> contentEntryUids);

    @UmQuery("SELECT djiUid AS jobItemUid, " +
            "djiContentEntryUid AS contentEntryUid, " +
            "downloadedSoFar AS bytesSoFar, " +
            "downloadLength AS totalBytes," +
            "djiStatus AS state " +
            "FROM DownloadJobItem " +
            "WHERE DownloadJobItem.djiDjUid = :downloadJobUid")
    public abstract List<DownloadJobItemStatus> findStatusByDownlaodJobUid(long downloadJobUid);

    private DownloadJobItem generateDjiFromContainerWithContentEntry(
            ContainerWithContentEntry containerWithContentEntry) {
        return new DownloadJobItem();
    }

    public List<DownloadJobItemToBeCreated2> findByParentContentEntryUuids(
            List<Long> parentContentEntryUids) {
        return null;
    }

    @UmInsert
    public abstract void insertDownloadJobItemParentChildJoin(DownloadJobItemParentChildJoin dj);


    @UmTransaction
    public void createJobItemsRecursively(long rootContentEntryUid, UmAppDatabase repo) {
        List<DownloadJobItemToBeCreated2> childItemsToCreate;
        DownloadJobItem rootDownlaodItem = new DownloadJobItem();
        rootDownlaodItem.setDjiUid(insert(rootDownlaodItem));

        Map<Long, Long> contentEntryUidToDjiUidMap = new HashMap<>();
        List<Long> parentUids = new ArrayList<>();
        parentUids.add(rootContentEntryUid);
        contentEntryUidToDjiUidMap.put(rootContentEntryUid, rootDownlaodItem.getDjiUid());

        List<DownloadJobItem> downloadJobItems = new ArrayList<>();
        HashSet<Long> createdJoinCepjUids = new HashSet<>();

        do {
            childItemsToCreate = findByParentContentEntryUuids(parentUids);
            parentUids.clear();

            for(DownloadJobItemToBeCreated2 child : childItemsToCreate){
                if(!contentEntryUidToDjiUidMap.containsKey(child.getContentEntryUid())) {
                    DownloadJobItem newItem = new DownloadJobItem();
                    //TODO: fill in the item
                    newItem.setDjiUid(insert(newItem));

                    contentEntryUidToDjiUidMap.put(child.getContentEntryUid(),
                            newItem.getDjiUid());

                    if(newItem.getDjiContainerUid() != 0)
                        parentUids.add(child.getContentEntryUid());

                    newItem.setDjiUid(insert(newItem));
                }

                if(!createdJoinCepjUids.contains(child.getCepcjUid())) {
                    insertDownloadJobItemParentChildJoin(new DownloadJobItemParentChildJoin(
                        contentEntryUidToDjiUidMap.get(child.getParentEntryUid()),
                            contentEntryUidToDjiUidMap.get(child.getContentEntryUid()),
                            child.getCepcjUid()));
                    createdJoinCepjUids.add(child.getCepcjUid());
                }
            }
        }while(!parentUids.isEmpty());
    }

}
