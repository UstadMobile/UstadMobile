package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.ContentEntryListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.test.rules.withScenarioIdlingResourceRule
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("KAS Content entry list screen tests")
class ContentEntryList2FragmentKasTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    private val parentEntryUid = 10000L

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

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

        GlobalScope.launch {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(4, parentEntryUid)
        }

        launchFragment(bundleOf(UstadView.ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ContentEntryList2View.ARG_CONTENT_FILTER to ContentEntryList2View.ARG_LIBRARIES_CONTENT))

        ContentEntryListScreen {
            recycler {
                isVisible()
                hasSize(4)
                childAt<ContentEntryListScreen.MainItem>(0) {
                    title.hasText("Dummy  entry title 1")
                    click()
                }

                assertEquals("After clicking on item, it navigates to detail view",
                        R.id.content_entry_details_dest, systemImplNavRule.navController.currentDestination?.id)
            }


        }
    }

    @AdbScreenRecord("Given content entry list in a picker mode when create new content is clicked should show content creation options")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenCreateNewContentClicked_shouldShowContentCreationOptions() {
        runBlocking {
            dbRule.db.insertContentEntryWithParentChildJoinAndMostRecentContainer(4, parentEntryUid)
        }

        launchFragment()

        ContentEntryListScreen {

            newEntryItem {
                isDisplayed()
                click()
            }

            newBottomSheet {
                isDisplayed()
            }
        }


    }


    private fun launchFragment(bundle: Bundle = bundleOf(UstadView.ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
            ContentEntryList2View.ARG_CONTENT_FILTER to ContentEntryList2View.ARG_LIBRARIES_CONTENT,
            UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())): FragmentScenario<ContentEntryList2Fragment> {
        return launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundle) {
            ContentEntryList2Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }
    }

}