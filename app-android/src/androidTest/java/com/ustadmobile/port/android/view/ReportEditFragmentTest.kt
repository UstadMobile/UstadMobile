package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.db.dao.ReportDao
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.ext.toQueryLikeParam
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.screen.ReportEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance


@AdbScreenRecord("Report edit screen tests")
class ReportEditFragmentTest: TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    private lateinit var gson: Gson


    @Before
    fun setup() {
        gson = getApplicationDi().direct.instance()
    }

    @AdbScreenRecord("with no report present, fill all the fields and navigate to detail")
    @Test
    fun givenNoReportPresentYet_whenFilledInAndSaveClicked_thenShouldNavigateToDetailScreen() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            ReportEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity: ReportWithSeriesWithFilters? =
                fragmentScenario.waitUntilLetOnFragment { it.entity }
        val formVals = ReportWithSeriesWithFilters().apply {
            reportTitle = "New Report"
            xAxis = Report.WEEK
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            reportSeriesWithFiltersList = listOf(ReportSeries().apply {
                reportSeriesName = "Total Duration"
                reportSeriesVisualType = ReportSeries.BAR_CHART
                reportSeriesYAxis = ReportSeries.TOTAL_DURATION
            })
        }

        init{

        }.run{

            ReportEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity, true,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                val reportList = dbRule.repo.reportDao
                        .findAllActiveReportLive(false)
                        .waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("Should not be in db", 0, reportList?.size)

                Assert.assertEquals("After finishing edit report, it navigates to detail view",
                        R.id.report_detail_dest, systemImplNavRule.navController.currentDestination?.id)
            }

        }

    }


    @AdbScreenRecord("with an existing report, when updated, on click done, save on database")
    @Test
    fun givenReportExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingReport = ReportWithSeriesWithFilters().apply {
            reportTitle = "New Report"
            xAxis = Report.WEEK
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
            val reportSeriesList = listOf(ReportSeries().apply {
                reportSeriesUid = 1
                reportSeriesName = "Series 2"
                reportSeriesYAxis = ReportSeries.TOTAL_DURATION
                reportSeriesSubGroup = Report.GENDER
                reportSeriesVisualType = ReportSeries.LINE_GRAPH
                reportSeriesFilters = mutableListOf(ReportFilter().apply {
                    reportFilterUid = 1
                    reportFilterSeriesUid = 1
                    reportFilterField = ReportFilter.FIELD_PERSON_GENDER
                    reportFilterCondition = ReportFilter.CONDITION_IS
                    reportFilterDropDownValue = Person.GENDER_MALE
                }, ReportFilter().apply {
                    reportFilterUid = 2
                    reportFilterSeriesUid = 1
                    reportFilterField = ReportFilter.FIELD_PERSON_AGE
                    reportFilterCondition = ReportFilter.CONDITION_GREATER_THAN
                    reportFilterValue = 13.toString()
                })
            })
            reportSeries = Gson().toJson(reportSeriesList)
            reportUid = dbRule.repo.reportDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingReport.reportUid)) {
            ReportEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }


        //Freeze and serialize the value as it was first shown to the user
        var entityLoadedByFragment: ReportWithSeriesWithFilters? = null
        while(entityLoadedByFragment == null){
            entityLoadedByFragment = fragmentScenario.nullableLetOnFragment { it.entity }
            Thread.sleep(100)
        }

        val entityLoadedJson = gson.toJson(entityLoadedByFragment)
        val updatedReport = gson.fromJson(entityLoadedJson, ReportWithSeriesWithFilters::class.java).apply {
            reportTitle = "Updated Report"
            xAxis = Report.MONTH
            val reportSeriesList = listOf(ReportSeries().apply {
                reportSeriesUid = 1
                reportSeriesName = "Series 2"
                reportSeriesYAxis = ReportSeries.TOTAL_DURATION
                reportSeriesSubGroup = Report.GENDER
                reportSeriesVisualType = ReportSeries.BAR_CHART
                reportSeriesFilters = mutableListOf(ReportFilter().apply {
                    reportFilterUid = 1
                    reportFilterSeriesUid = 1
                    reportFilterField = ReportFilter.FIELD_PERSON_GENDER
                    reportFilterCondition = ReportFilter.CONDITION_IS
                    reportFilterDropDownValue = Person.GENDER_MALE
                })
            })
            reportSeriesWithFiltersList = reportSeriesList
            reportSeries = Gson().toJson(reportSeriesList)
        }

        init{

        }.run{

            ReportEditScreen{

                fillFields(fragmentScenario, updatedReport, entityLoadedByFragment, true,
                        impl = systemImplNavRule.impl,
                        context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity json was updated",
                        "New Report",
                        gson.fromJson(entityLoadedJson, ReportWithSeriesWithFilters::class.java).reportTitle)


                val updatedEntityFromDb = dbRule.db.reportDao.findByUidLive(existingReport.reportUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.reportTitle == "Updated Report" }

                val listSeriesType = object : TypeToken<ArrayList<ReportSeries?>?>() {}.type
                val seriesList = gson.fromJson<List<ReportSeries>>(updatedEntityFromDb!!.reportSeries, listSeriesType)

                Assert.assertEquals("Report name is updated", "Updated Report",
                        updatedEntityFromDb.reportTitle)

                Assert.assertEquals("Series changed to bar chart", ReportSeries.BAR_CHART,
                        seriesList[0].reportSeriesVisualType)

                Assert.assertEquals("Filter size reduced to 1", 1,
                        seriesList[0].reportSeriesFilters!!.size)
            }

        }


    }

}
