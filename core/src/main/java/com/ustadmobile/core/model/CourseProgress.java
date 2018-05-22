package com.ustadmobile.core.model;

/**
 * Created by mike on 7/25/17.
 */

public class CourseProgress {

    public static final int STATUS_NOT_STARTED = 0;

    private int status;

    private float score;

    private int progress;

    public CourseProgress() {

    }

    public CourseProgress(int status, int score, int progress) {
        this.status = status;
        this.score = score;
        this.progress = progress;
    }

    /**
     * This status will be one of the following:
     *
     * STATUS_NOT_STARTED
     * MessageID.passed
     * MessageID.failed
     *
     * @return
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
