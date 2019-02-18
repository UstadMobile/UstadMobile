package com.ustadmobile.core.db;

/**
 * Constants representing a network job status
 *
 * Statuses are grouped in ranges:
 * 0:       not queued
 * 1:       paused (by the user)
 * 2-10     waiting (inc. wait for retry)
 * 11-20    active/running
 * 21-30    finished
 */
public class JobStatus {
    
    public static final int NOT_QUEUED = 0;

    //Waiting type statuses - 1-10


    public static final int PAUSED = 1;

    /**
     * The minimum value of waiting type statuses e.g. queued, waiting for connection, etc. (inclusive)
     */
    public static final int WAITING_MIN = 2;

    /**
     * The maximum value of waiting type statuses - e.g.
     */
    public static final int WAITING_MAX = 10;

    public static final int QUEUED = 3;

    /**
     * Waiting for a connection. Could be waiting for Wifi / p2p availability if the download job
     * should complete without using mobile data. Could also be waiting for any kind of network if
     * the job does not depend on wifi.
     */
    public static final int WAITING_FOR_CONNECTION = 4;

    public static final int WAIT_FOR_RETRY = 5;

    //Running statuses - 11-20

    public static final int RUNNING_MIN = 11;

    public static final int RUNNING_MAX = 20;
    /**
     * The DownloadTask has been created and is starting. Done to ensure that there is no possibility
     * of two tasks being queued accidently at the same time.
     */
    public static final int STARTING = 11;

    public static final int RUNNING = 12;

    public static final int CANCELLING = 13;

    public static final int PAUSING = 14;

    public static final int STOPPING = 15;

    //Complete statuses where the job is not part of the queue - 21-30

    public static final int COMPLETE_MIN = 21;

    public static final int COMPLETE_MAX = 30;

    public static final int COMPLETE = 24;

    public static final int FAILED = 25;

    public static final int STOPPED = 27;

    public static final int CANCELED = 28;


    
}
