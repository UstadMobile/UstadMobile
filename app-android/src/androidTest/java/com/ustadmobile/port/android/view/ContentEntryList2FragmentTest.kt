package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_LIBRARIES_CONTENT
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView.Companion.ARG_LISTMODE
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.test.rules.DataBindingIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withDataBindingIdlingResource
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.lang.Thread.sleep

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

    private val parentEntryUid = 10000L

    @AdbScreenRecord("Given Content entry present when user clicks on an entry then should navigate to entry")
    @Test
    fun givenContentEntryPresent_whenClickOnContentEntry_thenShouldNavigateToContentEntryDetail() {
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


    @AdbScreenRecord("Given content entry list in a picker mode when folder entry clicked should open it and allow entry selection")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenFolderEntryClicked_thenShouldOpenItAndAllowEntrySelection() {
        val parentEntryUid = 10000L
        val createdEntries = runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(3,parentEntryUid,
                    nonLeafIndexes = mutableListOf(0)) }
        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(6,
                    createdEntries[0].contentEntryUid) }

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

        onView(withId(R.id.fragment_list_recyclerview))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(3, click()))

        onView(withId(R.id.fragment_list_recyclerview)).perform(
                actionOnItemAtPosition<RecyclerView.ViewHolder>(1,
                        RecyclerViewChildActions.actionOnChild(click(), R.id.content_entry_select_btn)))

    }

    @AdbScreenRecord("Given content entry list in a picker mode when create new content is clicked should show content creation options")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenCreateNewContentClicked_shouldShowContentCreationOptions(){
        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(3,parentEntryUid) }

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

        onView(withId(R.id.item_createnew_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.item_createnew_layout)).perform(click())

        onView(withId(R.id.bottom_content_option_sheet)).check(matches(isDisplayed()))

    }


    @Test
    fun givenContentEntryListOpenedInPickerMode_whenOnBackPressedWhileInAFolder_thenShouldGoBackToThePreviousParentFolder() {
        val parentEntryUid = 10000L
        val createdEntries = runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(3,parentEntryUid,
                    nonLeafIndexes = mutableListOf(0)) }
        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(6,
                    createdEntries[0].contentEntryUid) }

        var list2Fragment: ContentEntryList2Fragment? = null
        with(launchFragmentInContainer<ContentEntryList2Fragment>(
                        bundleOf(ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                                ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT,
                                ARG_LISTMODE to ListViewMode.PICKER.toString()),
                        themeResId = R.style.UmTheme_App
                )
        ){ onFragment { run {
                list2Fragment = it
                Navigation.setViewNavController(it.requireView(), systemImplNavRule.navController) }
        } }.withDataBindingIdlingResource(dataBindingIdlingResourceRule)

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))

        onView(withId(R.id.fragment_list_recyclerview))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(3, click()))

        //observers on list can't be cancelled on background, so cancel on main thread
        GlobalScope.launch(doorMainDispatcher()) {
            list2Fragment?.handleOnBackPressed()
        }
        
        sleep(1000)

        //items on a recycler should be created parent items + 1 for create new content item view
        onView(withId(R.id.fragment_list_recyclerview)).check(matches(hasChildCount(createdEntries.size + 1)))
    }


}