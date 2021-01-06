package com.ustadmobile.core.util.ext

import com.nhaarman.mockitokotlin2.mock
import com.soywiz.klock.DateTime
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

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
        }

        val di = DI {
            import(ustadTestRule.diModule)
            bind<UstadAccountManager>(overrides = true) with singleton { accountManager }
        }
        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        runBlocking {
            repo.insertPersonAndGroup(Person().apply{
                personUid = loggedPersonUid
                admin = true
                firstNames = "Bob"
                lastName = "Jones"
            })
            repo.insertTestStatements()
        }
    }


    @Test
    fun givenStatementsShowingUsagePerMonth_whenQueryNumberOfSessionsDataSet_withXaisMonth_thenShowExpectedValues(){

        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 3, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesDataSet = ReportSeries.NUMBER_SESSIONS
            })
        }

        val queryList = report.generateSql(loggedPersonUid)
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                Assert.assertEquals("number of sessions for month 04", 1f, reportList[0].yAxis)
                Assert.assertEquals("number of sessions for month 05", 2f, reportList[1].yAxis)
                Assert.assertEquals("number of sessions for month 06", 13f, reportList[2].yAxis)
            }
        }
    }

    @Test
    fun givenStatementShowingNumberOfStudentsCompletedByGenderInMonth_whenDataSetIsNumberOfStudentsCompleted_withAxisMonth_andSubGroupByGender_thenShowExpectedValues(){

        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 3, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesDataSet = ReportSeries.NUMBER_STUDENTS_COMPLETED
                reportSeriesSubGroup = Report.GENDER
            })
        }

        val queryList = report.generateSql(loggedPersonUid)
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                val groupByXAxis = reportList.groupBy { it.xAxis }
                groupByXAxis.forEach {
                    when(it.key){
                        "04 2019" -> {
                            Assert.assertEquals("number of students for month 04 with gender female",
                                    0, it.value.find { it.subgroup == "1" }?.yAxis ?: 0)
                            Assert.assertEquals("number of students for month 04 with gender male",
                                    1f, it.value.find { it.subgroup == "2" }?.yAxis ?: 0)
                        }

                        "05 2019" -> {
                            Assert.assertEquals("number of students for month 05 with gender female",
                                    1f, it.value.find { it.subgroup == "1" }?.yAxis ?: 0)
                            Assert.assertEquals("number of students for month 05 with gender male",
                                    1f, it.value.find { it.subgroup == "2" }?.yAxis ?: 0)
                        }
                        "06 2019" -> {
                            Assert.assertEquals("number of students for month 06 with gender female",
                                    0, it.value.find { it.subgroup == "1" }?.yAxis ?: 0)
                            Assert.assertEquals("number of students for month 06 with gender male",
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
                reportSeriesDataSet = ReportSeries.ACTIVITIES_RECORDED
                reportSeriesSubGroup = Report.CLASS
            })
        }

        val queryList = report.generateSql(loggedPersonUid)
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                val numberOfActivitiesRecordedForClass = reportList.find {
                    it.xAxis == "11 06 2019" && it.subgroup == "200" }
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
                reportSeriesDataSet = ReportSeries.AVERAGE_DURATION
                reportSeriesSubGroup = Report.CONTENT_ENTRY
            })
        }

        val queryList = report.generateSql(loggedPersonUid)
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                val avgDurationForWeek09JuneByContent = reportList.find {
                    it.xAxis == "09 06 2019" && it.subgroup == "23223" }
                Assert.assertEquals("data matches", 212307.69f, avgDurationForWeek09JuneByContent!!.yAxis)
            }
        }

    }

    @Test
    fun givenStatementShowingPercentStudentsCompletedByGenderAbove18YearsOfAge_whenDataSetIsPercentStudentCompleted_whenXaxisIsGender_andFilterWithAgeGreaterThan18_thenShowExpectedValues(){
        val report = ReportWithSeriesWithFilters().apply {
            xAxis = Report.GENDER
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesDataSet = ReportSeries.PERCENT_STUDENTS_COMPLETED
                reportSeriesFilters = listOf(ReportFilter().apply {
                    reportFilterField = ReportFilter.FIELD_PERSON_AGE
                    reportFilterCondition = ReportFilter.CONDITION_GREATER_THAN
                    reportFilterValue = "18"
                })
            })
        }

        val queryList = report.generateSql(loggedPersonUid)
        runBlocking {
            queryList.entries.forEach {
                val reportList = db.statementDao.getResults(it.value.sqlStr, it.value.queryParams)
                Assert.assertEquals("data matches", 7.6923075f, reportList[0].yAxis)
            }
        }
    }

}