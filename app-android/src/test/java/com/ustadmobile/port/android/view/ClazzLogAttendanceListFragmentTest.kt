package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClazzLogAttendanceListFragmentTest {

    @Test
    fun basicTest() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.setGraph(R.navigation.mobile_navigation)

        val clazzLogAttendanceListScenario = launchFragmentInContainer<ClazzLogListAttendanceFragment>(
            bundleOf(UstadView.ARG_FILTER_BY_CLAZZUID to "42")
        )

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))

        clazzLogAttendanceListScenario.onFragment {
            Assert.assertNotEquals(it, null)
        }
    }

}