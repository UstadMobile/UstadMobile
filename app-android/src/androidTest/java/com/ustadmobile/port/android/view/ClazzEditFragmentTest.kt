package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.view.UstadView
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
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Class edit screen tests")
class ClazzEditFragmentTest : TestCase() {

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


    @AdbScreenRecord("")
    @Test
    fun givenNoClazzPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        init {

        }.run {

            val existingHolidayCal = HolidayCalendar().apply {
                umCalendarName = "Demo Calendar"
                umCalendarUid = dbRule.db.holidayCalendarDao.insert(this)
            }

            val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
                ClazzEditFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                    it.arguments = bundleOf()
                }
            }


            val currentEntity = fragmentScenario.waitUntilLetOnFragment { it.entity }

            val formVals = ClazzWithHolidayCalendarAndSchool().apply {
                clazzName = "New Clazz"
                clazzDesc = "Description"
                clazzStartTime = (DateTime.now().toOffsetByTimezone("Asia/Dubai").localMidnight - 7.days).utc.unixMillisLong
                clazzEndTime = (DateTime.now().toOffsetByTimezone("Asia/Dubai").localMidnight - 30.days).utc.unixMillisLong
                holidayCalendar = existingHolidayCal
            }

            val schedules = listOf(Schedule().apply {
                scheduleDay = Schedule.DAY_MONDAY
                sceduleStartTime = 8.hours.millisecondsLong
                scheduleEndTime = 10.hours.millisecondsLong
                scheduleFrequency = Schedule.SCHEDULE_FREQUENCY_WEEKLY
                scheduleActive = true
            })

            ClazzEditScreen {

                fillFields(fragmentScenario, formVals, currentEntity, schedules)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                val clazzes = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("Clazz data set", "New Clazz", clazzes!!.first().clazzName)

                Assert.assertEquals("Schedules in database are set for class", schedules.size,
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
                    umCalendarUid = dbRule.db.holidayCalendarDao.insert(this)
                }

                val existingClazz = ClazzWithHolidayCalendarAndSchool().apply {
                    clazzName = "New Clazz"
                    clazzDesc = "Clazz description"
                    clazzHolidayUMCalendarUid = existingHolidayCal.umCalendarUid
                    clazzUid = dbRule.db.clazzDao.insert(this)
                }

                val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                        fragmentArgs = bundleOf(UstadView.ARG_ENTITY_UID to existingClazz.clazzUid)) {
                    ClazzEditFragment().also {
                        it.installNavController(systemImplNavRule.navController)
                    }
                }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                        .withScenarioIdlingResourceRule(crudIdlingResourceRule)


                //Freeze and serialize the value as it was first shown to the user
                var entityLoadedByFragment = fragmentScenario.waitUntilLetOnFragment { it.entity }
                val entityLoadedJson = defaultGson().toJson(entityLoadedByFragment)
                val newClazzValues = defaultGson().fromJson(entityLoadedJson, ClazzWithHolidayCalendarAndSchool::class.java).apply {
                    clazzName = "Updated Clazz"
                }

                fillFields(fragmentScenario, newClazzValues, entityLoadedByFragment)

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                Assert.assertEquals("Entity in database was loaded for user",
                        "New Clazz",
                        defaultGson().fromJson(entityLoadedJson, ClazzWithHolidayCalendarAndSchool::class.java).clazzName)

                val updatedEntityFromDb = dbRule.db.clazzDao.findByUidLive(existingClazz.clazzUid)
                        .waitUntilWithFragmentScenario(fragmentScenario) { it?.clazzName == "Updated Clazz" }
                Assert.assertEquals("Clazz name is updated", "Updated Clazz",
                        updatedEntityFromDb?.clazzName)

            }
        }
    }

}
