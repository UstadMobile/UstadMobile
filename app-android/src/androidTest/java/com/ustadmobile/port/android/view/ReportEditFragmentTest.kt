/*
package com.ustadmobile.port.android.view

import android.content.Context
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.soywiz.klock.DateTime
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*

@AdbScreenRecord("Report edit screen tests")
class ReportEditFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @AdbScreenRecord("with no report present, fill all the fields and navigate to detail")
    @Test
    fun givenNoReportPresentYet_whenFilledInAndSaveClicked_thenShouldNavigateToDetailScreen() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            ReportEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val verb = VerbEntity().apply {
            urlId = "progressed"
            verbUid = dbRule.db.verbDao.insert(this)
        }

        val xlangEntry = XLangMapEntry().apply {
            verbLangMapUid = verb.verbUid
            valueLangMap = "Progress"
            statementLangMapUid = dbRule.db.xLangMapEntryDao.insert(this)
        }

        val verbDisplay = VerbDisplay().apply {
            verbUid = verb.verbUid
            urlId = verb.urlId
            display = xlangEntry.valueLangMap
        }

        val person = Person().apply {
            firstNames = "Ustad"
            lastName = "Mobile"
            personUid = dbRule.db.personDao.insert(this)
        }

        val contentEntry = ContentEntry().apply {
            title = "Khan Academy"
            description = "content here"
            contentEntryUid = dbRule.db.contentEntryDao.insert(this)
        }


        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = ReportWithFilters().apply {
            reportTitle = "New Report"
            chartType = Report.LINE_GRAPH
            yAxis = Report.AVG_DURATION
            xAxis = Report.WEEK
            subGroup = Report.GENDER
            fromDate = DateTime(2019, 4, 10).unixMillisLong
            toDate = DateTime(2019, 6, 11).unixMillisLong
        }

        fillFields(fragmentScenario, formVals, currentEntity, true,
                person, verbDisplay, contentEntry,
                impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext())

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val reportList = dbRule.db.reportDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertEquals("Should not be in db", 0, reportList?.size)

        Assert.assertEquals("After finishing edit report, it navigates to detail view",
                R.id.report_detail_dest, systemImplNavRule.navController.currentDestination?.id)
        val currentArgs = systemImplNavRule.navController.currentDestination?.arguments

    }


    @AdbScreenRecord("with an existing report, when updated, on click done, save on database")
    @Test
    fun givenReportExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingReport = ReportWithFilters().apply {
            reportTitle = "New Report"
            chartType = Report.LINE_GRAPH
            yAxis = Report.AVG_DURATION
            xAxis = Report.WEEK
            subGroup = Report.GENDER
            reportUid = dbRule.db.reportDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingReport.reportUid)) {
            ReportEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val editIdlingResource = UstadSingleEntityFragmentIdlingResource(fragmentScenario.letOnFragment { it })
        IdlingRegistry.getInstance().register(editIdlingResource)

        onIdle()

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, ReportWithFilters::class.java).apply {
            reportTitle = "Updated Report"
            chartType = Report.BAR_CHART
            yAxis = Report.COUNT_ACTIVITIES
            xAxis = Report.MONTH
            subGroup = Report.GENDER
        }

        val person = Person().apply {
            firstNames = "Ustad"
            lastName = "Mobile"
            personUid = dbRule.db.personDao.insert(this)
        }

        fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment, person = person,
                impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext())

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        Assert.assertEquals("Entity in database was loaded for user",
                "New Report",
                defaultGson().fromJson(entityLoadedJson, ReportWithFilters::class.java).reportTitle)

        val updatedEntityFromDb = dbRule.db.reportDao.findByUidLive(existingReport.reportUid)
                .waitUntilWithFragmentScenario(fragmentScenario) { it?.reportTitle == "Updated Report" }
        val reportFilerListFromDb = dbRule.db.reportFilterDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertEquals("Report name is updated", "Updated Report",
                updatedEntityFromDb?.reportTitle)
        Assert.assertEquals("chart type updated", Report.BAR_CHART,
                updatedEntityFromDb?.chartType)
        Assert.assertEquals("y axis updated", Report.COUNT_ACTIVITIES,
                updatedEntityFromDb?.yAxis)
        Assert.assertEquals("y axis updated", Report.COUNT_ACTIVITIES,
                updatedEntityFromDb?.yAxis)
        Assert.assertEquals("x axis updated", Report.MONTH,
                updatedEntityFromDb?.xAxis)
        Assert.assertEquals("subgroup updated", Report.GENDER,
                updatedEntityFromDb?.subGroup)
        Assert.assertEquals("one filter added", 1,
                reportFilerListFromDb!!.size)
    }

    companion object {

        fun fillFields(fragmentScenario: FragmentScenario<ReportEditFragment>? = null,
                       report: ReportWithFilters,
                       reportOnForm: ReportWithFilters? = ReportWithFilters(),
                       setFieldsRequiringNavigation: Boolean = true, person: Person? = null,
                       verbDisplay: VerbDisplay? = null, entry: ContentEntry? = null, impl: UstadMobileSystemImpl, context: Context) {

            report.reportTitle?.takeIf { it != reportOnForm?.reportTitle }?.also {
                onView(withId(R.id.fragment_report_edit_title)).perform(clearText(), typeText(it))
            }

            Espresso.closeSoftKeyboard()

            report.chartType.takeIf { it != reportOnForm?.chartType }?.also {
                setMessageIdOption(R.id.fragment_edit_report_dialog_visual_type_textinputlayout,
                        impl.getString(ReportEditPresenter.ChartOptions.values().find { report -> report.optionVal == it }!!.messageId, context))
            }

            report.yAxis.takeIf { it != reportOnForm?.yAxis }?.also {
                setMessageIdOption(R.id.fragment_edit_report_dialog_yaxis_textinputlayout,
                        impl.getString(ReportEditPresenter.YAxisOptions.values().find { report -> report.optionVal == it }!!.messageId, context))
            }

            report.xAxis.takeIf { it != reportOnForm?.xAxis }?.also {
                setMessageIdOption(R.id.fragment_edit_report_dialog_xaxis_textinputlayout,
                        impl.getString(ReportEditPresenter.XAxisOptions.values().find { report -> report.optionVal == it }!!.messageId, context))
            }

            report.subGroup.takeIf { it != reportOnForm?.subGroup }?.also {
                setMessageIdOption(R.id.fragment_edit_report_dialog_subgroup_textinputlayout,
                        impl.getString(ReportEditPresenter.XAxisOptions.values().find { report -> report.optionVal == it }!!.messageId, context))
            }

            report.fromDate.takeIf { it != reportOnForm?.fromDate }?.also {
                setDateField(R.id.activity_report_edit_fromDate_textinputlayout, it)
            }
            report.toDate.takeIf { it != reportOnForm?.toDate }?.also {
                setDateField(R.id.activity_report_edit_toDate_textinputlayout, it)
            }

            if (!setFieldsRequiringNavigation) {
                return
            }

            fragmentScenario?.onFragment { fragment ->
                fragment.takeIf { verbDisplay != null }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("VerbDisplay", defaultGson().toJson(listOf(verbDisplay)))
            }

            fragmentScenario?.onFragment { fragment ->
                fragment.takeIf { person != null }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("Person", defaultGson().toJson(listOf(person)))
            }

            fragmentScenario?.onFragment { fragment ->
                fragment.takeIf { entry != null }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("Content", defaultGson().toJson(listOf(entry)))
            }

        }
    }
}*/
