package com.ustadmobile.core.networkmanager;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.lib.db.entities.DownloadJobItem;
import com.ustadmobile.lib.db.entities.DownloadJobItemWithDownloadSetItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

    static class TrackedContentEntry {

        private AtomicReference<DownloadJobItemWithDownloadSetItem> downloadJobItem =
                new AtomicReference<>();

        private TrackedContentEntry parent;

        private List<UmObserver<DownloadJobItemWithDownloadSetItem>> observers = new ArrayList<>();

        TrackedContentEntry(DownloadJobItemWithDownloadSetItem downloadJobItem) {
            this.downloadJobItem.set(downloadJobItem);
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
                trackedEntry = new TrackedContentEntry(downloadJobItem);
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

    public void postUpdate(DownloadJobItemWithDownloadSetItem update) {
        executor.execute(() -> {
            TrackedContentEntry entry = downloadJobItemUidToTrackedEntryMap.get(update.getDjiUid());
            if(entry == null) {
                throw new IllegalStateException("Cannot update an untracked item");
            }

            DownloadJobItem lastItem = entry.downloadJobItem.get();
            long deltaDownloaded = update.getDownloadedSoFar() -
                    (lastItem != null ? lastItem.getDownloadedSoFar() : 0);
            long deltaDownloadLength = update.getDownloadLength() -
                    (lastItem != null ? lastItem.getDownloadLength() : 0);

            entry.downloadJobItem.set(update);
            entry.fireOnChanged();

            TrackedContentEntry entryToUpdate = entry;
            while((entryToUpdate = entryToUpdate.parent) != null){
                DownloadJobItem downloadJobItem = entryToUpdate.downloadJobItem.get();
                if(downloadJobItem != null) {
                    downloadJobItem.setDownloadedSoFar(
                            downloadJobItem.getDownloadedSoFar() + deltaDownloaded);
                    downloadJobItem.setDownloadLength(
                            downloadJobItem.getDownloadLength() + deltaDownloadLength);
                    entryToUpdate.fireOnChanged();
                }

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
}
