package com.ustadmobile.port.sharedse.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class RunnableQueue {

    private Vector<Runnable> queue = new Vector<>();

    private AtomicBoolean ready = new AtomicBoolean(false);

    private ReentrantLock lock = new ReentrantLock();

    public RunnableQueue() {

    }

    public void runWhenReady(Runnable runnable) {
        try {
            lock.lock();
            if(ready.get()){
                runnable.run();
            }else{
                queue.add(runnable);
            }
        }finally {
            lock.unlock();
        }
    }

    public void setReady(boolean ready) {
        List<Runnable> itemsToRun = null;
        try {
            lock.lock();
            if(ready && !this.ready.get()) {
                this.ready.set(ready);
                itemsToRun = new LinkedList<>(queue);
                queue.clear();
            }
        }finally {
            lock.unlock();
        }

        if(itemsToRun != null) {
            for(Runnable r : itemsToRun){
                r.run();
            }
        }
    }
}
