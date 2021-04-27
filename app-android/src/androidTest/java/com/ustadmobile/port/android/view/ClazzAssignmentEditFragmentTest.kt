package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ClazzAssignmentEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView


@AdbScreenRecord("ClazzAssignmentEdit screen Test")
class ClazzAssignmentEditFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

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


    @AdbScreenRecord("given ClazzAssignment not present when filled then should save to database")
    @Test
    fun givenNoClazzAssignmentPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            ClazzAssignmentEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = ClazzAssignment().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. clazzAssignmentName = "New ClazzAssignment"
        }

        init{

        }.run{

            ClazzAssignmentEditScreen{

                clazzAssignment.clazzAssignmentName?.takeIf {it != clazzAssignmentOnForm?.clazzAssignmentName }?.also {
                    ClazzAssignmentTitleInput{
                        edit{
                            clearText()
                            typeText(it)
                        }
                    }
                }

                //TODO: if required, use the savedstatehandle to add link entities
                fragmentScenario.onFragment { fragment ->
                    fragment.takeIf {clazzAssignment.relatedEntity != clazzAssignmentOnForm?.relatedEntity }
                            ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                            ?.set("RelatedEntityName", defaultGson().toJson(listOf(clazzAssignment.relatedEntity)))
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val clazzAssignmentList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("ClazzAssignment data set", "New ClazzAssignment",
                        clazzAssignmentList.first() .clazzAssignmentName)

            }


        }
    }


    @AdbScreenRecord("given ClazzAssignment exists when updated then should be updated on database")
    @Test
    fun givenClazzAssignmentExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingClazzAssignment = ClazzAssignment().apply {
            clazzAssignmentName = "New ClazzAssignment"
            caUid = dbRule.db.clazzAssignmentDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingClazzAssignment.caUid)) {
            ClazzAssignmentEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, ClazzAssignment::class.java).apply {
            clazzAssignmentName = "Updated ClazzAssignment"
        }

        init{


        }.run{

            ClazzAssignmentEditScreen {

                clazzAssignment.clazzAssignmentName?.takeIf {it != clazzAssignmentOnForm?.clazzAssignmentName }?.also {
                    ClazzAssignmentTitleInput{
                        edit{
                            clearText()
                            typeText(it)
                        }
                    }
                }

                //TODO: if required, use the savedstatehandle to add link entities

                fragmentScenario.onFragment { fragment ->
                    fragment.takeIf {clazzAssignment.relatedEntity != clazzAssignmentOnForm?.relatedEntity }
                            ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                            ?.set("RelatedEntityName", defaultGson().toJson(listOf(clazzAssignment.relatedEntity)))
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New ClazzAssignment",
                        defaultGson().fromJson(entityLoadedJson, ClazzAssignment::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingClazzAssignment.caUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated ClazzAssignment" }
                Assert.assertEquals("ClazzAssignment name is updated", "Updated ClazzAssignment",
                        updatedEntityFromDb?.clazzAssignmentName)

            }

        }

    }
}