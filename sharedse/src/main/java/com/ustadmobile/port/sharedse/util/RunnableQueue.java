package com.ustadmobile.port.sharedse.util;

import java.util.Iterator;
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
        if(ready.get()){
            runnable.run();
        }else{
            try {
                lock.lock();
                queue.add(runnable);
            }finally {
                lock.unlock();
            }
        }
    }

    public void setReady(boolean ready) {
        try {
            lock.lock();
            if(ready && !this.ready.get()) {
                Iterator<Runnable> iterator = queue.iterator();
                while(iterator.hasNext()) {
                    Runnable r= iterator.next();
                    r.run();
                    iterator.remove();
                }
            }

            this.ready.set(ready);
        }finally {
            lock.unlock();
        }
    }
}
