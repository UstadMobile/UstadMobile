package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
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
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.*


class ReportEditFragmentTest {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @Test
    fun givenNoReportPresentYet_whenFilledInAndSaveClicked_thenShouldNavigateToDetailScreen() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme) {
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

        val person = Person().apply {
            firstNames = "Ustad"
            lastName = "Mobile"
            personUid = dbRule.db.personDao.insert(this)
        }

        val contentEntry = ContentEntry().apply {
            title = "Khan Academy"
            contentEntryUid = dbRule.db.contentEntryDao.insert(this)
        }


        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = ReportWithFilters().apply {
            reportTitle = "New Report"
            chartType = Report.LINE_GRAPH
            xAxis = Report.AVG_DURATION
            yAxis = Report.WEEK
            subGroup = Report.WEEK
            reportFilterList = listOf(
                    ReportFilter().apply {
                        entityType = ReportFilter.PERSON_FILTER
                        entityUid = person.personUid
                    },
                    ReportFilter().apply {
                        entityType = ReportFilter.VERB_FILTER
                        entityUid = verb.verbUid
                    },
                    ReportFilter().apply {
                        entityType = ReportFilter.CONTENT_FILTER
                        entityUid = contentEntry.contentEntryUid
                    })
        }

        fillFields(fragmentScenario, formVals, currentEntity)

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val reportList = dbRule.db.reportDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertEquals("Report data set", "New Report",
                reportList?.first()?.reportTitle)
    }


    @Test
    fun givenReportExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingReport = ReportWithFilters().apply {
            reportTitle = "New Report"
            reportUid = dbRule.db.reportDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
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
        }

        fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment)

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        Assert.assertEquals("Entity in database was loaded for user",
                "New Report",
                defaultGson().fromJson(entityLoadedJson, ReportWithFilters::class.java))

        val updatedEntityFromDb = dbRule.db.reportDao.findByUidLive(existingReport.reportUid)
                .waitUntilWithFragmentScenario(fragmentScenario) { it?.reportTitle == "Updated Report" }
        Assert.assertEquals("Report name is updated", "Updated Report",
                updatedEntityFromDb?.reportTitle)
    }

    companion object {

        fun fillFields(fragmentScenario: FragmentScenario<ReportEditFragment>,
                       report: ReportWithFilters,
                       reportOnForm: ReportWithFilters?,
                       setFieldsRequiringNavigation: Boolean = true) {

            report.reportTitle?.takeIf { it != reportOnForm?.reportTitle }?.also {
                onView(withId(R.id.fragment_report_edit_title)).perform(clearText(), typeText(it))
            }



            if (!setFieldsRequiringNavigation) {
                return
            }

            //TODO: if required, use the savedstatehandle to add link entities

            fragmentScenario.onFragment { fragment ->
                fragment.takeIf { report.reportFilterList != reportOnForm?.reportFilterList }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("VerbDisplay", defaultGson().toJson(listOf(ReportFilter())))
            }

        }
    }
}