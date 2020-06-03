package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.fragment.findNavController
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.toughra.ustadmobile.R
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendarAndSchool
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.test.port.android.util.*
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class ClazzEditFragmentTest  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @Test
    fun givenNoClazzPresentYet_whenFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val existingHolidayCal = HolidayCalendar().apply {
            umCalendarName = "Demo Calendar"
            umCalendarUid = dbRule.db.holidayCalendarDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_UstadTheme) {
            ClazzEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
                it.arguments = bundleOf()
            }
        }

        val currentEntity = fragmentScenario.letOnFragment { it.entity }
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


        fillFields( fragmentScenario, formVals, currentEntity, schedules)

        fragmentScenario.clickOptionMenu(R.id.menu_done)

        val clazzes = dbRule.db.clazzDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
            it.isNotEmpty()
        }

        Assert.assertEquals("Clazz data set", "New Clazz", clazzes!!.first().clazzName)

        Assert.assertEquals("Schedules in database are set for class", schedules.size,
            dbRule.db.scheduleDao.findAllSchedulesByClazzUidAsLiveList(clazzes.first().clazzUid)
                    .waitUntilWithFragmentScenario(fragmentScenario){ it.isNotEmpty() }?.size)
    }


    companion object {

        fun fillFields(fragmentScenario: FragmentScenario<ClazzEditFragment>,
                        clazz: ClazzWithHolidayCalendarAndSchool,
                       clazzOnForm: ClazzWithHolidayCalendarAndSchool?,
                       schedules: List<Schedule> = listOf(),
                       schedulesOnForm: List<Schedule>? = null,
                       setFieldsRequiringNavigation: Boolean = true) {

            takeIf { clazz.clazzName != clazzOnForm?.clazzName }?.run {
                onView(withId(R.id.activity_clazz_edit_name_text)).perform(clearText(), typeText(clazz.clazzName))
            }
            takeIf { clazz.clazzDesc != clazzOnForm?.clazzName}?.run {
                onView(withId(R.id.activity_clazz_edit_desc_text)).perform(clearText(), typeText(clazz.clazzDesc))
            }
            takeIf { clazz.clazzStartTime != clazzOnForm?.clazzStartTime }?.run {
                setDateField(R.id.activity_clazz_edit_start_date_edittext, clazz.clazzStartTime)
            }
            takeIf { clazz.clazzEndTime != clazzOnForm?.clazzEndTime}?.run {
                setDateField(R.id.activity_clazz_edit_end_date_edittext, clazz.clazzEndTime)
            }


            if(!setFieldsRequiringNavigation) {
                return
            }


            schedules.filter { schedulesOnForm == null || it !in schedulesOnForm }.forEach {schedule ->
                fragmentScenario.onFragment {
                    it.findNavController().currentBackStackEntry?.savedStateHandle
                            ?.set("Schedule", defaultGson().toJson(listOf(schedule)))
                }
                onIdle()
            }

            fragmentScenario.onFragment {fragment ->
                fragment.takeIf {clazz.holidayCalendar != clazzOnForm?.holidayCalendar }
                        ?.findNavController()?.currentBackStackEntry?.savedStateHandle
                        ?.set("HolidayCalendar", defaultGson().toJson(listOf(clazz.holidayCalendar)))
            }

        }

    }
}