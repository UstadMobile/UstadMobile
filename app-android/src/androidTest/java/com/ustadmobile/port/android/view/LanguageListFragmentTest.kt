package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.port.android.screen.LanguageListScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Language screen tests")
class LanguageListFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when LeavingReason clicked then navigate to LeavingReasonEdit")
    @Test
    fun givenLeavingReasonListPresent_whenClickOnLeavingReason_thenShouldNavigateToLeavingReasonEdit() {
        val testEntity = Language().apply {
            name = "English"
            iso_639_1_standard = "en"
            iso_639_2_standard = "eng"
            langUid = dbRule.repo.languageDao.insert(this)
        }



        init{

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf()) {
                LanguageListFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{

            LanguageListScreen{

                recycler{

                    childWith<LanguageListScreen.Language>{
                        withDescendant { withText(testEntity.name!!) }
                    }perform {
                        name {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to edit view",
                            R.id.language_edit_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}