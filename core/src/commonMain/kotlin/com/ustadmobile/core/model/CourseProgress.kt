package com.ustadmobile.core.model

/**
 * Created by mike on 7/25/17.
 */

class CourseProgress {

    /**
     * This status will be one of the following:
     *
     * STATUS_NOT_STARTED
     * MessageID.passed
     * MessageID.failed
     *
     * @return
     */
    var status: Int = 0

    var score: Float = 0.toFloat()

    var progress: Int = 0

    constructor() {}

    constructor(status: Int, score: Int, progress: Int) {
        this.status = status
        this.score = score.toFloat()
        this.progress = progress
    }

    companion object {

        val STATUS_NOT_STARTED = 0
    }
}
