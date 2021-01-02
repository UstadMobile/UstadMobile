package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.WorkspaceTermsEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView


@AdbScreenRecord("WorkspaceTermsEdit screen Test")
class WorkspaceTermsEditFragmentTest : TestCase(){

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
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())


    @AdbScreenRecord("given WorkspaceTerms not present when filled then should save to database")
    @Test
    fun givenNoWorkspaceTermsPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            WorkspaceTermsEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = SiteTerms().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. workspaceTermsName = "New WorkspaceTerms"
        }

        init{

        }.run{

            WorkspaceTermsEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val workspaceTermsList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("WorkspaceTerms data set", "New WorkspaceTerms",
                        workspaceTermsList.first() .workspaceTermsName)

            }


        }
    }


    @AdbScreenRecord("given WorkspaceTerms exists when updated then should be updated on database")
    @Test
    fun givenWorkspaceTermsExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingWorkspaceTerms = SiteTerms().apply {
            workspaceTermsName = "New WorkspaceTerms"
            workspaceTermsUid = dbRule.db.siteTermsDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingWorkspaceTerms.workspaceTermsUid)) {
            WorkspaceTermsEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, SiteTerms::class.java).apply {
            workspaceTermsName = "Updated WorkspaceTerms"
        }

        init{


        }.run{

            WorkspaceTermsEditScreen {

                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New WorkspaceTerms",
                        defaultGson().fromJson(entityLoadedJson, SiteTerms::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingWorkspaceTerms.workspaceTermsUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated WorkspaceTerms" }
                Assert.assertEquals("WorkspaceTerms name is updated", "Updated WorkspaceTerms",
                        updatedEntityFromDb?.workspaceTermsName)

            }

        }

    }
}