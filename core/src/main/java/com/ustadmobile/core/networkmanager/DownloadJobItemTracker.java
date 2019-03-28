package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DownloadJobItemTracker is designed to enable in-memory tracking of the status of DownloadJobItems.
 * Using LiveData directly connected to the database creates too many change events (and each of
 * these change events forces any query observing that table to run again), so it is not
 * possible to simply use the normal database/LiveData combination to track the progress of downloads,
 * as this involves frequent (e.g. once per second) updates.
 *
 */
public class DownloadJobItemTracker {

    private UmAppDatabase db;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Map<Long, TrackedContentEntry> contentEntryUidToTrackedEntryMap = new HashMap<>();

    private Map<Long, TrackedContentEntry> downloadJobItemUidToTrackedEntryMap = new HashMap<>();

    private AtomicReference<Set<DownloadJobItemWithDownloadSetItem>> updatedItems =
            new AtomicReference<>(new HashSet<>());

    static class TrackedContentEntry {

        private AtomicReference<DownloadJobItemWithDownloadSetItem> downloadJobItem =
                new AtomicReference<>();

        private AtomicBoolean changed = new AtomicBoolean(false);

        private TrackedContentEntry parent;

        private List<TrackedContentEntry> children;

        private List<UmObserver<DownloadJobItemWithDownloadSetItem>> observers = new ArrayList<>();

        private final AtomicReference<Set<DownloadJobItemWithDownloadSetItem>> updatedItemsRef;

        TrackedContentEntry(DownloadJobItemWithDownloadSetItem downloadJobItem,
                            AtomicReference<Set<DownloadJobItemWithDownloadSetItem>> updatedItemsRef) {
            this.downloadJobItem.set(downloadJobItem);
            this.updatedItemsRef = updatedItemsRef;
        }

        void updateDownloadJob(DownloadJobItemWithDownloadSetItem newItem) {
            updatedItemsRef.get().add(newItem);
            downloadJobItem.set(newItem);
            fireOnChanged();
        }

        void updateFromDelta(DownloadJobItem delta) {
            DownloadJobItemWithDownloadSetItem item = downloadJobItem.get();
            item.setDownloadedSoFar(item.getDownloadedSoFar() + delta.getDownloadedSoFar());
            item.setDownloadLength(item.getDownloadLength() + delta.getDownloadLength());
            updatedItemsRef.get().add(item);
            fireOnChanged();
        }

        void addObserver(UmObserver<DownloadJobItemWithDownloadSetItem> observer) {
            observers.add(observer);
            //TODO: fire out first onChange event
        }

        void fireOnChanged() {
            List<UmObserver<DownloadJobItemWithDownloadSetItem>> observersToNotify =
                    new ArrayList<>(observers);
            for(UmObserver<DownloadJobItemWithDownloadSetItem> observer : observersToNotify) {
                observer.onChanged(downloadJobItem.get());
            }
        }

        private boolean hasActiveObservers() {
            if(!observers.isEmpty())
                return true;

            List<TrackedContentEntry> childEntries = new ArrayList<>(children);
            HashSet<Long> checkedIds = new HashSet<>();
            do {
                for(TrackedContentEntry entry : childEntries) {
                    if(!entry.observers.isEmpty())
                        return true;

                    checkedIds.add(
                            entry.downloadJobItem.get().getDownloadSetItem().getDsiContentEntryUid());
                }



            }while(!childEntries.isEmpty());

            return false;
        }

    }

    public DownloadJobItemTracker(UmAppDatabase db) {
        this.db = db;
    }


    public void observeDownloadJobItem(long contentEntryUid, UmObserver<DownloadJobItemWithDownloadSetItem> observer) {
        //Load the given downloadjobitem, then load each parent item.
        executor.execute(() -> loadTrackedEntry(contentEntryUid, observer));
    }

