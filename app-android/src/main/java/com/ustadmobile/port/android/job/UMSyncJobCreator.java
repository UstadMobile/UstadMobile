package com.ustadmobile.port.android.job;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;

/**'
 * Job creator class for UMSYNC.
 * Created by varuna on 8/24/2017.
 */

public class UMSyncJobCreator implements JobCreator {

    /**
     * creates the job
     * @param tag   The Job TAG
     * @return Job
     */
    @Override
    public Job create(String tag) {
        System.out.println("UMSyncJobCreator: onCreate()..");

        switch (tag) {
            case UMSyncJob.TAG:
                System.out.println("UMSyncJobCreator  : Starting sync via Evernote's android-job..");
                return new UMSyncJob();
            default:
                return null;
        }
    }

    /**
     * Receiver if you want to call it without an Activity. Isn't used but might be useful later.
     */
    public static final class AddReceiver extends AddJobCreatorReceiver {
        @Override
        protected void addJobCreator(@NonNull Context context, @NonNull JobManager manager) {
            // manager.addJobCreator(new SyncJobCreator());
            //If you want to call it via Receiver, call it here.
        }
    }
}
