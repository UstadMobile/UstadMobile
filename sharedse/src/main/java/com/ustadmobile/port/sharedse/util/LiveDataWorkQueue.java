package com.ustadmobile.port.sharedse.util;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LiveDataWorkQueue can be used to run a queue of work items using a simple live data query
 * and an adapter.
 *
 * @param <T> The type of item to be run from the queue.
 */
public class LiveDataWorkQueue<T> {

    private int maxThreads;

    private final Map<Long, Runnable> activeItems = new Hashtable<>();

    private final ReentrantLock lock = new ReentrantLock();

    private ExecutorService executor;

    private UmLiveData<List<T>> workSource;

    private UmObserver<List<T>> workObserver;

    private WorkQueueItemAdapter<T> adapter;

    private AtomicReference<List<T>> currentQueue;

    private Set<Long> completedItems;

    private OnQueueEmptyListener queueEmptyListener;

    /**
     * The adapter must convert the item type into a WorkQueueItemHolder
     *
     * @param <T> The type of item being run from the queue
     */
    public interface WorkQueueItemAdapter<T> {

        Runnable makeRunnable(T item);

        long getUid(T item);

    }

    public interface OnQueueEmptyListener {

        void onQueueEmpty();

    }


    private class RunWrapper<T> implements Runnable{

        private T item;

        WorkQueueItemAdapter<T> adapter;

        private RunWrapper(T item, WorkQueueItemAdapter<T> adapter) {
            this.item = item;
            this.adapter = adapter;
        }

        @Override
        public void run() {
            adapter.makeRunnable(item).run();
            LiveDataWorkQueue.this.handleItemFinished(adapter.getUid(item));
        }
    }

    public WorkQueueItemAdapter<T> getAdapter() {
        return adapter;
    }

    public void setAdapter(WorkQueueItemAdapter<T> adapter) {
        this.adapter = adapter;
    }

    /**
     * Create a new live data work queue
     *
     * @param maxThreads maximum number of concurrent threads
     */
    public LiveDataWorkQueue(int maxThreads) {
        this.maxThreads = maxThreads;
        executor = Executors.newFixedThreadPool(maxThreads);
        currentQueue = new AtomicReference<>();
        completedItems = new HashSet<>();
    }


    /**
     * Start observing the livedata source for items to execute
     *
     * @param workSource UmLiveData that will provide items to be adapted and executed
     */
    public void start(UmLiveData<List<T>> workSource) {
        this.workSource = workSource;
        workObserver = this::handleWorkSourceChanged;
        workSource.observeForever(workObserver);
    }

    /**
     * Shutdown the executor and stop observing
     */
    public void shutdown() {
        executor.shutdown();
        workSource.removeObserver(workObserver);
    }

    private void handleWorkSourceChanged(List<T> sourceData) {
        try {
            lock.lock();
            currentQueue.set(sourceData);
            checkQueue();
        }finally {
            lock.unlock();
        }

    }

    private void checkQueue() {
        List<T> itemsToCheck = currentQueue.get();
        if(itemsToCheck == null)
            return;

        try {
            lock.lock();

            for(T sourceItem : itemsToCheck) {
                long uid = adapter.getUid(sourceItem);
                if(activeItems.size() < maxThreads && !activeItems.containsKey(uid)
                        && !completedItems.contains(uid)) {
                    RunWrapper<T> wrapper = new RunWrapper<>(sourceItem, adapter);
                    activeItems.put(uid, wrapper);
                    executor.submit(wrapper);
                }
            }

            if(activeItems.isEmpty() && queueEmptyListener != null) {
                queueEmptyListener.onQueueEmpty();
            }
        }finally {
            lock.unlock();
        }
    }


    private void handleItemFinished(long itemUid) {
        try {
            lock.lock();
            activeItems.remove(itemUid);
            completedItems.add(itemUid);
            checkQueue();
        }finally {
            lock.unlock();
        }
    }

}
