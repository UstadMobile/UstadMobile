package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.WorkSpaceEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson

import com.ustadmobile.lib.db.entities.WorkSpace
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.UstadView


@AdbScreenRecord("WorkSpaceEdit screen Test")
class WorkSpaceEditFragmentTest : TestCase(){

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


    @AdbScreenRecord("given WorkSpace not present when filled then should save to database")
    @Test
    fun givenNoWorkSpacePresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            WorkSpaceEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
        val formVals = WorkSpace().apply {
            //TODO: set the values that will be entered on the form here
            //e.g. workSpaceName = "New WorkSpace"
        }

        init{

        }.run{

            WorkSpaceEditScreen{

                fillFields(fragmentScenario, formVals, currentEntity,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val workSpaceList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("WorkSpace data set", "New WorkSpace",
                        workSpaceList.first() .workSpaceName)

            }


        }
    }


    @AdbScreenRecord("given WorkSpace exists when updated then should be updated on database")
    @Test
    fun givenWorkSpaceExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingWorkSpace = WorkSpace().apply {
            workSpaceName = "New WorkSpace"
            workSpaceUid = dbRule.db.siteDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingWorkSpace.workSpaceUid)) {
            WorkSpaceEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
        val newClazzValues = defaultGson().fromJson(entityLoadedJson, WorkSpace::class.java).apply {
            workSpaceName = "Updated WorkSpace"
        }

        init{


        }.run{

            WorkSpaceEditScreen {

                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment,
                        impl = systemImplNavRule.impl, context = ApplicationProvider.getApplicationContext(),
                        testContext = this@run)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New WorkSpace",
                        defaultGson().fromJson(entityLoadedJson, WorkSpace::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingWorkSpace.workSpaceUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated WorkSpace" }
                Assert.assertEquals("WorkSpace name is updated", "Updated WorkSpace",
                        updatedEntityFromDb?.workSpaceName)

            }

        }

    }
}