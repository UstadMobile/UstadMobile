package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.port.android.screen.ClazzLogListAttendanceScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.insertClazzLogs
import com.ustadmobile.util.test.ext.insertTestClazzAndMembers
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions.Companion.childOfViewAtPositionWithMatcher
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@AdbScreenRecord("Attendance list screen tests")
@RunWith(AndroidJUnit4::class)
class ClazzLogListAttendanceFragmentTest : TestCase() {

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true,
            account = UmAccount(7L, "bond", "", "http://localhost"))

    @After
    fun tearDown() {
        UstadMobileSystemImpl.instance.navController = null
    }

    @AdbScreenRecord("Given class is scheduled, when user clicks on a day then should navigate to record attendance")
    @Test
    fun givenClazzUidWithExistingLog_whenClickOnClazzLog_thenShouldNavigateToClazzEditAttendance() {

        init{

            val clazzAndMembers = runBlocking { dbRule.db.insertTestClazzAndMembers(5) }
            ClazzLog(0L, clazzAndMembers.clazz.clazzUid, System.currentTimeMillis(), 0L).apply {
                clazzLogUid = dbRule.db.clazzLogDao.insert(this)
            }

            launchFragmentInContainer(
                    bundleOf(UstadView.ARG_FILTER_BY_CLAZZUID to clazzAndMembers.clazz.clazzUid.toString()),
                    themeResId = R.style.UmTheme_App) {
                ClazzLogListAttendanceFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }


        }.run {

            ClazzLogListAttendanceScreen {

                recycler {
                    isDisplayed()
                    childAt<ClazzLogListAttendanceScreen.MainItem>(1) {
                        click()
                    }
                }

            }

            flakySafely {
                Assert.assertEquals("After clicking on attendance log, fragment goes to attendance view",
                        R.id.clazz_log_edit_attendance_dest, systemImplNavRule.navController.currentDestination?.id)
            }

        }


    }

    @AdbScreenRecord("Given attendance has been recorded for past days, graph should be displayed to user")
    @Test
    fun givenListOfRecordedClazzLogs_whenCreated_thenGraphShouldShow() {

        init {

            systemImplNavRule.navController.setGraph(R.navigation.mobile_navigation)

            val testClazz = Clazz("Test Clazz").apply {
                clazzTimeZone = "Asia/Dubai"
                clazzUid = dbRule.db.clazzDao.insert(this)
            }

            val oneDayInMs = (1000 * 60 * 60 * 24)
            val oneWeekInMs = (oneDayInMs * 7)
            val timeNow = System.currentTimeMillis()
            val timeRange = (timeNow - oneWeekInMs) to timeNow

            val numInClazz = 10
            runBlocking {
                dbRule.db.insertClazzLogs(testClazz.clazzUid, 5) { index ->
                    ClazzLog().apply {
                        logDate = timeRange.first + (index * oneDayInMs) + (1000 * 60 * 60 * 8)
                        clazzLogNumAbsent = if (index.rem(2) == 0) 2 else 4
                        clazzLogNumPartial = if (index.rem(2) == 0) 1 else 2
                        clazzLogNumPresent = numInClazz - (clazzLogNumAbsent + clazzLogNumPartial)
                        clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
                    }
                }
            }

            launchFragmentInContainer(
                    bundleOf(UstadView.ARG_FILTER_BY_CLAZZUID to testClazz.clazzUid.toString()),
                    themeResId = R.style.UmTheme_App
            ) {
                ClazzLogListAttendanceFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run {

            ClazzLogListAttendanceScreen {
                recycler {
                    childAt<ClazzLogListAttendanceScreen.MainItem>(0){
                        chartGraph{
                            isDisplayed()
                        }
                    }
                }
            }


        }



    }


}