/*
package com.ustadmobile.port.android.view

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.agoda.kakao.common.views.KView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.port.android.screen.*
import com.ustadmobile.test.port.android.util.waitUntilWithActivityScenario
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
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    private val context = ApplicationProvider.getApplicationContext<Application>()

    val impl = UstadMobileSystemImpl.instance

    @Before
    fun setup() {
        impl.messageIdMap = MessageIDMap.ID_MAP
        runBlocking {
            dbRule.db.insertTestStatements()
        }

    }


    @AdbScreenRecord("Given an empty report list, when the user clicks add report and fills in form, then the new report is shown in list")
    @Test
    fun givenEmptyReportList_whenUserClicksAddAndFillsInFormAndAddsToDashboardOnDetail_thenReportIsCreatedAndShownInList() {
        val newClazzValues = ReportWithFilters().apply {
            reportTitle = "Updated Report"
            chartType = Report.BAR_CHART
            yAxis = Report.SCORE
            xAxis = Report.MONTH
            subGroup = Report.GENDER
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
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

                fillFields(report = newClazzValues, setFieldsRequiringNavigation = false, impl = impl, context = context)

                personAddList.click()

            }

            PersonListScreen {

                recycler {
                    childWith<PersonListScreen.Person> {
                        withDescendant { withText("Hello World") }
                    } perform {
                        click()
                    }
                }

            }

            MainScreen {

                KView {
                    withId(R.id.menu_done)
                } perform {
                    click()
                }

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
                    }

                }

            }


        }

        */
/*  onView(AllOf.allOf(withId(R.id.item_createnew_line1_text),
                  ViewMatchers.isDescendantOfA(withId(R.id.fragment_edit_report_who_add_layout))))
                  .perform(click())

          onIdle()

          onView(withText("Hello World")).perform(click())

          onView(withId(R.id.menu_done)).perform(click())

          onView(withId(R.id.preview_add_to_dashboard_button)).perform(click())

          val createdReport = runBlocking {
              dbRule.db.reportDao.findAllLive().waitUntilWithActivityScenario(activityScenario) { it.size == 1 }
          }!!.first()
          onView(Matchers.allOf(withId(R.id.item_reportlist_report_cl), ViewMatchers.withTagValue(Matchers.equalTo(createdReport.reportUid))))
                  .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))*//*


    }

}
*/
