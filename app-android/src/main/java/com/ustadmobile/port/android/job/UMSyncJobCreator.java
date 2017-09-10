package com.ustadmobile.port.android.job;

import android.content.Context;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.Set;

/**'
 * Job creator class for UMSYNC.
 * Created by varuna on 8/24/2017.
 */

public class UMSyncJobCreator implements JobCreator {

    /**
     * Checks if jobs have finished, etc.
     * @param jobs  the jobs
     * @return  boolean if finished or not.
     */
    public boolean haveJobsFinished(Set<Job> jobs){
        for(Job thisJob: jobs){
            if(!thisJob.isFinished()){
                return true;
            }
        }
        return false;
    }

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
                Set<Job> allJobs = JobManager.instance().getAllJobs();
                Set<Job> allUMSyncJobs = JobManager.instance().getAllJobsForTag(UMSyncJob.TAG);
                Set<JobRequest> allJobRequests = JobManager.instance().getAllJobRequests();
                Set<JobRequest> allUMSyncJobRequests =
                        JobManager.instance().getAllJobRequestsForTag(UMSyncJob.TAG);
                System.out.println(" UMSyncJobCreator: Already running: " +
                        allJobs.size() + " jobs");
                System.out.println(" UMSyncJobCreator: Already running: " +
                        allUMSyncJobs.size() + " UMSyncJob jobs");
                System.out.println(" UMSyncJobCreator: Already running: " +
                        allJobRequests.size() + " UMSyncJob REQUESTS");
                System.out.println(" UMSyncJobCreator: Already running: " +
                        allUMSyncJobRequests.size() + " UMSyncJobREQUESTS");

                //if(!haveJobsFinished(allUMSyncJobs) && allUMSyncJobRequests.size() < 2) {
                if(!haveJobsFinished(allUMSyncJobs)) {
                    System.out.println(" UMSyncJobCreator: All UMSyncJobs have finished. Not skipping.");
                    //JobManager.instance().cancelAllForTag(UMSyncJob.TAG);
                    //System.out.println(" UMSyncJobCreator: Cancelled all jobs.");

                    System.out.println(" >>UMSyncJobCreator: Starting new UMSyncJob..");
                    return new UMSyncJob();
                }else {
                    System.out.println(" >>UMSyncJobCreator: Skipping, " +
                            "Sync job already running not finished");
                }
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
            System.out.println("UMSyncJobCreator: addJobCreator()..");
            // manager.addJobCreator(new SyncJobCreator());
            //If you want to call it via Receiver, call it here.
        }
    }
}
