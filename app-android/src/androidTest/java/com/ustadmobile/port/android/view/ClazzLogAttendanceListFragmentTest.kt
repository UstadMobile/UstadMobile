package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
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
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.util.test.ext.insertClazzAndClazzMembers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClazzLogAttendanceListFragmentTest {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    lateinit var navController: NavController

    private lateinit var db: UmAppDatabase

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

    @Test
    fun givenClazzUidWithExistingLog_whenClickOnClazzLog_thenShouldNavigateToClazzEditAttendance() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)
        navController.setGraph(R.navigation.mobile_navigation)

        val clazzAndMembers = runBlocking { db.insertClazzAndClazzMembers(5) }
        val clazzLog = ClazzLog(0L, clazzAndMembers.first.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogListAttendanceFragment>(
            bundleOf(UstadView.ARG_FILTER_BY_CLAZZUID to clazzAndMembers.first.clazzUid.toString())
        )

        clazzLogAttendanceListScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mDataBinding!!.fragmentListRecyclerview
        }

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_list_recyclerview))
                .perform(
                        RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        Assert.assertEquals("After clicking on attendance log, fragment goes to attendance view",
                navController.currentDestination?.id, R.id.clazz_log_edit_attendance_dest)
    }

}