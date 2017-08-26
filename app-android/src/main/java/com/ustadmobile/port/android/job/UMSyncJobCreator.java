package com.ustadmobile.port.android.job;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.nanolrs.android.job.SyncJob;
import com.ustadmobile.nanolrs.core.manager.NodeManager;
import com.ustadmobile.nanolrs.core.manager.UserManager;
import com.ustadmobile.nanolrs.core.model.Node;
import com.ustadmobile.nanolrs.core.model.User;
import com.ustadmobile.nanolrs.core.persistence.PersistenceManager;

/**
 * Created by varuna on 8/24/2017.
 */

public class UMSyncJobCreator implements JobCreator {

    private User loggedInUser;
    private Node endNode;

    @Override
    public Job create(String tag) {
        System.out.println("UMSyncJobCreator: onCreate()..");

        switch (tag) {
            case UMSyncJob.TAG:
                System.out.println("UMSyncJobCreator  : Starting sync via Evernote's android-job..");

                //UMSyncJob umsync_job = new UMSyncJob();
                //umsync_job.setEndNode(endNode);
                //umsync_job.setLoggedInUser(loggedInUser);
                //return umsync_job;
                return new UMSyncJob();
            default:
                return null;
        }
    }

    public static final class AddReceiver extends AddJobCreatorReceiver {
        @Override
        protected void addJobCreator(@NonNull Context context, @NonNull JobManager manager) {
            // manager.addJobCreator(new SyncJobCreator());
        }
    }
}
