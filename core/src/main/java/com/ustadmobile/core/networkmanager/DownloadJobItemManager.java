package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmResultCallback;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemParentChildJoin;
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus;

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

    private HashMap<Integer, DownloadJobItemStatus> jobItemUidToStatusMap = new HashMap<>();

    Set<DownloadJobItemStatus> changedItems = new HashSet<>();

    private UmAppDatabase db;

    private OnDownloadJobItemChangeListener onDownloadJobItemChangeListener;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public DownloadJobItemManager(UmAppDatabase db, int djUid) {
        this.db = db;
    }

    private void loadFromDb() {

    }

    public void updateProgress(int djiUid, long bytesSoFar, long totalBytes, byte state) {
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
        while(parents != null && !parents.isEmpty()) {
            LinkedList<DownloadJobItemStatus> nextParents = new LinkedList<>();
            for(DownloadJobItemStatus parent : parents) {
                parent.incrementTotalBytes(deltaTotalBytes);
                parent.incrementBytesSoFar(deltaBytesFoFar);
                changedItems.add(djStatus);
                if(onDownloadJobItemChangeListener != null)
                    onDownloadJobItemChangeListener.onDownloadJobItemChange(djStatus);

                if(parent.getParents() != null)
                    nextParents.addAll(parent.getParents());
            }

            parents = nextParents;
        }
    }

    public void insertDownloadJobItems(List<DownloadJobItem> items, UmResultCallback<Void> callback) {
        executor.execute(() -> {
            db.getDownloadJobItemDao().insertListAndSetIds(items);
            for(DownloadJobItem item : items) {
                jobItemUidToStatusMap.put((int)item.getDjiUid(), new DownloadJobItemStatus(item));
            }
            if(callback !=null)
                callback.onDone(null);
        });
    }

    public void insertParentChildJoins(List<DownloadJobItemParentChildJoin> joins,
                                       UmResultCallback<Void> callback){
        executor.execute(() -> {
            for(DownloadJobItemParentChildJoin join : joins) {
                DownloadJobItemStatus childStatus = jobItemUidToStatusMap.get((int)join.getDjiChildDjiUid());
                DownloadJobItemStatus parentStatus = jobItemUidToStatusMap.get((int)join.getDjiParentDjiUid());
                if(childStatus == null || parentStatus == null)
                    throw new IllegalArgumentException("Parent child join requested: but child or parent uids invalid");

                if(join.getDjiChildDjiUid() == join.getDjiParentDjiUid())
                    throw new IllegalArgumentException("childItemUid = parentItemUid");

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

    public void commit() {
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
