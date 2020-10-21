package com.ustadmobile.port.android.view

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.agoda.kakao.common.views.KView
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
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
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

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

                dbRule.insertPersonForActiveUser(Person().apply {
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
                    setSelectedItem(R.id.home_content_dest)
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

                    hasSize(4)

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

                KView {
                    withId(R.id.menu_done)
                } perform {
                    click()
                }

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

}