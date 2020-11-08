/*
package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.nhaarman.mockitokotlin2.mock
import com.soywiz.klock.hours
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.*
import com.ustadmobile.util.test.ext.insertTestClazzAndMembers
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Attendance recording screen test")
class ClazzLogEditAttendanceFragmentTest  {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null, 3)
    }

    private fun markStudent(studentIndex: Int, buttonId: Int) {
        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(studentIndex,
                        RecyclerViewChildActions.actionOnChild(click(), buttonId)))
    }

    private fun clickDoneAndWaitForAttendanceToSave(scenario: FragmentScenario<*>, clazzLogUid: Long) {
        scenario.clickOptionMenu(R.id.menu_done)

        dbRule.db.clazzLogDao.findByUidLive(clazzLogUid)
                .waitUntilWithFragmentScenario(scenario, 5000) {
                    it?.clazzLogStatusFlag == ClazzLog.STATUS_RECORDED
                }
    }

    @AdbScreenRecord("Given an existing class when mixed attendance is recorded should be saved to database")
    //@Test
    fun givenExistingClazzWithMembesAndClazzLog_whenMixedStudentAttendanceRecorded_thenShouldBeSavedToDatabase() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzAndMembers = runBlocking { dbRule.db.insertTestClazzAndMembers(5) }

        val clazzLog = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = dbRule.db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogEditAttendanceFragment>(
            fragmentArgs = bundleOf(UstadView.Companion.ARG_ENTITY_UID to clazzLog.clazzLogUid.toString()), themeResId = R.style.UmTheme_App) {
            ClazzLogEditAttendanceFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        clazzLogAttendanceListScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.clazzLogEditRecyclerView
        }

        //Mark first three present
        (0..2).forEach {position ->
            markStudent(position + 3, R.id.present_button)
        }

        //mark next two absent
        (3..4).forEach {position ->
            markStudent(position + 3, R.id.absent_button)
        }

        clickDoneAndWaitForAttendanceToSave(clazzLogAttendanceListScenario, clazzLog.clazzLogUid)


        val clazzLogAttendanceRecords = runBlocking { dbRule.db.clazzLogAttendanceRecordDao.findByClazzLogUid(clazzLog.clazzLogUid) }
        Assert.assertEquals("Found expected number of attendance records", clazzAndMembers.studentList.size,
                clazzLogAttendanceRecords.size)
        Assert.assertEquals("Expected number of students are present", 3,
            clazzLogAttendanceRecords.filter {it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED}.size)
        Assert.assertEquals("Expected number of students are absent",2,
                clazzLogAttendanceRecords.filter {it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ABSENT}.size)

        val clazzLogInDb = dbRule.db.clazzLogDao.findByUid(clazzLog.clazzLogUid)
        Assert.assertEquals("Expected numbe of students are present on ClazzLog entity", 3,
            clazzLogInDb!!.clazzLogNumPresent)

        Assert.assertEquals("Expected numbe of students are absent on ClazzLog entity", 2,
                clazzLogInDb!!.clazzLogNumAbsent)

        IdlingRegistry.getInstance().unregister(recyclerViewIdlingResource)
    }


    //@Test
    @AdbScreenRecord("Given an existing class when mark all is clicked and user saves, then should be saved to database")
    fun givenExistingClazzLog_whenClickMarkAll_thenShouldBeSavedToDatabase() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzAndMembers = runBlocking { dbRule.db.insertTestClazzAndMembers(5) }

        val clazzLog = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = dbRule.db.clazzLogDao.insert(this)
        }

        val clazzLog2 = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis() - 24.hours.millisecondsLong,
            0L).apply {
            clazzLogUid = dbRule.db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogEditAttendanceFragment>(
                bundleOf(UstadView.Companion.ARG_ENTITY_UID to clazzLog.clazzLogUid.toString()), themeResId = R.style.UmTheme_App
        ){ClazzLogEditAttendanceFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        clazzLogAttendanceListScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.clazzLogEditRecyclerView
        }

        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.mark_all_present)),
                    click()))

        clickDoneAndWaitForAttendanceToSave(clazzLogAttendanceListScenario, clazzLog.clazzLogUid)

        val clazzLogAttendanceRecords = runBlocking {
            dbRule.db.clazzLogAttendanceRecordDao.findByClazzLogUid(clazzLog.clazzLogUid)
        }
        Assert.assertTrue("All clazz logs are marked as attended",
                clazzLogAttendanceRecords.all { it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED})

        IdlingRegistry.getInstance().unregister(recyclerViewIdlingResource)

    }


   // @Test
    @AdbScreenRecord("Given an existing class, the user can go to a previous day and fill in attendance for that day. Both are saved to database")
    fun givenExistingClazzLog_whenClickMarkAllAndClickToPrevClazzLog_willSaveToDatabaseAndCanMarkPrevDay() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzAndMembers = runBlocking { dbRule.db.insertTestClazzAndMembers(5) }

        val clazzLog = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = dbRule.db.clazzLogDao.insert(this)
        }

        val prevDayClazzLog = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis() - 24.hours.millisecondsLong,
                0L).apply {
            clazzLogUid = dbRule.db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogEditAttendanceFragment>(
                bundleOf(UstadView.Companion.ARG_ENTITY_UID to clazzLog.clazzLogUid.toString()), themeResId = R.style.UmTheme_App
        ){ClazzLogEditAttendanceFragment().also {
            it.installNavController(systemImplNavRule.navController)
        }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)

        clazzLogAttendanceListScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.clazzLogEditRecyclerView
        }

        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.mark_all_present)),
                        click()))

        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
                        RecyclerViewChildActions.actionOnChild(click(), R.id.prev_button)))

        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.mark_all_present)),
                        click()))


        clickDoneAndWaitForAttendanceToSave(clazzLogAttendanceListScenario, prevDayClazzLog.clazzLogUid)

        val clazzLogAttendanceRecords = runBlocking {
            dbRule.db.clazzLogAttendanceRecordDao.findByClazzLogUid(
                    clazzLog.clazzLogUid)
        }
        Assert.assertTrue("All clazz logs are marked as attended for most recent day",
                clazzLogAttendanceRecords.all { it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED})

        val prevDayClazzLogAttendanceRecords = runBlocking {
            dbRule.db.clazzLogAttendanceRecordDao.findByClazzLogUid(clazzLog.clazzLogUid)
        }

        Assert.assertTrue("All clazz logs are marked as attended for most recent day",
                prevDayClazzLogAttendanceRecords.all { it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED})

       IdlingRegistry.getInstance().unregister(recyclerViewIdlingResource)
    }

}
*/
