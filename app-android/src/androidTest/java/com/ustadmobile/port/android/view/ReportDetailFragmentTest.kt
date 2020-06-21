package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.test.port.android.util.UstadSingleEntityFragmentIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@AdbScreenRecord("Report Detail Screen Test")
@RunWith(Parameterized::class)
class ReportDetailFragmentTest(val report: Report){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule()

    lateinit var fragmentIdlingResource: UstadSingleEntityFragmentIdlingResource

    @Before
    fun setup() {
        runBlocking {
            dbRule.db.insertTestStatements()
        }
    }


    @AdbScreenRecord("show report on detail")
    @Test
    fun givenReportExists_whenLaunched_thenShouldShowReport() {
        val reportUid = dbRule.db.reportDao.insert(report)

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(ARG_ENTITY_UID to reportUid)) {
            ReportDetailFragment().also {
                it.installNavController(systemImplNavRule.navController)
                fragmentIdlingResource = UstadSingleEntityFragmentIdlingResource(it)
                IdlingRegistry.getInstance().register(fragmentIdlingResource)
            }
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        onView(withId(R.id.fragment_detail_report_list)).check(matches(isDisplayed()))

        onIdle()

        IdlingRegistry.getInstance().unregister(fragmentIdlingResource)

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
                subGroup = Report.WEEK
                fromDate = DateTime(2019, 4, 10).unixMillisLong
                toDate = DateTime(2019, 6, 11).unixMillisLong
            })
        }


    }

}