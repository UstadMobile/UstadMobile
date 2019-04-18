package com.ustadmobile.port.sharedse.util;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple queue manager class that can use an abstract source of Runnables (e.g. one tied to a
 * database query) and then submit them to an executor service.
 */
public class WorkQueue {

    private ExecutorService executor;

    private int maxThreads;

    private final List<Runnable> activeItems;

    private WorkQueueSource source;

    private final List<EmptyWorkQueueListener> emptyWorkQueueListeners;

    public interface WorkQueueSource {

        Runnable nextItem();

    }

    public interface EmptyWorkQueueListener {

        void onQueueEmpty(WorkQueue queue);

    }

    public WorkQueue(WorkQueueSource source, int maxThreads){
        this.source = source;
        this.maxThreads = maxThreads;
        activeItems = new Vector<>(maxThreads);
        emptyWorkQueueListeners = new Vector<>();
    }

    public void start() {
        executor = Executors.newFixedThreadPool(maxThreads);
        checkQueue();
    }

    public void shutdown() {
        executor.shutdown();
    }

    public void checkQueue() {
        Runnable nextItem;
        synchronized (activeItems) {
            while(activeItems.size() < maxThreads && (nextItem = source.nextItem()) != null) {
                final Runnable itemRef = nextItem;
                Runnable runWrapper = () -> {
                    itemRef.run();
                    activeItems.remove(itemRef);
                    checkQueue();
                };
                activeItems.add(itemRef);
                executor.submit(runWrapper);
            }

            if(activeItems.isEmpty()) {
                fireWorkQueueEmptyEvent();
            }
        }

    }

    protected void fireWorkQueueEmptyEvent(){
        for(EmptyWorkQueueListener listener : emptyWorkQueueListeners) {
            listener.onQueueEmpty(this);
        }
    }

    public void addEmptyWorkQueueListener(EmptyWorkQueueListener listener) {
        emptyWorkQueueListeners.add(listener);
    }

    public void removeEmptyWorkQueueListener(EmptyWorkQueueListener listener) {
        emptyWorkQueueListeners.remove(listener);
    }


}
