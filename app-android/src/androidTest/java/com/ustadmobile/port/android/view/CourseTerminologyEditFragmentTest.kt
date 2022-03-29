package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.port.android.screen.CourseTerminologyEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance

@AdbScreenRecord("CourseTerminologyEdit screen Test")
class CourseTerminologyEditFragmentTest : TestCase(){

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


    @AdbScreenRecord("given CourseTerminology not present when filled then should save to database")
    @Test
    fun givenNoCourseTerminologyPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            CourseTerminologyEditFragment(). also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            CourseTerminologyEditScreen{


                fragmentScenario.clickOptionMenu(R.id.menu_done)


                val courseTerminologyList = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }


            }


        }
    }


    @AdbScreenRecord("given CourseTerminology exists when updated then should be updated on database")
    @Test
    fun givenCourseTerminologyExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {
        val existingCourseTerminology = CourseTerminology().apply {
            ctTitle = "New CourseTerminology"
            ctUid = dbRule.db.courseTerminologyDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingCourseTerminology.ctUid)) {
            CourseTerminologyEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        //Freeze and serialize the value as it was first shown to the user
        val entityLoadedByFragment = fragmentScenario.letOnFragment { it.entity }
        val gson: Gson = getApplicationDi().direct.instance()
        val entityLoadedJson = gson.toJson(entityLoadedByFragment)
        val newClazzValues = gson.fromJson(entityLoadedJson, CourseTerminology::class.java).apply {
            ctTitle = "Updated CourseTerminology"
        }

        init{


        }.run{

            CourseTerminologyEditScreen {



                //TODO: if required, use the savedstatehandle to add link entities

                /*
                fragmentScenario.onFragment { fragment ->
                    fragment..findNavController()?.currentBackStackEntry?.savedStateHandle
                            ?.set("RelatedEntityName", defaultGson().toJson(listOf(courseTerminology.relatedEntity)))
                }
                */

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New CourseTerminology",
                        gson.fromJson(entityLoadedJson, CourseTerminology::class.java).ctTitle)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingCourseTerminology.ctUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated CourseTerminology" }

            }

        }

    }
}