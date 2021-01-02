package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.port.android.screen.WorkspaceTermsDetailScreen
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord(" WorkspaceTermsDetail screen Test")
class WorkspaceTermsDetailFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given WorkspaceTerms exists when launched then show WorkspaceTerms")
    @Test
    fun givenWorkspaceTermsExists_whenLaunched_thenShouldShowWorkspaceTerms() {
        val existingClazz = SiteTerms().apply {
            workspaceTermsName = "Test WorkspaceTerms"
            workspaceTermsUid = dbRule.db.siteTermsDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(ARG_ENTITY_UID to existingClazz.clazzUid)) {
            WorkspaceTermsDetailFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            WorkspaceTermsDetailScreen{

                title{
                    isDisplayed()
                    hasText("Test WorkspaceTerms")
                }
            }


        }

    }

}