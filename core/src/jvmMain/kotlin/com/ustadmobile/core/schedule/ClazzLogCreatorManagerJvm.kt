package com.ustadmobile.core.schedule

import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_CLAZZUID
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_ENDPOINTURL
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_FROMTIME
import com.ustadmobile.core.schedule.ClazzLogCreatorManager.Companion.INPUT_TOTIME
import com.ustadmobile.core.util.ext.startNowOrAt
import org.kodein.di.*
import org.quartz.JobBuilder.newJob
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey

class ClazzLogCreatorManagerJvm(override val di : DI): ClazzLogCreatorManager, DIAware {

    override fun requestClazzLogCreation(clazzUid: Long, endpointUrl: String, fromTime: Long, toTime: Long) {
        val scheduler: Scheduler = di.direct.instance()

        val job = newJob(ClazzLogScheduleJob::class.java)
            .usingJobData(INPUT_CLAZZUID, clazzUid)
            .usingJobData(INPUT_ENDPOINTURL, endpointUrl)
            .usingJobData(INPUT_FROMTIME, fromTime)
            .usingJobData(INPUT_TOTIME, toTime)
            .build()

        //Add an id
        val triggerKey = TriggerKey("genclazzlog-$endpointUrl-$clazzUid")

        //unschedule any existing instance of the trigger
        scheduler.unscheduleJob(triggerKey)

        val jobTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .startNowOrAt(fromTime)
            .build()

        scheduler.scheduleJob(job, jobTrigger)
    }
}