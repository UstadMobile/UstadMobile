package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.port.android.screen.ReportDetailScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.*
import com.ustadmobile.util.test.ext.insertTestStatementsForReports
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
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
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {
        runBlocking {
            dbRule.insertPersonAndStartSession(Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
                admin = true
                personUid = 42
            })
            dbRule.repo.insertTestStatementsForReports()
        }
    }


    @AdbScreenRecord("show report on detail")
    @Test
    fun givenReportExists_whenLaunched_thenShouldShowReport() {

        init {
            dbRule.repo.reportDao.insert(report)
            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(ARG_ENTITY_UID to report.reportUid)) {
                ReportDetailFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }
        }.run {

            ReportDetailScreen {
                reportList {
                    isDisplayed()
                }
            }

        }

    }

    companion object {


        @JvmStatic
        @Parameterized.Parameters
        fun data(): Iterable<Report> {
            return listOf(
               ReportWithSeriesWithFilters().apply {
                        reportUid = 3
                        xAxis = Report.DAY
                        fromDate = DateTime(2019, 3, 10).unixMillisLong
                        toDate = DateTime(2019, 6, 11).unixMillisLong
                        reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                            reportSeriesYAxis = ReportSeries.TOTAL_DURATION
                            reportSeriesVisualType = ReportSeries.BAR_CHART
                            reportSeriesSubGroup = Report.CLASS
                            reportSeriesUid = 4
                            reportSeriesName = "Total duration"

                        })
                        reportSeries = Json.encodeToString(
                            ListSerializer(ReportSeries.serializer()),
                                reportSeriesWithFiltersList ?: listOf())
                    })
        }


    }

}