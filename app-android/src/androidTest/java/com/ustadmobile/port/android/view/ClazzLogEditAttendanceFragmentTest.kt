package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.util.test.ext.insertClazzAndClazzMembers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ClazzLogEditAttendanceFragmentTest : UstadFragmentTest() {

    lateinit var recyclerViewIdlingResource: RecyclerViewIdlingResource

    @Before
    fun setup() {
        setupDbWithAccount()
        setupNavController()
        recyclerViewIdlingResource = RecyclerViewIdlingResource(null)
    }

    @Test
    fun givenExistingClazzWithMembesAndClazzLog_whenStudentAttendanceRecorded_thenShouldBeSavedToDatabase() {
        IdlingRegistry.getInstance().register(recyclerViewIdlingResource)

        val clazzAndMembers = runBlocking { db.insertClazzAndClazzMembers(5) }

        val clazzLog = ClazzLog(0L, clazzAndMembers.first.clazzUid, System.currentTimeMillis(), 0L).apply {
            clazzLogUid = db.clazzLogDao.insert(this)
        }

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogEditAttendanceFragment>(
            bundleOf(UstadView.Companion.ARG_ENTITY_UID to clazzLog.clazzLogUid.toString()), themeResId = R.style.Theme_UstadTheme
        )

        clazzLogAttendanceListScenario.onFragment {
            recyclerViewIdlingResource.recyclerView = it.mBinding!!.clazzLogEditRecyclerView
        }


        onView(withId(R.id.clazz_log_edit_recycler_view)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
                    click()))
    }

}