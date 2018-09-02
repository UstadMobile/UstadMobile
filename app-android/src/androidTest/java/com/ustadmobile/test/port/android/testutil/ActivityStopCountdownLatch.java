package com.ustadmobile.test.port.android.testutil;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This can be used to watch for an activity to stop. This has proven to be needed for using
 * Espresso intended, which has no timeout option and is apparently prone to race conditions.
 *
 * Use as follows:
 *
 * ActivityStopCountdownLatch myLatch = new ActivityStopCountdownLatch(activity);
 *
 *   //navigate to another fragment, or do something that would cause the activity to stop
 *
 * myLatch.wait(10, TimeUnit.SECONDS);
 *
 *   //run assertions that depend on the activity having been stopped
 *
 */
public class ActivityStopCountdownLatch implements Application.ActivityLifecycleCallbacks{

    private final CountDownLatch latch = new CountDownLatch(1);

    private Activity activity;

    /**
     * Create a new latch
     * @param activity Activity to wait for onActivityStopped on. Will match by the activity's class.
     */
    public ActivityStopCountdownLatch(Activity activity) {
        this.activity = activity;
        activity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if(activity.getClass().equals(this.activity.getClass()) && latch.getCount() > 0){
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
            latch.countDown();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    /**
     * Wait for the activity in question to stop
     *
     * @see CountDownLatch#await(long, TimeUnit)
     * @param timeout Timeout as per CountDownLatch.await
     * @param unit Timeout unit as per CountdownLatch.await
     * @return true if the activity has stopped, false otherwise
     */
    public boolean wait(long timeout, @NonNull TimeUnit unit)  {
        try {
            return latch.await(timeout, unit);
        }catch(InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
