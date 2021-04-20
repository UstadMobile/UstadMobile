package com.ustadmobile.lib.rest

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.ustadmobile.core.account.EndpointSet
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.UstadCentralReportRow
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
        endpointSet.endpointUrls.forEach { endpoint ->

            val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

            // REGISTERED_USERS_INDICATOR
            rowReportList.addAll(
                    db.personDao.getRegisteredUsers(UstadCentralReportRow.TOTAL_KEY,
                    systemTimeInMillis()))
            rowReportList.addAll(
                    db.personDao.getRegisteredUsers(UstadCentralReportRow.GENDER_KEY,
                    systemTimeInMillis()))
            rowReportList.addAll(
                    db.personDao.getRegisteredUsers(UstadCentralReportRow.COUNTRY_KEY,
                    systemTimeInMillis()))
            rowReportList.addAll(
                    db.personDao.getRegisteredUsers(UstadCentralReportRow.CONNECTIVITY_KEY,
                    systemTimeInMillis()))


            // ACTIVE_USERS_INDICATOR
            rowReportList.addAll(
            db.statementDao.getActiveUsers(UstadCentralReportRow.TOTAL_KEY, systemTimeInMillis(),
                    monthOffset.unixMillisLong, now.unixMillisLong))
            rowReportList.addAll(
            db.statementDao.getActiveUsers(UstadCentralReportRow.GENDER_KEY, systemTimeInMillis(),
                    monthOffset.unixMillisLong, now.unixMillisLong))
            rowReportList.addAll(
            db.statementDao.getActiveUsers(UstadCentralReportRow.COUNTRY_KEY, systemTimeInMillis(),
                    monthOffset.unixMillisLong, now.unixMillisLong))
            rowReportList.addAll(
            db.statementDao.getActiveUsers(UstadCentralReportRow.CONNECTIVITY_KEY, systemTimeInMillis(),
                    monthOffset.unixMillisLong, now.unixMillisLong))

            // ACTIVE_USER_DURATION_INDICATOR
            rowReportList.addAll(
            db.statementDao.getDurationUsage(UstadCentralReportRow.TOTAL_KEY,
                    systemTimeInMillis(), dayOffset.unixMillisLong, now.unixMillisLong))
            rowReportList.addAll(
            db.statementDao.getDurationUsage(UstadCentralReportRow.GENDER_KEY,
                    systemTimeInMillis(), dayOffset.unixMillisLong, now.unixMillisLong))
            rowReportList.addAll(
            db.statementDao.getDurationUsage(UstadCentralReportRow.COUNTRY_KEY,
                    systemTimeInMillis(), dayOffset.unixMillisLong, now.unixMillisLong))
            rowReportList.addAll(
            db.statementDao.getDurationUsage(UstadCentralReportRow.CONNECTIVITY_KEY,
                    systemTimeInMillis(), dayOffset.unixMillisLong, now.unixMillisLong))

        }

        val client: HttpClient by di.instance()

        GlobalScope.launch {
            client.post<HttpStatement>(statsEndpoint){
                header("content-type", "application/json")
                body = listOf(rowReportList)
            }
        }


    }
}
