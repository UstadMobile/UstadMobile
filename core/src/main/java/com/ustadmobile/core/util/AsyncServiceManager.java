package com.ustadmobile.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class to help manage a service that starts and stops asynchronously.
 *
 */
public abstract class AsyncServiceManager {

    public static final int STATE_STOPPED = 0;

    public static final int STATE_STARTING = 1;

    public static final int STATE_STARTED = 2;

    public static final int STATE_STOPPING = 3;

    private volatile int targetState;

    private volatile int currentState;

    private DelayedExecutor delayedExecutor;

    private final ReentrantLock lock = new ReentrantLock();

    private Vector<OnStateChangeListener> stateChangeListeners = new Vector<>();

    public interface DelayedExecutor {

        void runAfterDelay(Runnable runnable, long delay);

    }

    public interface OnStateChangeListener {
        void onStateChanged(AsyncServiceManager serviceManager, int newState);
    }

    public interface AsyncAwaitChecker {
        boolean stopWaiting(int newState);
    }

    public AsyncServiceManager(int initialState, DelayedExecutor delayedExecutor) {
        currentState = initialState;
        targetState = initialState;
        this.delayedExecutor = delayedExecutor;
    }

    //Blank constructor required for mocking for tests
    public AsyncServiceManager() {

    }

    protected void setDelayedExecutor(DelayedExecutor executor) {
        this.delayedExecutor = executor;
    }

    public void setEnabled(boolean enabled) {
        try {
            lock.lock();
            targetState = enabled ? STATE_STARTED : STATE_STOPPED;
            if(targetState == currentState
                    || targetState == STATE_STARTED && currentState == STATE_STARTING
                    || targetState == STATE_STOPPED && currentState == STATE_STOPPING) {
                //nothing to do
                return;
            }

            if(targetState == STATE_STARTED && currentState == STATE_STOPPED) {
                currentState = STATE_STARTING;
                fireStateChangedEvent(currentState);
                delayedExecutor.runAfterDelay(this::start, 0);
            }else if(targetState == STATE_STOPPED && currentState == STATE_STARTED) {
                currentState = STATE_STOPPING;
                fireStateChangedEvent(currentState);
                delayedExecutor.runAfterDelay(this::stop, 0);
            }else {
                delayedExecutor.runAfterDelay(this::checkState, 1000);
            }
        }finally {
            lock.unlock();
        }
    }

    private void checkState(){
        setEnabled(targetState == STATE_STARTED);
    }

    protected void notifyStateChanged(final int state, final int newTargetState) {
        try {
            lock.lock();
            this.currentState = state;

            if(newTargetState != -1)
                targetState = newTargetState;

        }finally {
            lock.unlock();
        }
        fireStateChangedEvent(state);
    }

    protected void notifyStateChanged(final int state) {
        notifyStateChanged(state, -1);
    }

    protected void fireStateChangedEvent(int newState) {
        ArrayList<OnStateChangeListener> listenerList = new ArrayList<>(stateChangeListeners.size());
        listenerList.addAll(stateChangeListeners);
        for(OnStateChangeListener listener : listenerList) {
            listener.onStateChanged(this, newState);
        }
    }

    public int getState() {
        return currentState;
    }

    public void addOnStateChangeListener(OnStateChangeListener listener) {
        stateChangeListeners.add(listener);
    }

    public void removeOnStateChangeListener(OnStateChangeListener listener) {
        stateChangeListeners.remove(listener);
    }

    public void await(AsyncAwaitChecker checker, long timeout, TimeUnit timeoutUnit) {
        if(checker.stopWaiting(currentState))
            return;

        CountDownLatch latch = new CountDownLatch(1);
        OnStateChangeListener listener = (service, newState) -> {
            if(checker.stopWaiting(newState))
                latch.countDown();
        };
        addOnStateChangeListener(listener);
        try { latch.await(timeout, timeoutUnit); }
        catch(InterruptedException e) { /*should not happen*/}
        removeOnStateChangeListener(listener);
    }


    public abstract void start();

    public abstract void stop();



}
