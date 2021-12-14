package com.ustadmobile.core.db

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
object JobStatus {

    const val NOT_QUEUED = 0

    //Waiting type statuses - 1-10

    const val NEEDS_PREPARED = 1

    const val PREPARING = 2

    const val PAUSED = 3

    /**
     * The minimum value of waiting type statuses e.g. queued, waiting for connection, etc. (inclusive)
     */
    const val WAITING_MIN = 4

    /**
     * The maximum value of waiting type statuses - e.g.
     */
    const val WAITING_MAX = 10

    const val QUEUED = 4

    /**
     * Waiting for a connection. Could be waiting for Wifi / p2p availability if the download job
     * should complete without using mobile data. Could also be waiting for any kind of network if
     * the job does not depend on wifi.
     */
    const val WAITING_FOR_CONNECTION = 5

    const val WAIT_FOR_RETRY = 6

    //Running statuses - 11-20

    const val RUNNING_MIN = 11

    const val RUNNING_MAX = 20
    /**
     * The DownloadTask has been created and is starting. Done to ensure that there is no possibility
     * of two tasks being queued accidently at the same time.
     */
    const val STARTING = 11

    const val RUNNING = 12

    const val CANCELLING = 13

    const val PAUSING = 14

    const val STOPPING = 15

    //Complete statuses where the job is not part of the queue - 21-30

    const val COMPLETE_MIN = 21

    const val COMPLETE_MAX = 30

    const val PARTIAL_FAILED = 23

    const val COMPLETE = 24

    const val FAILED = 25

    const val STOPPED = 27

    const val CANCELED = 28

    @Deprecated("Status should be completed after deleting job")
    const val DELETED = 29

    fun statusToString(status: Int): String {
        when (status) {
            NOT_QUEUED -> return "NOT_QUEUED"
            PAUSED -> return "PAUSED"
            QUEUED -> return "QUEUED"
            RUNNING -> return "RUNNING"
            COMPLETE -> return "COMPLETE"
            FAILED -> return "FAILED"
            DELETED -> return "DELETED"
        }

        return "" + status
    }

}
