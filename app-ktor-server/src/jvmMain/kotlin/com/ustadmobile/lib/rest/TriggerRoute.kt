package com.ustadmobile.lib.rest

import io.ktor.routing.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder

fun Route.TriggerRoute(statsEndpoint: String){

    post("trigger-stats"){

        if(statsEndpoint != CONF_STATS_SERVER){

            val scheduler = di().direct.instance<Scheduler>()
            val job = JobBuilder.newJob(StatsIndicatorJob::class.java)
                    .usingJobData(INDICATOR_STATS_ENDPOINT, statsEndpoint)
                    .build()

            val jobTrigger = TriggerBuilder.newTrigger()
                    .startNow()
                    .build()

            scheduler.scheduleJob(job, jobTrigger)
        }

    }

}