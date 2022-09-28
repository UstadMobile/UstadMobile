package com.ustadmobile.core.schedule

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class ClazzLogScheduleJob : Job {

    override fun execute(context: JobExecutionContext?) {
        val di = context?.scheduler?.context?.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap

        val fromTime = jobDataMap.getLong(ClazzLogCreatorManager.INPUT_FROMTIME)
        val toTime = jobDataMap.getLong(ClazzLogCreatorManager.INPUT_TOTIME)
        val endpointUrl = jobDataMap.getString(ClazzLogCreatorManager.INPUT_ENDPOINTURL)
        val clazzUid = jobDataMap.getLong(ClazzLogCreatorManager.INPUT_CLAZZUID)

        val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = DoorTag.TAG_DB)
        db.createClazzLogs(fromTime, toTime, clazzUid)
    }
}