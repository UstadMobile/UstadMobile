package com.ustadmobile.port.android.view

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.screen.*
import com.ustadmobile.test.port.android.util.waitUntilWithActivityScenario
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
        systemImplNavRule.impl.messageIdMap = MessageIDMap.ID_MAP
        runBlocking {
            dbRule.insertPersonForActiveUser(Person().apply {
                firstNames = "Bob"
                lastName = "Jones"
                admin = true
                personUid = 42
            })
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
                reportSeriesDataSet = ReportSeries.NUMBER_STUDENTS_COMPLETED
                reportSeriesFilters = listOf(ReportFilter().apply {
                    reportFilterField = ReportFilter.FIELD_PERSON_GENDER
                    reportFilterCondition = ReportFilter.CONDITION_IS
                    reportFilterDropDownValue = Person.GENDER_FEMALE
                })
            })
        }

        val activityScenario = launchActivity<MainActivity>()

        init {

        }.run {

            MainScreen {

                bottomNav {
                    setSelectedItem(R.id.report_list_dest)
                }

                fab.click()
            }

            ReportEditScreen {

                fillFields(report = reportToCreate, setFieldsRequiringNavigation = false,
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
                dbRule.db.reportDao.findAllLive().waitUntilWithActivityScenario(activityScenario) { it.size == 1 }
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
