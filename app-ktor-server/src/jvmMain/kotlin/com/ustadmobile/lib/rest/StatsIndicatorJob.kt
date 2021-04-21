package com.ustadmobile.lib.rest

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointSet
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.withUtf8Charset
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.UstadCentralReportRow
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class StatsIndicatorJob : Job {

    override fun execute(context: JobExecutionContext) {

        val di = context.scheduler.context.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap
        val endpointSet = di.direct.instance<EndpointSet>()
        val statsEndpoint = jobDataMap.getString(INDICATOR_STATS_ENDPOINT)

        val now = DateTime.now()
        val monthOffset = now - 30.days
        val dayOffset = now - 1.days

        val rowReportList = mutableListOf<UstadCentralReportRow>()
        val listOfDisaggregation = listOf(UstadCentralReportRow.TOTAL_KEY,
                UstadCentralReportRow.GENDER_KEY, UstadCentralReportRow.COUNTRY_KEY,
                UstadCentralReportRow.CONNECTIVITY_KEY)
        endpointSet.endpointUrls.forEach { endpoint ->

            val db: UmAppDatabase by di.on(Endpoint(endpoint)).instance(tag = DoorTag.TAG_DB)

            listOfDisaggregation.forEach{
                // REGISTERED_USERS_INDICATOR
                rowReportList.addAll(
                        db.personDao.getRegisteredUsers(it,
                                systemTimeInMillis()))

                // ACTIVE_USERS_INDICATOR
                rowReportList.addAll(
                        db.statementDao.getActiveUsers(it, systemTimeInMillis(),
                                monthOffset.unixMillisLong, now.unixMillisLong))

                // ACTIVE_USER_DURATION_INDICATOR
                rowReportList.addAll(
                        db.statementDao.getDurationUsage(it,
                                systemTimeInMillis(), dayOffset.unixMillisLong, now.unixMillisLong))

            }

        }

        val client: HttpClient by di.instance()

        GlobalScope.launch {
            client.post<HttpStatement>(statsEndpoint){
                body = defaultSerializer().write(rowReportList, ContentType.Application.Json.withUtf8Charset())
            }.execute()
        }


    }
}
