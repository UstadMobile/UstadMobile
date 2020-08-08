package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
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
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.port.android.UmViewActions.withItemCount
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.ScenarioIdlingResourceRule
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import it.xabaras.android.espresso.recyclerviewchildactions.RecyclerViewChildActions
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.greaterThan
import org.junit.Before
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
    val dataBindingIdlingResourceRule = ScenarioIdlingResourceRule(DataBindingIdlingResource())

    @JvmField
    @Rule
    val crudIdlingResourceRule = ScenarioIdlingResourceRule(CrudIdlingResource())

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    private val parentEntryUid = 10000L


    @Before
    fun setup() {
        dbRule.insertPersonForActiveUser(Person().apply {
            firstNames = "Test"
            lastName = "User"
            username = "admin"
            admin = true
        })
    }

    @AdbScreenRecord("Given Content entry present when user clicks on an entry then should navigate to entry")
    @Test
    fun givenContentEntryPresent_whenClickOnContentEntry_thenShouldNavigateToContentEntryDetail() {

        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(4,parentEntryUid) }

        launchFragment(bundleOf(ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT))

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))

        onView(withId(R.id.fragment_list_recyclerview)).check(withItemCount(greaterThan(0)))

        onView(withId(R.id.fragment_list_recyclerview))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))


        assertEquals("After clicking on item, it navigates to detail view",
                R.id.content_entry_details_dest, systemImplNavRule.navController.currentDestination?.id)
    }


    @AdbScreenRecord("Given content entry list in a picker mode when folder entry clicked should open it and allow entry selection")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenFolderEntryClicked_thenShouldOpenItAndAllowEntrySelection() {
        val createdEntries = runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(3,parentEntryUid,
                    nonLeafIndexes = mutableListOf(0)) }
        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(6,
                    createdEntries[0].contentEntryUid) }

        launchFragment()

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

        launchFragment()

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))

        onView(withId(R.id.item_createnew_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.item_createnew_layout)).perform(click())

        onView(withId(R.id.bottom_content_option_sheet)).check(matches(isDisplayed()))

    }


    @AdbScreenRecord("Given content entry list in a picker mode when on back pressed while in a folder should show previous parent list")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenOnBackPressedWhileInAFolder_thenShouldGoBackToThePreviousParentFolder() {
        val createdEntries = runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(3,parentEntryUid,
                    nonLeafIndexes = mutableListOf(0)) }
        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(6,
                    createdEntries[0].contentEntryUid) }

        var list2Fragment: ContentEntryList2Fragment? = null

        val scenario = launchFragment()

        scenario.onFragment {
            list2Fragment = it
        }

        onView(withId(R.id.fragment_list_recyclerview)).check(matches(isDisplayed()))

        onView(withId(R.id.fragment_list_recyclerview))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(3, click()))

        //on back navigation: observers on list can't be cancelled on background, so cancel on main thread
        GlobalScope.launch(doorMainDispatcher()) {
            list2Fragment?.onHostBackPressed()
        }

        //wait for back navigation to complete before assertions
        sleep(1000)

        //items on a recycler should be created parent items + 1 for create new content item view
        onView(withId(R.id.fragment_list_recyclerview)).check(matches(hasChildCount(createdEntries.size + 1)))
    }

    private fun launchFragment(bundle:Bundle = bundleOf(ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
            ARG_CONTENT_FILTER to ARG_LIBRARIES_CONTENT,
            ARG_LISTMODE to ListViewMode.PICKER.toString())): FragmentScenario<ContentEntryList2Fragment>{
        return launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundle) {
            ContentEntryList2Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }.withScenarioIdlingResourceRule(dataBindingIdlingResourceRule)
                .withScenarioIdlingResourceRule(crudIdlingResourceRule)
    }
}