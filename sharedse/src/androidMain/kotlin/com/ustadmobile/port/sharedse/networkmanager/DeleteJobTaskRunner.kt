package com.ustadmobile.port.sharedse.networkmanager

/**
 * Abstract class which used to implement platform specific job delete task
 *
 * @author kileha3
 */
abstract class DeleteJobTaskRunner : Runnable {

    protected lateinit var args: Map<String, String>

    protected var context: Any?= null

    /**
     * Constructor for testing purpose
     */
    constructor() {}

    /**
     * Constructor used when creating new instance of a task runner
     * @param context Platform application context
     * @param args arguments to be passed.
     */
    constructor(context: Any?, args: Map<String, String>) {
        this.args = args
        this.context = context
    }

    companion object {

        const val ARG_DOWNLOAD_JOB_UID = "downoad_job_uid"
    }
}
