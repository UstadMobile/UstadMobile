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
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toDisplayString
import com.ustadmobile.core.view.ReportListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.*
import com.ustadmobile.test.port.android.util.setMessageIdOption
import com.ustadmobile.test.port.android.util.waitUntilWithActivityScenario
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertTestStatements
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
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

    val impl = UstadMobileSystemImpl.instance

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setup() {
        runBlocking {
            dbRule.insertPersonForActiveUser(Person().apply {
                admin = true
                firstNames = "Bob"
                lastName = "Jones"
                personUid = 42
            })
            dbRule.repo.reportDao.initPreloadedTemplates()
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
                reportSeriesSubGroup = Report.GENDER
                reportSeriesYAxis = ReportSeries.INTERACTIONS_RECORDED
                reportSeriesFilters = listOf(ReportFilter().apply {
                    reportFilterField = ReportFilter.FIELD_PERSON_GENDER
                    reportFilterCondition = ReportFilter.CONDITION_IS
                    reportFilterDropDownValue = Person.GENDER_FEMALE
                })
            })
        }

        var activityScenario: ActivityScenario<MainActivity>? = null
        init {

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

            ReportTemplateScreen{

                recycler.firstChild<ReportTemplateScreen.ReportTemplate> {
                    title.click()
                }

            }

            ReportEditScreen {

                val reportOnForm = Report.FIXED_TEMPLATES[0]
                val listOfSeries = Json.parse(ReportSeries.serializer().list, reportOnForm.reportSeries!!)

                fillFields(updatedReport = reportToCreate,
                        reportOnForm = ReportWithSeriesWithFilters(reportOnForm, listOfSeries),
                        setFieldsRequiringNavigation = false,
                        impl = impl, context = context, testContext = this@run)

                nestedScroll.swipeUp()

                seriesRecycler{

                    scrollTo {
                        withDescendant { withText("Filter") }
                    }
                    scrollTo {
                        withDescendant { withId(R.id.item_edit_report_filter_add_layout) }
                    }

                    firstChild<ReportEditScreen.Series> {

                        addFilterButton.click()

                        ReportFilterEditScreen{

                            setMessageIdOption(fieldTextValue,
                                    impl.getString(
                                            ReportFilterEditPresenter.FieldOption.PERSON_GENDER.messageId,
                                            ApplicationProvider.getApplicationContext()))

                            setMessageIdOption(conditionTextValue,
                                    impl.getString(
                                            ReportFilterEditPresenter.ConditionOption.IS_CONDITION.messageId,
                                            ApplicationProvider.getApplicationContext()))

                            setMessageIdOption(valueDropDownTextValue,
                                    impl.getString(MessageID.female,
                                            ApplicationProvider.getApplicationContext()))

                            MainScreen {
                                menuDone.click()
                            }
                        }


                        filterRecycler{

                            hasSize(1)
                            firstChild<ReportEditScreen.Filter> {
                                filterName{
                                    hasText(ReportFilter().apply {
                                        reportFilterField = ReportFilter.FIELD_PERSON_GENDER
                                        reportFilterCondition = ReportFilter.CONDITION_IS
                                        reportFilterDropDownValue = Person.GENDER_FEMALE
                                    }.toDisplayString(ApplicationProvider.getApplicationContext()))
                                }
                            }

                        }

                    }
                }

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
                dbRule.db.reportDao.findAllActiveReportLive(false)
                        .waitUntilWithActivityScenario(activityScenario!!) { it.size == 1 }
            }!!.first()

            ReportListScreen {

                recycler {

                    childWith<ReportListScreen.Report> {
                        withTag(createdReport.reportUid)
                    } perform {
                        reportTitle{
                            hasText(reportToCreate.reportTitle!!)
                        }
                    }
                    }

                }

            }

        }

}
