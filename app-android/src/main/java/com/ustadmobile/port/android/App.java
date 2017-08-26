package com.ustadmobile.port.android;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.ustadmobile.port.android.job.UMSyncJob;
import com.ustadmobile.port.android.job.UMSyncJobCreator;

import java.util.concurrent.TimeUnit;

/**
 * Created by varuna on 8/23/2017.
 */

public class App extends Application {

    private int syncJobId;

    private JobManager mJobManager;

    @Override
    public void onCreate() {
        System.out.println("UMSyncJob: App.java..");
        super.onCreate();
        JobManager.create(this).addJobCreator(new UMSyncJobCreator());
        if(mJobManager == null){
            mJobManager = JobManager.instance();
        }

        if(syncJobId > -1 && mJobManager.getJobRequest(syncJobId) == null){
            System.out.println("UMSyncJob: Job not running. Scheduling it ..");
            scheduleJob();
        }else{
            System.out.println("UMSyncJob: Job: " + syncJobId + " is already running. Not scheduling again.");
        }
    }

    public void scheduleJob(){
        //Running job . We can check if this job id is running or closed.
        //Not checking network
        //Not checking device idle
        //Not checking device charging
        //Nothing to force requirement check (as above)

        //mJobManager = JobManager.instance();
        syncJobId = new JobRequest.Builder(UMSyncJob.TAG)
                //15 min is min and flex min is 5 min
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                //Will make sure it gets run on boot
                .setPersisted(true)
                .build()
                .schedule();
        System.out.println("UMSyncJob: Job scheduled. ID: " + syncJobId);
    }
}

