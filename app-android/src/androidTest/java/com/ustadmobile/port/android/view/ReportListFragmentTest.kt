package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.port.android.screen.ReportListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertTestStatementsForReports
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Report list screen test")
class ReportListFragmentTest : TestCase() {

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

    @AdbScreenRecord("given report in list, when clicked, go to detail report")
    @Test
    fun givenReportPresent_whenClickOnReport_thenShouldNavigateToReportDetail() {
        val testEntity = Report().apply {
            reportTitle = "Test Name"
            xAxis = Report.MONTH
            reportOwnerUid = 42
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeries = """                
                        [{
                         "reportSeriesUid": 0,
                         "reportSeriesName": "Series 1",
                         "reportSeriesYAxis": ${ReportSeries.TOTAL_DURATION},
                         "reportSeriesVisualType": ${ReportSeries.BAR_CHART},
                         "reportSeriesSubGroup": ${ReportSeries.NONE}
                        }]
                    """.trimIndent()
            reportUid = dbRule.repo.reportDao.insert(this)
        }


        init {

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf()) {
                ReportListFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run {

            ReportListScreen {

                recycler {
                    childWith<ReportListScreen.Report> {
                        withTag(testEntity.reportUid)
                    } perform {

                        reportTitle.hasText(testEntity.reportTitle!!)

                        click()
                    }
                }

            }

            Assert.assertEquals("After clicking on item, it navigates to detail view",
                    R.id.report_detail_dest, systemImplNavRule.navController.currentDestination?.id)
        }
    }

}