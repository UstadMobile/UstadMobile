package com.ustadmobile.test.core.impl;

import java.util.Timer;

/**
 * Created by mike on 12/25/17.
 */

public class AsyncTestHelper {

    private Object lock = new Object();

    private Object test;

    private long testDeadline = 0;

    boolean testFinished = false;

    private Runnable testRunnable;

    public AsyncTestHelper(Object test) {
        this.test = test;
    }

    public void delayTestFinish(int millis) {
        if(testDeadline == 0)
            testDeadline = System.currentTimeMillis();

        testDeadline += millis;
    }

    public void waitForTest() {
        while(System.currentTimeMillis() < testDeadline){
            synchronized (lock) {
                try {lock.wait(testDeadline - System.currentTimeMillis());}
                catch(InterruptedException e) {}
            }
        }

        if(!testFinished) {
            throw new RuntimeException("AsyncTestHelper: wait timed out and tests not finished!");
        }
    }

    public void finishTest() {
        testFinished = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void runTests() {
        testRunnable.run();
        finishTest();
    }

    public void setTestsRunnable(Runnable runnable) {
        this.testRunnable = runnable;
    }


}
