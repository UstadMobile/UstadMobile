package com.ustadmobile.lib.rest

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
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
import org.kodein.di.instance
import org.kodein.di.on
import org.quartz.Job
import org.quartz.JobExecutionContext

class StatsIndicatorJob : Job {

    override fun execute(context: JobExecutionContext) {

        val di = context.scheduler.context.get("di") as DI
        val jobDataMap = context.jobDetail.jobDataMap
        val endpoint = jobDataMap.getString(INDICATOR_ENDPOINT)
        val statsEndpoint = jobDataMap.getString(INDICATOR_STATS_ENDPOINT)

        val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

        val now = DateTime.now()
        val monthOffset = now - 30.days
        val dayOffset = now - 1.days

        // REGISTERED_USERS_INDICATOR
        val totalUserCount = db.personDao.getTotalActiveCountUsers()
        val genderUserCount = db.personDao.getActiveCountUsersByGender()
        val countryUserCount = db.personDao.getActiveCountUsersByCountry()

        // ACTIVE_USERS_INDICATOR
        val totalUsedContentCount = db.statementDao.getActiveUsersUsedContent(
                monthOffset.unixMillisLong, now.unixMillisLong)
        val genderUsedContentCount = db.statementDao.getActiveUsersUsedContentByGender(
                monthOffset.unixMillisLong, now.unixMillisLong)
        val countryUsedContentCount = db.statementDao.getActiveUsersUsedContentByCountry(
                monthOffset.unixMillisLong, now.unixMillisLong)

        // ACTIVE_USER_DURATION_INDICATOR
        val totalDurationUsed = db.statementDao.getDurationUsageOverPastDay(
                dayOffset.unixMillisLong, now.unixMillisLong)
        val totalDurationUsedByGender = db.statementDao.getDurationUsageOverPastDayByGender(
                dayOffset.unixMillisLong, now.unixMillisLong)
        val totalDurationUsedByCountry = db.statementDao.getDurationUsageOverPastDayByCountry(
                dayOffset.unixMillisLong, now.unixMillisLong)

        val rowReportList = mutableListOf<UstadCentralReportRow>()

        // REGISTERED_USERS_INDICATOR
        rowReportList.add(UstadCentralReportRow().apply {
            indicatorId = UstadCentralReportRow.REGISTERED_USERS_INDICATOR
            disaggregationKey = UstadCentralReportRow.TOTAL_KEY
            disaggregationValue = UstadCentralReportRow.TOTAL_KEY
            value = totalUserCount.toDouble()
            timestamp = systemTimeInMillis()
        })

        genderUserCount.forEach{

            rowReportList.add(UstadCentralReportRow().apply {
                indicatorId = UstadCentralReportRow.REGISTERED_USERS_INDICATOR
                disaggregationKey = UstadCentralReportRow.GENDER_KEY
                disaggregationValue = it.gender
                value = it.count.toDouble()
                timestamp = systemTimeInMillis()
            })

        }

        countryUserCount.forEach{
            rowReportList.add(UstadCentralReportRow().apply {
                indicatorId = UstadCentralReportRow.REGISTERED_USERS_INDICATOR
                disaggregationKey = UstadCentralReportRow.COUNTRY_KEY
                disaggregationValue = it.country.hashCode()
                value = it.count.toDouble()
                timestamp = systemTimeInMillis()
            })
        }

        // ACTIVE_USERS_INDICATOR
        rowReportList.add(UstadCentralReportRow().apply {
            indicatorId = UstadCentralReportRow.ACTIVE_USERS_INDICATOR
            disaggregationKey = UstadCentralReportRow.TOTAL_KEY
            disaggregationValue = UstadCentralReportRow.TOTAL_KEY
            value = totalUsedContentCount.toDouble()
            timestamp = systemTimeInMillis()
        })

        genderUsedContentCount.forEach{

            rowReportList.add(UstadCentralReportRow().apply {
                indicatorId = UstadCentralReportRow.ACTIVE_USERS_INDICATOR
                disaggregationKey = UstadCentralReportRow.GENDER_KEY
                disaggregationValue = it.gender
                value = it.count.toDouble()
                timestamp = systemTimeInMillis()
            })

        }

        countryUsedContentCount.forEach{
            rowReportList.add(UstadCentralReportRow().apply {
                indicatorId = UstadCentralReportRow.ACTIVE_USERS_INDICATOR
                disaggregationKey = UstadCentralReportRow.COUNTRY_KEY
                disaggregationValue = it.country.hashCode()
                value = it.count.toDouble()
                timestamp = systemTimeInMillis()
            })
        }

        // ACTIVE_USER_DURATION_INDICATOR
        rowReportList.add(UstadCentralReportRow().apply {
            indicatorId = UstadCentralReportRow.ACTIVE_USER_DURATION_INDICATOR
            disaggregationKey = UstadCentralReportRow.TOTAL_KEY
            disaggregationValue = UstadCentralReportRow.TOTAL_KEY
            value = totalDurationUsed.toDouble()
            timestamp = systemTimeInMillis()
        })

        totalDurationUsedByGender.forEach{

            rowReportList.add(UstadCentralReportRow().apply {
                indicatorId = UstadCentralReportRow.ACTIVE_USER_DURATION_INDICATOR
                disaggregationKey = UstadCentralReportRow.GENDER_KEY
                disaggregationValue = it.gender
                value = it.duration.toDouble()
                timestamp = systemTimeInMillis()
            })

        }

        totalDurationUsedByCountry.forEach{
            rowReportList.add(UstadCentralReportRow().apply {
                indicatorId = UstadCentralReportRow.ACTIVE_USER_DURATION_INDICATOR
                disaggregationKey = UstadCentralReportRow.COUNTRY_KEY
                disaggregationValue = it.country.hashCode()
                value = it.duration.toDouble()
                timestamp = systemTimeInMillis()
            })
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
