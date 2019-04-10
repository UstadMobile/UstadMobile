package com.ustadmobile.core.impl


/**
 * This is a workaround to enable refactoring the database into it's own project.
 */
object NetworkTaskStatus {

    /*
     * Statuses are grouped in ranges:
     * 0:       not queued
     * 1:       paused (by the user)
     * 2-10     waiting (inc. wait for retry)
     * 11-20    active/running
     * 21-30    finished
     */

    val STATUS_PAUSED = 1

    /**
     * The minimum value of waiting type statuses e.g. queued, waiting for connection, etc.
     */
    val STATUS_WAITING_MIN = 2

    /**
     * The maximum value of waiting type statuses - e.g.
     */
    val STATUS_WAITING_MAX = 10

    val STATUS_QUEUED = 3

    /**
     * Waiting for a connection. Could be waiting for Wifi / p2p availability if the download job
     * should complete without using mobile data. Could also be waiting for any kind of network if
     * the job does not depend on wifi.
     */
    val STATUS_WAITING_FOR_CONNECTION = 4

    val STATUS_WAIT_FOR_RETRY = 5

    //Running statuses - 11-20

    val STATUS_RUNNING_MIN = 11

    val STATUS_RUNNING_MAX = 20
    /**
     * The DownloadTask has been created and is starting. Done to ensure that there is no possibility
     * of two tasks being queued accidently at the same time.
     */
    val STATUS_STARTING = 11

    val STATUS_RUNNING = 12


    //Complete statuses where the job is not part of the queue - 21-30

    val STATUS_COMPLETE_MIN = 20

    val STATUS_COMPLETE_MAX = 30

    val STATUS_COMPLETE = 24

    val STATUS_FAILED = 25

    val STATUS_STOPPED = 26

    val STATUS_CANCELED = 27


    @Deprecated("")
    val STATUS_WAITING = 1

    @Deprecated("")
    val STATUS_RETRY_LATER = 23

    @Deprecated("")
    val STATUS_WAITING_FOR_NETWORK = 27

}
