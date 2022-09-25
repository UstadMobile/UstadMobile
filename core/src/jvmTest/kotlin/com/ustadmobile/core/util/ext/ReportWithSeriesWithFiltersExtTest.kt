package com.ustadmobile.core.util.ext

import org.mockito.kotlin.mock
import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.door.ext.dbType
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.insertTestStatementsForReports
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton
import com.ustadmobile.core.db.dao.getResults
import com.ustadmobile.door.ext.withDoorTransaction
import com.ustadmobile.door.ext.withDoorTransactionAsync

class ReportWithSeriesWithFiltersExtTest {


    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private val context = Any()

    private val loggedPersonUid:Long = 234568

    private val serverUrl = "http://localhost/dummy/"

    private lateinit var accountManager: UstadAccountManager

    @Before
    fun setup() {
        accountManager = mock{
            on{activeAccount}.thenReturn(UmAccount(loggedPersonUid,"","",serverUrl))
            on { activeEndpoint }.thenReturn(Endpoint(serverUrl))
        }

        val di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
        }
        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        runBlocking {
            repo.withDoorTransactionAsync { txRepo ->
                val person = txRepo.insertPersonAndGroup(Person().apply{
                    personUid = loggedPersonUid
                    admin = true
                    firstNames = "Bob"
                    lastName = "Jones"
                })

                txRepo.grantScopedPermission(person, Role.ALL_PERMISSIONS,
                    ScopedGrant.ALL_TABLES, ScopedGrant.ALL_ENTITIES)
            }

            repo.withDoorTransaction { txRepo ->
                txRepo.insertTestStatementsForReports()
            }

        }
    }


    @Test
    fun givenStatementsShowingUsagePerMonth_whenQueryNumberOfSessionsDataSet_withXaisMonth_thenShowExpectedValues(){

        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 3, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesYAxis = ReportSeries.NUMBER_SESSIONS
            })
        }

        val queryList = report.generateSql(loggedPersonUid, db.dbType())
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                Assert.assertEquals("number of sessions for month 04", 0f, reportList[0].yAxis)
                Assert.assertEquals("number of sessions for month 05", 1f, reportList[1].yAxis)
                Assert.assertEquals("number of sessions for month 06", 2f, reportList[2].yAxis)
            }
        }
    }

    @Test
    fun givenStatementShowingNumberOfActiveUsersByGenderInMonth_whenDataSetIsNumberOfActiveUsers_withAxisMonth_andSubGroupByGender_thenShowExpectedValues(){

        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 3, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesYAxis = ReportSeries.NUMBER_ACTIVE_USERS
                reportSeriesSubGroup = Report.GENDER
            })
        }

        val queryList = report.generateSql(loggedPersonUid, db.dbType())
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                val groupByXAxis = reportList.groupBy { it.xAxis }
                groupByXAxis.forEach {
                    when(it.key){
                        "04 2019" -> {
                            Assert.assertEquals("number of active users for month 04 with gender female",
                                    0, it.value.find { it.subgroup == "1" }?.yAxis ?: 0)
                            Assert.assertEquals("number of active users for month 04 with gender male",
                                    1f, it.value.find { it.subgroup == "2" }?.yAxis ?: 0)
                        }

                        "05 2019" -> {
                            Assert.assertEquals("number of active users for month 05 with gender female",
                                    1f, it.value.find { it.subgroup == "1" }?.yAxis ?: 0)
                            Assert.assertEquals("number of active users for month 05 with gender male",
                                    1f, it.value.find { it.subgroup == "2" }?.yAxis ?: 0)
                        }
                        "06 2019" -> {
                            Assert.assertEquals("number of active users for month 06 with gender female",
                                    1f, it.value.find { it.subgroup == "1" }?.yAxis ?: 0)
                            Assert.assertEquals("number of active users for month 06 with gender male",
                                    2f, it.value.find { it.subgroup == "2" }?.yAxis ?: 0)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun giveStatementShowingActivitiesRecordedInDayByClass_whenDataSetIsActivitiesRecorded_withXaxisIsDay_andSubGroupedByClass_thenShowExpectedValues(){
        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.DAY
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesYAxis = ReportSeries.INTERACTIONS_RECORDED
                reportSeriesSubGroup = Report.CLASS
            })
        }

        val queryList = report.generateSql(loggedPersonUid, db.dbType())
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                val numberOfActivitiesRecordedForClass = reportList.find {
                    it.xAxis == "11/06/2019" && it.subgroup == "200" }
                Assert.assertEquals("data matches", 13f, numberOfActivitiesRecordedForClass!!.yAxis)
            }
        }

    }

    @Test
    fun givenStatementShowingAvgDurationInWeekByContent_whenDataSetIsAvgDurationPerSession_whenXaxisIsWeek_andSubGroupByContent_thenShowExpectedValues(){
        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.WEEK
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesYAxis = ReportSeries.AVERAGE_DURATION
                reportSeriesSubGroup = Report.CONTENT_ENTRY
            })
        }

        val queryList = report.generateSql(loggedPersonUid, db.dbType())
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                val avgDurationForWeek09JuneByContent = reportList.find {
                    it.xAxis == "10/06/2019" && it.subgroup == "23223" }
                Assert.assertEquals("data matches", 212307.0f, avgDurationForWeek09JuneByContent!!.yAxis)
            }
        }

    }

    @Test
    fun givenStatementShowingAverageUsageTimePerUserByGenderAbove18YearsOfAge_whenDataSetIsAverageUsageTimePerUser_whenXaxisIsGender_andFilterWithAgeGreaterThan18_thenShowExpectedValues(){
        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.GENDER
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesYAxis = ReportSeries.AVERAGE_USAGE_TIME_PER_USER
                reportSeriesFilters = listOf(ReportFilter().apply {
                    reportFilterField = ReportFilter.FIELD_PERSON_AGE
                    reportFilterCondition = ReportFilter.CONDITION_GREATER_THAN
                    reportFilterValue = "18"
                })
            })
        }

        val queryList = report.generateSql(loggedPersonUid, db.dbType())
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                Assert.assertEquals("data matches", 960000f, reportList[0].yAxis)
            }
        }
    }

}