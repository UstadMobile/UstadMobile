package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.networkmanager.initPicasso
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.port.android.screen.ReportDetailScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.UstadSingleEntityFragmentIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.*
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@AdbScreenRecord("Report Detail Screen Test")
@RunWith(Parameterized::class)
class ReportDetailFragmentTest(val report: Report) : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {
        runBlocking {
            dbRule.db.insertTestStatements()
        }
    }


    @AdbScreenRecord("show report on detail")
    @Test
    fun givenReportExists_whenLaunched_thenShouldShowReport() {


        init{
            val reportUid = dbRule.db.reportDao.insert(report)
            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(ARG_ENTITY_UID to reportUid)) {
                ReportDetailFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }
        }.run {

            ReportDetailScreen{
                reportList{
                    isDisplayed()
                }
            }

        }

    }

    companion object {


        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Report> {
            return listOf(ReportWithFilters().apply {
                chartType = Report.BAR_CHART
                yAxis = Report.AVG_DURATION
                xAxis = Report.MONTH
                fromDate = DateTime(2019, 4, 10).unixMillisLong
                toDate = DateTime(2019, 6, 11).unixMillisLong
            },
                    ReportWithFilters().apply {
                        chartType = Report.LINE_GRAPH
                        yAxis = Report.AVG_DURATION
                        xAxis = Report.MONTH
                        fromDate = DateTime(2019, 4, 10).unixMillisLong
                        toDate = DateTime(2019, 6, 11).unixMillisLong
                    }, ReportWithFilters().apply {
                chartType = Report.BAR_CHART
                yAxis = Report.SCORE
                xAxis = Report.MONTH
                subGroup = Report.GENDER
                fromDate = DateTime(2019, 4, 10).unixMillisLong
                toDate = DateTime(2019, 6, 11).unixMillisLong
            }, ReportWithFilters().apply {
                chartType = Report.LINE_GRAPH
                yAxis = Report.SCORE
                xAxis = Report.MONTH
                subGroup = Report.CONTENT_ENTRY
                fromDate = DateTime(2019, 4, 10).unixMillisLong
                toDate = DateTime(2019, 6, 11).unixMillisLong
            })
        }


    }

}