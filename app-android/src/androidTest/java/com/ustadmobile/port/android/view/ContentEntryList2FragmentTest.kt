package com.ustadmobile.port.android.view

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_LISTMODE
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Content entry list screen tests")
class ContentEntryList2FragmentTest  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule()

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("Given Content entry present when user clicks on an entry then should navigate to entry")
    @Test
    fun givenContentEntryPresent_whenClickOnContentEntry_thenShouldNavigateToContentEntryDetail() {
        val parentEntryUid = 10000L

        val contentEntries = runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(4,parentEntryUid) }

        val fragmentScenario = launchFragmentInContainer<ContentEntryList2Fragment>(
                bundleOf(ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                        ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT),
                themeResId = R.style.UmTheme_App
        ).withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
        }

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(hasChildCount(contentEntries.size)))

        onView(withId(R.id.fragment_list_recyclerview))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))


        assertEquals("After clicking on item, it navigates to detail view",
                R.id.content_entry_details_dest, systemImplNavRule.navController.currentDestination?.id)
    }


    @AdbScreenRecord("Given content entry list when opened in picker mode should show select button")
    @Test
    fun givenContentEntryList_whenOpenedInPickerMode_thenShowSelectEntryButton() {
        val parentEntryUid = 10000L
        val context = ApplicationProvider.getApplicationContext<Application>()
        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(2,parentEntryUid) }

        val fragmentScenario = launchFragmentInContainer<ContentEntryList2Fragment>(
                bundleOf(ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                        ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT,
                        ARG_LISTMODE to ListViewMode.PICKER.toString()),
                themeResId = R.style.UmTheme_App
        ).withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController)
        }

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(
                hasDescendant(withText(String.format(context.getString(R.string.select_item), "")))))
    }
}