    /**
     * Load an entry into the tracking process
     *
     * @param contentEntryUidToObserve
     * @param observer
     */
    private void loadTrackedEntry(long contentEntryUidToObserve, UmObserver<DownloadJobItemWithDownloadSetItem> observer) {
        //TODO: when this is called from insert, try and avoid digging it out of the database again
        DownloadJobItemWithDownloadSetItem downloadJobItem = db.getDownloadJobItemDao()
                .findByContentEntryUid(contentEntryUidToObserve);
        long currentContentEntryUid = contentEntryUidToObserve;
        do {
            TrackedContentEntry trackedEntry;
            if(contentEntryUidToTrackedEntryMap.containsKey(currentContentEntryUid)) {
                trackedEntry = contentEntryUidToTrackedEntryMap.get(currentContentEntryUid);
                if(currentContentEntryUid == contentEntryUidToObserve)
                    trackedEntry.addObserver(observer);
                return;
            }else {
                trackedEntry = new TrackedContentEntry(downloadJobItem, updatedItems);
                contentEntryUidToTrackedEntryMap.put(currentContentEntryUid, trackedEntry);

                //TODO: handle when downloadJobItem is set after we load the trackedentry
                if(downloadJobItem != null)
                    downloadJobItemUidToTrackedEntryMap.put(downloadJobItem.getDjiUid(),
                            trackedEntry);

                if(observer != null && currentContentEntryUid == contentEntryUidToObserve)
                    trackedEntry.addObserver(observer);

                if(downloadJobItem != null &&
                        downloadJobItemUidToTrackedEntryMap.containsKey(downloadJobItem.getDjiParentDjiUid())){
                    trackedEntry.parent = downloadJobItemUidToTrackedEntryMap
                            .get(downloadJobItem.getDjiParentDjiUid());
                }
            }

            downloadJobItem = downloadJobItem != null && downloadJobItem.getDjiParentDjiUid() != 0 ?
                    db.getDownloadJobItemDao().findByUidWithDownloadSetItem(
                            downloadJobItem.getDjiParentDjiUid()) : null;
            currentContentEntryUid = downloadJobItem != null ? downloadJobItem.getDownloadSetItem()
                    .getDsiContentEntryUid() : 0;
        }while(downloadJobItem != null);
    }

    public void removeDownloadJobItemObserver(long downloadJobItemUid, UmObserver<DownloadJobItemWithDownloadSetItem> observer) {
        //post a job to check after 10 seconds or so for any items that have no active observers,
        //commit their state to the database and remove them from memory
    }

    /**
     *
     * @param update
     */
    public void postUpdate(DownloadJobItemWithDownloadSetItem update) {
        /*
         * We rely on calculating the delta of download progress etc. between the object reference
         * we have, and the one we last knew about. For that reason, we need to make a new object
         * for each update.
         */
        final DownloadJobItemWithDownloadSetItem newItem = new DownloadJobItemWithDownloadSetItem(update);
        executor.execute(() -> {
            TrackedContentEntry entry = downloadJobItemUidToTrackedEntryMap.get(update.getDjiUid());
            if(entry == null) {
                throw new IllegalStateException("Cannot update an untracked item");
            }

            DownloadJobItem lastItem = entry.downloadJobItem.get();
            DownloadJobItem deltaItem = new DownloadJobItem();
            deltaItem.setDownloadedSoFar(newItem.getDownloadedSoFar() -
                    (lastItem != null ? lastItem.getDownloadedSoFar() : 0));
            deltaItem.setDownloadLength(newItem.getDownloadLength() -
                    (lastItem != null ? lastItem.getDownloadLength() : 0));

            entry.updateDownloadJob(newItem);
            TrackedContentEntry entryToUpdate = entry;

            while((entryToUpdate = entryToUpdate.parent) != null){
                entryToUpdate.updateFromDelta(deltaItem);
                entryToUpdate = entryToUpdate.parent;
            }
        });
    }

    public void insertNewDownloadJobItem(DownloadJobItemWithDownloadSetItem newItem) {
        /*
         * Initial item is set to a zero length entity, so postUpdate will see the changes.
         * This could be improved (e.g. postupdate could have a new/update mode).
         */
        DownloadJobItemWithDownloadSetItem initialItem = new DownloadJobItemWithDownloadSetItem(
                newItem.getDownloadSetItem(), 0);
        initialItem.setDjiParentDjiUid(newItem.getDjiParentDjiUid());
        initialItem.setDjiUid(db.getDownloadJobItemDao().insert(initialItem));
        newItem.setDjiUid(initialItem.getDjiUid());
        executor.execute(() -> {
            loadTrackedEntry(newItem.getDownloadSetItem().getDsiContentEntryUid(), null);
            postUpdate(newItem);
        });
    }

    public void commit() {
        Set<DownloadJobItemWithDownloadSetItem> itemsToCommit = updatedItems.getAndSet(new HashSet<>());
        List<DownloadJobItem> itemsToCommitList = new ArrayList<>(itemsToCommit.size());
        itemsToCommitList.addAll(itemsToCommit);
        db.getDownloadJobItemDao().updateList(itemsToCommitList);
    }
}
