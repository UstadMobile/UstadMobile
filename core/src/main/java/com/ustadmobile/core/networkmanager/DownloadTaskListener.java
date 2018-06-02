package com.ustadmobile.core.networkmanager;

/**
 * Interface to handle when a DownloadTask status is changed (e.g. it has finished, failed, etc)
 */
public interface DownloadTaskListener {

    /**
     * Called by the DownloadTask when the status has changed (eg. the download has finished,
     * failed, etc)
     *
     * @param task The task being worked on. This is always a DownloadTask
     * @param status The status flag that the status has changed to as per NetworkTask.STATUS_ flags
     */
    void handleDownloadTaskStatusChanged(NetworkTask task, int status);
}
