package com.ustadmobile.test.core;

import com.ustadmobile.core.impl.UmCallback;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created by mike on 1/14/18.
 */

public class TestCaseCallbackHelper<T> implements UmCallback<T> {

    private TestCase testCase;

    private T result;

    private Throwable exception;

    private ArrayList<Runnable> runList = new ArrayList<>();

    private ArrayList<Integer> waitList = new ArrayList<>();

    private ArrayList<Boolean> completedList = new ArrayList<>();

    public TestCaseCallbackHelper(TestCase testCase) {
        this.testCase = testCase;
    }

    public TestCaseCallbackHelper add(int timeLimit, Runnable r) {
        runList.add(r);
        waitList.add(timeLimit);
        return this;
    }


    public void run(int runIndex) {
        runList.get(runIndex).run();
        synchronized (this) {
            if(result == null && exception == null && runIndex < (runList.size() - 1)) {
                try { wait(waitList.get(runIndex));}
                catch(InterruptedException e) {}
            }
        }

        //handle timeout here

        runIndex++;

        if(runIndex < runList.size())
            run(runIndex);

    }


    public synchronized T getResult() {
        return result;
    }

    public synchronized void clear() {
        this.result = null;
        this.exception = null;
    }

    @Override
    public synchronized void onSuccess(T result) {
        this.result = result;
        notifyAll();
    }

    @Override
    public synchronized void onFailure(Throwable exception) {
        this.exception = exception;
        notifyAll();
    }

    public void start() {
        run(0);
    }

}
