package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.port.android.screen.ContentEntryEditScreen
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.test.port.android.util.waitUntilWithFragmentScenario
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Content entry edit screen tests")
class ContentEntryEdit2FragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val adbScreenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given folder does not yet exist, when user fills in form for new folder, should be saved to database")
    @Test
    fun givenNoFolderYet_whenFormFilledInAndSaveClicked_thenShouldSaveToDatabase() {
        val dummyTitle = "New Folder Entry"

        val fragmentScenario = launchFragmentInContainer(
                fragmentArgs = bundleOf(ARG_LEAF to false.toString(),
                        ARG_PARENT_ENTRY_UID to 10000L.toString()),
                themeResId = R.style.UmTheme_App) {
            ContentEntryEdit2Fragment().also {
                it.installNavController(systemImplNavRule.navController,
                        initialDestId = R.id.content_entry_edit_dest)
            }
        }

        init {
        }.run {

            ContentEntryEditScreen {

                importButton {
                    isNotDisplayed()
                }
                storageOption {
                    isNotDisplayed()
                }

                val currentEntity = fragmentScenario.letOnFragment { it.entity }
                val formVals = ContentEntryWithLanguage().apply {
                    title = dummyTitle
                    description = "Description"
                }

                formVals.title?.takeIf { it != currentEntity?.title }?.also {
                    entryTitleTextInput {
                        edit {
                            clearText()
                            typeText(it)
                        }
                    }
                }
                formVals.description?.takeIf { it != currentEntity?.description }?.also {
                    descTextInput {
                        edit {
                            clearText()
                            typeText(it)
                        }
                    }
                }

                fragmentScenario.clickOptionMenu(R.id.menu_done)

                val entries = dbRule.db.contentEntryDao.findAllLive().waitUntilWithFragmentScenario(fragmentScenario) {
                    it.isNotEmpty()
                }

                Assert.assertEquals("Entry's data set", dummyTitle, entries?.first()?.title)
            }


        }

    }


}
