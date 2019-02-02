package com.ustadmobile.port.sharedse.util;

import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmObserver;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    /**
     * The adapter must convert the item type into a WorkQueueItemHolder
     *
     * @param <T> The type of item being run from the queue
     */
    public interface WorkQueueItemAdapter<T> {

        WorkQueueItemHolder<T> getItem(T item);

    }

    /**
     * The work queue item holder provides a unique id for each item to be run, and, when required,
     * must provide a runnable for the given item
     *
     * @param <T> The type of item being run from the queue
     */
    public interface WorkQueueItemHolder<T> {

        long getUid();

        Runnable makeRunnable();

    }



    private static class RunWrapper<T> implements Runnable{

        private LiveDataWorkQueue<T> workQueue;

        private WorkQueueItemHolder<T> item;

        private RunWrapper(WorkQueueItemHolder<T> item, LiveDataWorkQueue<T> workQueue) {
            this.item = item;
            this.workQueue  = workQueue;
        }

        @Override
        public void run() {
            item.makeRunnable().run();
            workQueue.removeItemFromActiveItems(item.getUid());
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

            for(T sourceItem : sourceData) {
                WorkQueueItemHolder<T> itemHolder = adapter.getItem(sourceItem);
                if(activeItems.size() < maxThreads && !activeItems.containsKey(itemHolder.getUid())) {
                    RunWrapper<T> wrapper = new RunWrapper<>(itemHolder, this);
                    activeItems.put(itemHolder.getUid(), wrapper);
                    executor.submit(wrapper);
                }
            }
        }finally {
            lock.unlock();
        }
    }

    private void removeItemFromActiveItems(long itemUid) {
        try {
            lock.lock();
            activeItems.remove(itemUid);
        }finally {
            lock.unlock();
        }
    }

}
