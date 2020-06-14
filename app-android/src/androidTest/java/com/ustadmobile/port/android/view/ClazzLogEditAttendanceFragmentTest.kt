package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import com.nhaarman.mockitokotlin2.mock
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.insertTestClazzAndMembers
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule()

    @Before
    fun setup() {
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null, 3)
    }

    private fun markStudent(studentIndex: Int, buttonId: Int) {
        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(studentIndex,
                        RecyclerViewChildActions.actionOnChild(click(), buttonId)))
    }

    private fun waitForAttendanceToSave(clazzLogUid: Long) {
        runBlocking(Dispatchers.Main) {
            waitForLiveData(dbRule.db.clazzLogDao.findByUidLive(clazzLogUid), 5000) {
                it?.clazzLogStatusFlag == ClazzLog.STATUS_RECORDED
            }
        }
    }

    @Test
    fun givenExistingClazzWithMembesAndClazzLog_whenMixedStudentAttendanceRecorded_thenShouldBeSavedToDatabase() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzAndMembers = runBlocking { dbRule.db.insertTestClazzAndMembers(5) }

        val clazzLog = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = dbRule.db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogEditAttendanceFragment>(
            fragmentArgs = bundleOf(UstadView.Companion.ARG_ENTITY_UID to clazzLog.clazzLogUid.toString()), themeResId = R.style.Theme_UstadTheme) {
            ClazzLogEditAttendanceFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

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

        clazzLogAttendanceListScenario.onFragment {
            it.onOptionsItemSelected(mock {
                on { itemId }.thenReturn(R.id.menu_done)
            })
        }

        waitForAttendanceToSave(clazzLog.clazzLogUid)


        val clazzLogAttendanceRecords = dbRule.db.clazzLogAttendanceRecordDao.findByClazzLogUid(clazzLog.clazzLogUid)
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
    }


    @Test
    fun givenExistingClazzLog_whenClickMarkAll_thenShouldBeSavedToDatabase() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzAndMembers = runBlocking { dbRule.db.insertTestClazzAndMembers(5) }

        val clazzLog = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = dbRule.db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogEditAttendanceFragment>(
                bundleOf(UstadView.Companion.ARG_ENTITY_UID to clazzLog.clazzLogUid.toString()), themeResId = R.style.Theme_UstadTheme
        ).withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        clazzLogAttendanceListScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.clazzLogEditRecyclerView
        }

        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(R.string.mark_all_present)),
                    click()))

        waitForAttendanceToSave(clazzLog.clazzLogUid)

        val clazzLogAttendanceRecords = dbRule.db.clazzLogAttendanceRecordDao.findByClazzLogUid(
                clazzLog.clazzLogUid)
        Assert.assertTrue("All clazz logs are marked as attended",
                clazzLogAttendanceRecords.all { it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED})

    }



}