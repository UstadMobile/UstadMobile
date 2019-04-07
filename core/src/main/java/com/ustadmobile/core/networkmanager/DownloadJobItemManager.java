package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.DownloadJob;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus;
import com.ustadmobile.lib.util.UMUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DownloadJobItemManager {

    public interface OnDownloadJobItemChangeListener {

        void onDownloadJobItemChange(DownloadJobItemStatus status);

    }

    private int downloadJobUid;

    private HashMap<Integer, DownloadJobItemStatus> jobItemUidToStatusMap = new HashMap<>();

    private Set<DownloadJobItemStatus> changedItems = new HashSet<>();

    private UmAppDatabase db;

    private OnDownloadJobItemChangeListener onDownloadJobItemChangeListener;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private volatile DownloadJobItemStatus rootItemStatus;

    private volatile long rootContentEntryUid;

    public DownloadJobItemManager(UmAppDatabase db, int downloadJobUid) {
        this.db = db;
        this.downloadJobUid = downloadJobUid;
        executor.scheduleWithFixedDelay(this::doCommit, 1000, 1000, TimeUnit.MILLISECONDS);
        try {
            executor.schedule(() -> loadFromDb(), 0, TimeUnit.SECONDS).get();
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadFromDb() {
        DownloadJob downloadJob = db.getDownloadJobDao().findByUid(downloadJobUid);
        rootContentEntryUid = downloadJob.getDjRootContentEntryUid();
        UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "DownloadJobItemManager: load " +
                "Download job uid " + downloadJobUid + " root content entry uid = " +
                rootContentEntryUid);

        List<DownloadJobItemStatus> jobItems = db.getDownloadJobItemDao()
                .findStatusByDownlaodJobUid(downloadJobUid);
        for(DownloadJobItemStatus status : jobItems) {
            jobItemUidToStatusMap.put(status.getJobItemUid(), status);
            if(status.getContentEntryUid() == rootContentEntryUid)
                rootItemStatus = status;
        }

        List<DownloadJobItemParentChildJoin> joinList = db.getDownloadJobItemParentChildJoinDao()
                .findParentsByChildUid(downloadJobUid);
        for(DownloadJobItemParentChildJoin join : joinList) {
            DownloadJobItemStatus parentStatus = jobItemUidToStatusMap.get((int)join.getDjiParentDjiUid());
            DownloadJobItemStatus childStatus = jobItemUidToStatusMap.get((int)join.getDjiChildDjiUid());

            if(parentStatus == null || childStatus == null) {
                throw new IllegalStateException("Invalid parent/child join");
            }

            childStatus.addParent(parentStatus);
        }
    }

    public void updateProgress(int djiUid, long bytesSoFar, long totalBytes) {
        executor.execute(() -> {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Updating ID #" +
                    djiUid + " bytesSoFar = " + bytesSoFar + " totalBytes=" + totalBytes);
            DownloadJobItemStatus djStatus = jobItemUidToStatusMap.get(djiUid);
            long deltaBytesFoFar = bytesSoFar - djStatus.getBytesSoFar();
            long deltaTotalBytes = totalBytes - djStatus.getTotalBytes();

            djStatus.setBytesSoFar(bytesSoFar);
            djStatus.setTotalBytes(totalBytes);
            changedItems.add(djStatus);

            if(onDownloadJobItemChangeListener != null)
                onDownloadJobItemChangeListener.onDownloadJobItemChange(djStatus);

            updateAllParents(djStatus.getJobItemUid(), djStatus.getParents(), deltaBytesFoFar,
                    deltaTotalBytes);
        });
    }

    private void updateAllParents(int djiUid, List<DownloadJobItemStatus> parents, long deltaBytesFoFar,
                                  long deltaTotalBytes) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Updating ID #" +
                djiUid + " parents = " + UMUtil.debugPrintList(parents) +
                " deltaBytesSoFar=" + deltaBytesFoFar + ", deltaTotalBytes=" + deltaTotalBytes);
        while(parents != null && !parents.isEmpty()) {
            LinkedList<DownloadJobItemStatus> nextParents = new LinkedList<>();
            for(DownloadJobItemStatus parent : parents) {
                UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "\tIncrement parent" +
                        parent.getJobItemUid());
                parent.incrementTotalBytes(deltaTotalBytes);
                parent.incrementBytesSoFar(deltaBytesFoFar);
                changedItems.add(parent);
                if(onDownloadJobItemChangeListener != null)
                    onDownloadJobItemChangeListener.onDownloadJobItemChange(parent);

                if(parent.getParents() != null)
                    nextParents.addAll(parent.getParents());
            }

            parents = nextParents;
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "\tUpdating ID #" +
                    djiUid + " next parents = " + UMUtil.debugPrintList(parents));
        }
    }

    public void insertDownloadJobItems(List<DownloadJobItem> items, UmResultCallback<Void> callback) {
        executor.execute(() -> {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Adding download job items" +
                    UMUtil.debugPrintList(items));
            db.getDownloadJobItemDao().insertListAndSetIds(items);

            for(DownloadJobItem item : items) {
                Integer uidIntObj = Integer.valueOf((int)item.getDjiUid());

                DownloadJobItemStatus itemStatus = new DownloadJobItemStatus(item);
                jobItemUidToStatusMap.put(uidIntObj, itemStatus);
                if(item.getDjiContentEntryUid() == rootContentEntryUid)
                    rootItemStatus = itemStatus;
            }

            if(callback !=null)
                callback.onDone(null);
        });
    }

    public void insertDownloadJobItemsSync(List<DownloadJobItem> items) {
        CountDownLatch latch = new CountDownLatch(1);
        insertDownloadJobItems(items, (aVoid) -> latch.countDown());
        try {
            latch.await(5, TimeUnit.SECONDS);
        }catch(InterruptedException e) { /*should not happen */ }
    }


    public void insertParentChildJoins(List<DownloadJobItemParentChildJoin> joins,
                                       UmResultCallback<Void> callback){
        executor.execute(() -> {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Adding parent-child joins" +
                    UMUtil.debugPrintList(joins));
            for(DownloadJobItemParentChildJoin join : joins) {
                DownloadJobItemStatus childStatus = jobItemUidToStatusMap.get((int)join.getDjiChildDjiUid());
                DownloadJobItemStatus parentStatus = jobItemUidToStatusMap.get((int)join.getDjiParentDjiUid());
                if(childStatus == null || parentStatus == null) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 420,
                            "Parent child join requested: but child or parent uids invalid: " + join);
                    throw new IllegalArgumentException("Parent child join requested: but child or parent uids invalid");
                }

                if(join.getDjiChildDjiUid() == join.getDjiParentDjiUid()) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 420,
                            "Parent child join requested: child uid = parent uid: " + join);
                    throw new IllegalArgumentException("childItemUid = parentItemUid");
                }

                childStatus.addParent(parentStatus);

                updateAllParents(childStatus.getJobItemUid(), Arrays.asList(parentStatus),
                        childStatus.getBytesSoFar(), childStatus.getTotalBytes());
            }

            db.getDownloadJobItemParentChildJoinDao().insertList(joins);

            if(callback != null)
                callback.onDone(null);
        });
    }

    public void findStatusByContentEntryUid(long contentEntryUid, UmResultCallback<DownloadJobItemStatus> callback) {
        executor.execute(() -> {
            for(DownloadJobItemStatus status : jobItemUidToStatusMap.values()) {
                if(status.getContentEntryUid() == contentEntryUid) {
                    callback.onDone(status);
                    return;
                }
            }

            callback.onDone(null);
        });
    }

    public void commit(UmResultCallback<Void> callback){
        executor.execute(() -> {
            doCommit();
            callback.onDone(null);
        });
    }

    private void doCommit() {
        db.getDownloadJobItemDao().updateDownloadJobItemsProgress(new LinkedList<>(changedItems));
        changedItems.clear();
    }

    public OnDownloadJobItemChangeListener getOnDownloadJobItemChangeListener() {
        return onDownloadJobItemChangeListener;
    }

    public void setOnDownloadJobItemChangeListener(OnDownloadJobItemChangeListener onDownloadJobItemChangeListener) {
        this.onDownloadJobItemChangeListener = onDownloadJobItemChangeListener;
    }

    public int getDownloadJobUid() {
        return downloadJobUid;
    }

    public DownloadJobItemStatus getRootItemStatus() {
        return rootItemStatus;
    }

    public long getRootContentEntryUid() {
        return rootContentEntryUid;
    }

    public void close() {
        executor.shutdownNow();
    }
}
