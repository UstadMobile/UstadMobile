package com.ustadmobile.port.android.view

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.ContentEntryListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Content entry list screen tests")
class ContentEntryList2FragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    private val parentEntryUid = 10000L

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {
        dbRule.insertPersonAndStartSession(Person().apply {
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
            dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(4, parentEntryUid)
        }

        launchFragment(bundleOf(UstadView.ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
                ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION to ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT))

        ContentEntryListScreen {
            recycler {
                isVisible()
                hasSize(5)
                childAt<ContentEntryListScreen.MainItem>(1) {
                    title.hasText("Dummy  entry title 1")
                    click()
                }

                assertEquals("After clicking on item, it navigates to detail view",
                        R.id.content_entry_detail_dest, systemImplNavRule.navController.currentDestination?.id)
            }


        }
    }

    /*
    Disabled 17/Dec/20
    @AdbScreenRecord("Given content entry list in a picker mode when create new content is clicked should show content creation options")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenCreateNewContentClicked_shouldShowContentCreationOptions() {

        init {

            runBlocking {
                dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(4, parentEntryUid)
            }

            launchFragment()

        }.run {

            ContentEntryListScreen {


                recycler {

                    childWith<ContentEntryListScreen.NewItem> {
                        withDescendant { withId(R.id.item_createnew_line1_text) }
                    } perform {
                        newItemTitle{
                            hasText("Add new content")
                            isDisplayed()
                        }
                        newEntryItem{
                            click()
                        }
                    }

                }

                newBottomSheet {
                    isDisplayed()
                }

            }


        }


    }
     */

    @AdbScreenRecord("Given content entry list in a picker mode when folder entry clicked should open it and allow entry selection")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenFolderEntryClicked_thenShouldOpenItAndAllowEntrySelection() {
        val createdEntries = runBlocking {
            dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(3, parentEntryUid,
                    nonLeafIndexes = mutableListOf(0))
        }
        runBlocking {
            dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(6,
                    createdEntries[0].contentEntryUid)
        }

        launchFragment()


        ContentEntryListScreen {

            recycler {

                isDisplayed()
                emptyChildAt(3) {
                    click()
                }
                childAt<ContentEntryListScreen.MainItem>(2) {
                    selectButton {
                        click()
                    }
                }

            }
        }
    }

    @AdbScreenRecord("Given content entry list in a picker mode when on back pressed while in a folder should show previous parent list")
    @Test
    fun givenContentEntryListOpenedInPickerMode_whenOnBackPressedWhileInAFolder_thenShouldGoBackToThePreviousParentFolder() {
        val createdEntries = runBlocking {
            dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(3, parentEntryUid,
                    nonLeafIndexes = mutableListOf(0))
        }
        runBlocking {
            dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(6,
                    createdEntries[0].contentEntryUid)
        }

        var list2Fragment: ContentEntryList2Fragment? = null

        val scenario = launchFragment()

        scenario.onFragment {
            list2Fragment = it
        }

        ContentEntryListScreen {

            recycler {

                isDisplayed()
                emptyChildAt(3) {
                    click()
                }
                //on back navigation: observers on list can't be cancelled on background, so cancel on main thread
                GlobalScope.launch(doorMainDispatcher()) {
                    list2Fragment?.onHostBackPressed()
                }
                hasSize(createdEntries.size + 2)

            }

        }
    }


    private fun launchFragment(bundle: Bundle = bundleOf(UstadView.ARG_PARENT_ENTRY_UID to parentEntryUid.toString(),
            ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION to ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT,
            UstadView.ARG_LISTMODE to ListViewMode.PICKER.toString())): FragmentScenario<ContentEntryList2Fragment> {
        return launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundle) {
            ContentEntryList2Fragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }
    }

}
