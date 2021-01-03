package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.port.android.screen.SiteDetailScreen
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord(" WorkspaceDetail screen Test")
class SiteDetailFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given Workspace exists when launched then show Workspace")
    @Test
    fun givenWorkspaceExists_whenLaunched_thenShouldShowWorkspace() {
        init{
            val existingSite = Site().apply {
                siteName = "Test Site"
                siteUid = dbRule.repo.siteDao.insert(this)
            }

            val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(ARG_ENTITY_UID to existingSite.siteUid)) {
                SiteDetailFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }

        }.run{
            SiteDetailScreen{
                recycler {
                    childWith<SiteDetailScreen.SiteDetailItem> {
                        withText("Test Site")
                        isDisplayed()
                    }
                }
            }
        }

    }

}