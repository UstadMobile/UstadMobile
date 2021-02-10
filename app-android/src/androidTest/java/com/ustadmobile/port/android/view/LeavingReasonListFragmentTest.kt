package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.LeavingReasonListScreen
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("LeavingReason screen tests")
class LeavingReasonListFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when LeavingReason clicked then navigate to LeavingReasonDetail")
    @Test
    fun givenLeavingReasonListPresent_whenClickOnLeavingReason_thenShouldNavigateToLeavingReasonDetail() {
        val testEntity = LeavingReason().apply {
            leavingReasonName = "Test Name"
            leavingReasonUid = dbRule.db.clazzDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            LeavingReasonListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            LeavingReasonListScreen{

                recycler{

                    childWith<LeavingReasonListScreen.LeavingReason>{
                        withDescendant { withTag(testEntity.leavingReasonUid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.leaving_reason_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}