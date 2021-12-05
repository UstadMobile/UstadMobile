package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import io.github.kakaocup.kakao.common.views.KView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.ContentEntryEditScreen
import com.ustadmobile.port.android.screen.ContentEntryListScreen
import com.ustadmobile.port.android.screen.MainScreen
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.util.test.ext.insertContentEntryWithParentChildJoinAndMostRecentContainer
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("ContentEntry end-to-end test")
class ContentEntryEndtoEnd : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("""Given existing entry list when user has permission to 
        update the folder name then edit name, update and check entry title got updated""")
    @Test
    fun givenExistingEntryAndUserHasPermission_whenEditEntryAndUpdate_thenCheckEntryTitleUpdated() {

        var entryToEdit: ContentEntry?
        init {

            runBlocking {

                dbRule.insertPersonAndStartSession(Person().apply {
                    firstNames = "Bob"
                    lastName = "Jones"
                    admin = true
                })
                val entry = dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(1, -4103245208651563007L, mutableListOf(0))
                entryToEdit = entry[0]
                dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(4, entryToEdit!!.contentEntryUid)
            }

            launchActivity<MainActivity>()

        }.run {

            MainScreen {

                bottomNav {
                    setSelectedItem(R.id.content_entry_list_dest)
                }
            }

            // Click on the folder, go to next screen and click on Edit Menu
            ContentEntryListScreen{

                recycler{

                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant { withText("Dummy folder title 1") }
                    }perform {
                        title{
                            click()
                        }
                    }

                    hasSize(5)

                    openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

                    KView {
                        withText("Edit")
                    } perform {
                        click()
                    }
                }
            }

            // Update the title for the entry
            ContentEntryEditScreen {

                entryTitleTextInput {
                    edit {
                        clearText()
                        replaceText("new Title")
                    }
                }

            }

            MainScreen {
                menuDone.click()
            }

            // go back to the previous screen and check the title got changed
            ContentEntryListScreen{

                pressBack()

                recycler{

                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant { withText("new Title") }
                    }perform {
                        title{
                            isDisplayed()
                            hasText("new Title")
                        }
                    }

                }
            }
        }
    }

    @AdbScreenRecord("given a list of items when long press on item then hide the item when visibility button is hit")
    @Test
    fun givenListOfEntries_whenUserLongPressAndSelectHideItem_thenEntriesIsHidden(){

        init {

            runBlocking {
                dbRule.insertPersonAndStartSession(Person().apply {
                    firstNames = "Test"
                    lastName = "User"
                    username = "admin"
                    admin = true
                })
                dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(3, -4103245208651563007L, mutableListOf(0))
            }

            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                it.putExtra(UstadView.ARG_NEXT,
                        "${ContentEntryList2View.VIEW_NAME}?${UstadView.ARG_PARENT_ENTRY_UID}=-4103245208651563007" +
                                "&${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION}=${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT}")
            }
            launchActivity<MainActivity>(launchIntent)

        }.run {

            ContentEntryListScreen{

                recycler{

                    flakySafely {
                        hasSize(4)
                    }


                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant { withText("Dummy folder title 1") }
                    }perform {
                        title{
                            longClick()
                        }
                    }

                    KView{
                        withContentDescription("Hide")
                    }perform {
                        click()
                    }

                    flakySafely {
                        hasSize(3)
                    }

                }


            }
        }
    }

    //@AdbScreenRecord("given a list of items when menu option show hidden items selected then show all items in list")
    //@Test
    fun givenListOfEntry_whenMenuOptionShowHiddenItemsSelected_thenShowAllItemsInList(){

        init {

            runBlocking {
                dbRule.insertPersonAndStartSession(Person().apply {
                    firstNames = "Test"
                    lastName = "User"
                    username = "admin"
                    admin = true
                })
                val list = dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(4, -4103245208651563007L)
                list.forEach{
                    it.ceInactive = true
                }
                dbRule.repo.contentEntryDao.updateList(list)
            }
            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                it.putExtra(UstadView.ARG_NEXT,
                        "${ContentEntryList2View.VIEW_NAME}?${UstadView.ARG_PARENT_ENTRY_UID}=-4103245208651563007" +
                                "&${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION}=${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT}")
            }

            launchActivity<MainActivity>(launchIntent)

        }.run {

            ContentEntryListScreen{

                recycler{

                    hasSize(1)

                    openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

                    KView {
                        withText("Show hidden items")
                    } perform {
                        click()
                    }

                    hasSize(4)

                }

            }

        }

    }

    @AdbScreenRecord("given a list of items when menu option show hidden items selected then show all items in list")
    @Test
    fun givenListOfEntry_whenUserSelectsHiddenAndUnHideItems_thenMenuOptionsChangesToUnHide(){

        init {

            runBlocking {
                dbRule.insertPersonAndStartSession(Person().apply {
                    firstNames = "Test"
                    lastName = "User"
                    username = "admin"
                    admin = true
                })
                val list = dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(4, -4103245208651563007L, mutableListOf(0))
                list[0].ceInactive = true
                dbRule.repo.contentEntryDao.updateList(list)
            }
            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                it.putExtra(UstadView.ARG_NEXT,
                        "${ContentEntryList2View.VIEW_NAME}?${UstadView.ARG_PARENT_ENTRY_UID}=-4103245208651563007" +
                                "&${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION}=${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT}")
            }

            launchActivity<MainActivity>(launchIntent)

        }.run {

            ContentEntryListScreen{

                recycler{

                    hasSize(4)

                    openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)

                    KView {
                        withText("Show hidden items")
                    } perform {
                        click()
                    }

                    hasSize(5)

                    childWith<ContentEntryListScreen.MainItem> {
                         withDescendant {
                             withText("Dummy folder title 1") }
                    }perform {
                        title{
                            longClick()
                        }
                    }

                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant {
                            withText("Dummy  entry title 2") }
                    }perform {
                        title{
                            click()
                        }
                    }

                    KView{
                        withContentDescription("Unhide")
                    }perform {
                        click()
                    }

                    hasSize(5)

                }

            }

        }

    }

    //Disabled 12/Nov
    //@Test
    fun givenListOfEntries_whenUserMovesEntriesToAnotherFolder_thenMoveToNewFolder(){

        init {

            runBlocking {
                dbRule.insertPersonAndStartSession(Person().apply {
                    firstNames = "Test"
                    lastName = "User"
                    username = "admin"
                    admin = true
                })

                val oneList = dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(3, -4103245208651563007L, mutableListOf(0,1))
                dbRule.repo.insertContentEntryWithParentChildJoinAndMostRecentContainer(3, oneList[0].contentEntryUid)

            }

            val context = ApplicationProvider.getApplicationContext<Context>()
            val launchIntent = Intent(context, MainActivity::class.java).also {
                it.putExtra(UstadView.ARG_NEXT,
                        "${ContentEntryList2View.VIEW_NAME}?${UstadView.ARG_PARENT_ENTRY_UID}=-4103245208651563007" +
                                "&${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION}=${ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT}")
            }

            launchActivity<MainActivity>(launchIntent)

        }.run {

            ContentEntryListScreen{


                recycler{

                    hasSize(3)

                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant {
                            withText("Dummy folder title 1") }
                    }perform {
                        title{
                            click()
                        }
                    }

                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant {
                            withText("Dummy  entry title 2") }
                    }perform {
                        title{
                            longClick()
                        }
                    }

                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant {
                            withText("Dummy  entry title 3") }
                    }perform {
                        title{
                            click()
                        }
                    }

                    KView{
                        withContentDescription("Move")
                    }perform {
                        click()
                    }

                    // includes add new content
                    hasSize(3)

                    childWith<ContentEntryListScreen.MainItem> {
                        withDescendant {
                            withText("Dummy folder title 2") }
                    }perform {
                        selectButton{
                            click()
                        }
                    }

                    hasSize(1)

                    KView{
                        withText("Open Folder")
                    }perform {
                        click()
                    }

                    hasSize(2)

                }


            }



        }


    }



}