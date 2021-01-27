/*
package com.ustadmobile.port.android.view

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.ReportListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.*
import com.ustadmobile.test.port.android.util.waitUntilWithActivityScenario
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@AdbScreenRecord("Report end-to-end test")
class ReportEndToEndTests : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setup() {
        runBlocking {
            dbRule.repo.insertTestStatements()
        }
    }


    @AdbScreenRecord("Given an empty report list, when the user clicks add report and fills in form, then the new report is shown in list")
    @Test
    fun givenEmptyReportList_whenUserClicksAddAndFillsInFormAndAddsToDashboardOnDetail_thenReportIsCreatedAndShownInList() {
        val reportToCreate = ReportWithSeriesWithFilters().apply {
            reportTitle = "New Report"
            xAxis = Report.MONTH
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesName = "Series X"
                reportSeriesVisualType = ReportSeries.BAR_CHART
                reportSeriesSubGroup = ReportSeries.NONE
                reportSeriesYAxis = ReportSeries.AVERAGE_USAGE_TIME_PER_USER
                reportSeriesFilters = listOf(ReportFilter().apply {
                    reportFilterField = ReportFilter.FIELD_PERSON_GENDER
                    reportFilterCondition = ReportFilter.CONDITION_IS
                    reportFilterDropDownValue = Person.GENDER_FEMALE
                })
            })
        }

        var activityScenario: ActivityScenario<MainActivity>? = null
        init {

            dbRule.insertPersonForActiveUser(Person().apply {
                admin = true
                firstNames = "Bob"
                lastName = "Jones"
            })
            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                it.putExtra(UstadView.ARG_NEXT,
                        "${ReportListView.VIEW_NAME}")
            }
            activityScenario = launchActivity(launchIntent)

        }.run {

            MainScreen {

                fab.click()
            }

            ReportEditScreen {

                fillFields(updatedReport = reportToCreate, setFieldsRequiringNavigation = false,
                        impl = systemImplNavRule.impl, context = context, testContext = this@run)

            }

            MainScreen {
                menuDone.click()
            }

            ReportDetailScreen {

                addToListButton {
                    click()
                }

            }

            val createdReport = runBlocking {
                dbRule.db.reportDao.findAllLive().waitUntilWithActivityScenario(activityScenario!!) { it.size == 1 }
            }!!.first()

            ReportListScreen {

                recycler {

                    firstChild<ReportListScreen.Report> {
                        reportLayout {
                            hasTag("${createdReport.reportUid}")
                            isDisplayed()

                        }
                        reportTitle{
                            hasText(reportToCreate.reportTitle!!)
                        }
                    }
                    }

                }

            }

        }

}
*/
