package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.insertTestClazzAndMembers
import com.ustadmobile.util.test.ext.insertClazzLogs
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions.Companion.childOfViewAtPositionWithMatcher
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith

@AdbScreenRecord("Attendance list screen tests")
@RunWith(AndroidJUnit4::class)
class ClazzLogListAttendanceFragmentTest {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    lateinit var navController: NavController

    private lateinit var db: UmAppDatabase

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null)
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        UstadMobileSystemImpl.instance.navController = navController

        val activeAccount = UmAccount(7L, "bond", "", "http://localhost")
        UmAccountManager.setActiveAccount(activeAccount, ApplicationProvider.getApplicationContext())

        db = UmAccountManager.getActiveDatabase(ApplicationProvider.getApplicationContext())
        db.clearAllTables()
    }

    @After
    fun tearDown(){
        UstadMobileSystemImpl.instance.navController = null
    }

    @AdbScreenRecord("Given class is scheduled, when user clicks on a day then should navigate to record attendance")
    @Test
    fun givenClazzUidWithExistingLog_whenClickOnClazzLog_thenShouldNavigateToClazzEditAttendance() {
        recyclerViewIdlingResource.minItemCount = 2 //as the chart header item is one
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val clazzAndMembers = runBlocking { db.insertTestClazzAndMembers(5) }
        val clazzLog = ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogListAttendanceFragment>(
            bundleOf(UstadView.ARG_FILTER_BY_CLAZZUID to clazzAndMembers.clazz.clazzUid.toString()),
                themeResId = R.style.Theme_UstadTheme
        ).withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        clazzLogAttendanceListScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mDataBinding!!.fragmentListRecyclerview
        }

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_list_recyclerview))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        Assert.assertEquals("After clicking on attendance log, fragment goes to attendance view",
                navController.currentDestination?.id, R.id.clazz_log_edit_attendance_dest)
    }

    @AdbScreenRecord("Given attendance has been recorded for past days, graph should be displayed to user")
    @Test
    fun givenListOfRecordedClazzLogs_whenCreated_thenGraphShouldShow() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val testClazz = Clazz("Test Clazz").apply {
            clazzTimeZone = "Asia/Dubai"
            clazzUid = db.clazzDao.insert(this)
        }

        val oneDayInMs = (1000 * 60 * 60 * 24)
        val oneWeekInMs = (oneDayInMs * 7)
        val timeNow = System.currentTimeMillis()
        val timeRange = (timeNow - oneWeekInMs) to timeNow

        val numInClazz = 10
        val clazzLogs = runBlocking { db.insertClazzLogs(testClazz.clazzUid, 5) {index ->
            ClazzLog().apply {
                logDate = timeRange.first + (index * oneDayInMs) + (1000 * 60 * 60 * 8)
                clazzLogNumAbsent = if(index.rem(2) == 0) 2 else 4
                clazzLogNumPresent = numInClazz - clazzLogNumAbsent
                clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
            }
        } }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogListAttendanceFragment>(
                bundleOf(UstadView.ARG_FILTER_BY_CLAZZUID to testClazz.clazzUid.toString()),
                themeResId = R.style.Theme_UstadTheme
        ).withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        clazzLogAttendanceListScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
            recyclerViewIdlingResource.recyclerView = it.mDataBinding!!.fragmentListRecyclerview
        }

        onView(withId(R.id.fragment_list_recyclerview)).check(
                matches(childOfViewAtPositionWithMatcher(R.id.chart, 0,
                        isDisplayed())))
    }


}