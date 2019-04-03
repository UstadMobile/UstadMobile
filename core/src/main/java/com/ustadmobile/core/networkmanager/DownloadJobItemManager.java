package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus;
import com.ustadmobile.lib.util.UMUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DownloadJobItemManager {

    public interface OnDownloadJobItemChangeListener {

        void onDownloadJobItemChange(DownloadJobItemStatus status);

    }

    private int downloadJobUid;

    private HashMap<Integer, DownloadJobItemStatus> jobItemUidToStatusMap = new HashMap<>();

    Set<DownloadJobItemStatus> changedItems = new HashSet<>();

    private UmAppDatabase db;

    private OnDownloadJobItemChangeListener onDownloadJobItemChangeListener;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public DownloadJobItemManager(UmAppDatabase db, int downloadJobUid, UmResultCallback<Void> loadCallback) {
        this.db = db;
        this.downloadJobUid = downloadJobUid;
        executor.execute(() -> loadFromDb(loadCallback));
    }

    public DownloadJobItemManager(UmAppDatabase db, int downloadJobUid) {
        this(db, downloadJobUid, null);
    }

    private void loadFromDb(UmResultCallback<Void> loadCallback) {
        List<DownloadJobItemStatus> jobItems = db.getDownloadJobItemDao()
                .findStatusByDownlaodJobUid(downloadJobUid);
        for(DownloadJobItemStatus status : jobItems) {
            jobItemUidToStatusMap.put(status.getJobItemUid(), status);
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

        if(loadCallback != null)
            loadCallback.onDone(null);
    }

    public void updateProgress(int djiUid, long bytesSoFar, long totalBytes, byte state) {
        executor.execute(() -> {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Updating ID #" +
                    djiUid + " bytesSoFar = " + bytesSoFar + " totalBytes=" + totalBytes);
            DownloadJobItemStatus djStatus = jobItemUidToStatusMap.get(djiUid);
            long deltaBytesFoFar = bytesSoFar - djStatus.getBytesSoFar();
            long deltaTotalBytes = totalBytes - djStatus.getTotalBytes();

            djStatus.setBytesSoFar(bytesSoFar);
            djStatus.setTotalBytes(totalBytes);
            changedItems.add(djStatus);
            djStatus.setState(state);

            if(onDownloadJobItemChangeListener != null)
                onDownloadJobItemChangeListener.onDownloadJobItemChange(djStatus);

            List<DownloadJobItemStatus> parents = djStatus.getParents();
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
                    changedItems.add(djStatus);
                    if(onDownloadJobItemChangeListener != null)
                        onDownloadJobItemChangeListener.onDownloadJobItemChange(djStatus);

                    if(parent.getParents() != null)
                        nextParents.addAll(parent.getParents());
                }

                parents = nextParents;
                UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "\tUpdating ID #" +
                        djiUid + " next parents = " + UMUtil.debugPrintList(parents));
            }
        });
    }

    public void insertDownloadJobItems(List<DownloadJobItem> items, UmResultCallback<Void> callback) {
        executor.execute(() -> {
            UstadMobileSystemImpl.l(UMLog.DEBUG, 420, "Adding download job items" +
                    UMUtil.debugPrintList(items));
            db.getDownloadJobItemDao().insertListAndSetIds(items);

            for(DownloadJobItem item : items) {
                Integer uidIntObj = Integer.valueOf((int)item.getDjiUid());
                jobItemUidToStatusMap.put(uidIntObj, new DownloadJobItemStatus(item));

            }
            if(callback !=null)
                callback.onDone(null);
        });
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

                updateProgress((int)join.getDjiParentDjiUid(), childStatus.getBytesSoFar(),
                        childStatus.getTotalBytes(),
                        parentStatus.getState());
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
}
