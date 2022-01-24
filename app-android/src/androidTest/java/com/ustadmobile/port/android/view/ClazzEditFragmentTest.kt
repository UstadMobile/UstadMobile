package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.hours
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.util.OneToManyJoinEditHelperMp.Companion.SUFFIX_RETKEY_DEFAULT
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.screen.ClazzEditScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import java.util.*

@AdbScreenRecord("Class edit screen tests")
class ClazzEditFragmentTest : TestCase() {

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

    lateinit var gson: Gson

    @Before
    fun setup() {
        gson = getApplicationDi().direct.instance()
    }

    @AdbScreenRecord("")
    @Test
    fun givenNoClazzPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val existingHolidayCal = HolidayCalendar().apply {
            umCalendarName = "Demo Calendar"
            umCalendarUid = dbRule.repo.holidayCalendarDao.insert(this)
        }

        lateinit  var fragmentScenario: FragmentScenario<ClazzEditFragment>
        init {
            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
                ClazzEditFragment().also {
                    it.installNavController(systemImplNavRule.navController,
                        initialDestId = R.id.clazz_edit_dest)

                    it.arguments = bundleOf()
                }
            }
        }.run {
            ClazzEditScreen {
                editNameLayout {
                    edit {
                        typeText("New Clazz")
                    }
                }

                editDescTextInput {
                    edit {
                        typeText("Description")
                    }
                }

                clazzStartTextInput {
                    edit {
                        setDateWithDialog(System.currentTimeMillis() - (30 * MS_PER_DAY),
                                TimeZone.getDefault().id)
                    }
                }

                clazzEndTextInput {
                    edit {
                        setDateWithDialog(System.currentTimeMillis() - (7 * MS_PER_DAY),
                                TimeZone.getDefault().id)
                    }
                }

                fragmentScenario.onFragment {
                    val schedule = Schedule().apply {
                        scheduleDay = Schedule.DAY_MONDAY
                        sceduleStartTime = 8.hours.millisecondsLong
                        scheduleEndTime = 10.hours.millisecondsLong
                        scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                        scheduleActive = true
                    }

                    it.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set(ClazzEdit2Presenter.ARG_SAVEDSTATE_SCHEDULES + SUFFIX_RETKEY_DEFAULT,
                                gson.toJson(listOf(schedule)))
                }

                fragmentScenario.onFragment {fragment ->
                    fragment.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set(ClazzEdit2Presenter.SAVEDSTATE_KEY_HOLIDAYCALENDAR,
                                gson.toJson(listOf(existingHolidayCal)))
                }

//                val repo = dbRule.repo as DoorDatabaseRepository
//                repo.clientId
                fragmentScenario.clickOptionMenu(R.id.menu_done)

                val clazzes = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("Clazz data set", "New Clazz", clazzes!!.first().clazzName)

                Assert.assertEquals("Schedules in database are set for class", 1,
                        dbRule.db.scheduleDao.findAllSchedulesByClazzUidAsLiveList(clazzes.first().clazzUid)
                                .waitUntilWithFragmentScenario(fragmentScenario) { it.isNotEmpty() }?.size)
            }

        }


    }

    @Test
    fun givenClazzExists_whenOpenedUpdatedAndSaveClicked_thenShouldBeUpdatedOnDatabase() {

        init {

        }.run {

            ClazzEditScreen {

                val existingHolidayCal = HolidayCalendar().apply {
                    umCalendarName = "Demo Calendar"
                    umCalendarUid = dbRule.repo.holidayCalendarDao.insert(this)
                }

                val existingClazz = ClazzWithHolidayCalendarAndSchool().apply {
                    clazzName = "New Clazz"
                    clazzDesc = "Clazz description"
                    clazzHolidayUMCalendarUid = existingHolidayCal.umCalendarUid
                    clazzUid = dbRule.repo.clazzDao.insert(this)
                }

                val args = mapOf(UstadView.ARG_ENTITY_UID to existingClazz.clazzUid.toString())

                val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                        fragmentArgs = args.toBundle()) {
                    ClazzEditFragment().also {
                        it.installNavController(systemImplNavRule.navController,
                            initialDestId = R.id.clazz_edit_dest,
                            initialArgs = args.toBundle())
                    }
                }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                        .withScenarioIdlingResourceRule(crudIdlingResourceRule)


                //Freeze and serialize the value as it was first shown to the user
                var entityLoadedByFragment = fragmentScenario.waitUntilLetOnFragment { it.entity }
                val entityLoadedJson = gson.toJson(entityLoadedByFragment)
                val newClazzValues = gson.fromJson(entityLoadedJson, ClazzWithHolidayCalendarAndSchool::class.java).apply {
                    clazzName = "Updated Clazz"
                }

                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment)

                val cal = Calendar.getInstance()
                cal.set(2020,5,31)
                clazzStartTextInput.edit.setDateWithDialog(cal.timeInMillis)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New Clazz",
                        gson.fromJson(entityLoadedJson, ClazzWithHolidayCalendarAndSchool::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingClazz.clazzUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated Clazz" }
                Assert.assertEquals("Clazz name is updated", "Updated Clazz",
                        updatedEntityFromDb?.clazzName)

            }
        }
    }

    companion object {
        const val MS_PER_DAY : Long = (1000 * 60 * 60 * 24)
    }

}
