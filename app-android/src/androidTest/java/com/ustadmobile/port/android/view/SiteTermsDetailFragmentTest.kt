package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.web.webdriver.Locator
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.port.android.screen.SiteTermsDetailScreen
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord(" WorkspaceTermsDetail screen Test")
class SiteTermsDetailFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given WorkspaceTerms exists when launched then show WorkspaceTerms")
    @Test
    fun givenWorkspaceTermsExists_whenLaunched_thenShouldShowWorkspaceTerms() {
        init{
            val existingTerms = SiteTerms().apply {
                termsHtml = "<div id='terms'>All your bases are belong to us</div>"
                sTermsUid = runBlocking { dbRule.repo.siteTermsDao.insertAsync(this@apply) }
            }

            launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                    fragmentArgs = bundleOf(ARG_ENTITY_UID to existingTerms.sTermsUid)) {
                SiteTermsDetailFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                }
            }
        }.run{
            SiteTermsDetailScreen{
                webView {
                    isDisplayed()
                    withElement(Locator.CSS_SELECTOR, "#terms") {
                        isDisplayed()
                    }
                }
            }
        }

    }

}