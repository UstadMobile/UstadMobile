package com.ustadmobile.core.schedule

interface ClazzLogCreatorManager {

    /**
     * Request running the ClazzLogCreator. On Android this will use WorkManager to run at the
     * specified time. On JVM this will use Quartz.
     *
     * @param endpointUrl the site endpoint url
     * @param clazzUid the clazzUid for the Clazz for which ClazzLogs will be created
     * @param fromTime the start time of the range for which clazzlogs will be created (inclusive).
     * This will be the time that the task will start running.
     * @param endTime the end time of the range for which clazzlogs will be created (inclusive)
     */
    fun requestClazzLogCreation(clazzUid: Long, endpointUrl: String, fromTime: Long, toTime: Long)

}