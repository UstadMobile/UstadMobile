package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.port.android.util.letOnFragment
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import com.ustadmobile.port.android.screen.ClazzAssignmentDetailOverviewScreen
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord(" ClazzAssignmentDetail screen Test")
class ClazzAssignmentDetailOverviewFragmentTest : TestCase(){

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("given ClazzAssignment exists when launched then show ClazzAssignment")
    @Test
    fun givenClazzAssignmentExists_whenLaunched_thenShouldShowClazzAssignment() {
        val existingClazz = ClazzAssignment().apply {
            clazzAssignmentName = "Test ClazzAssignment"
            clazzAssignmentUid = dbRule.db.clazzAssignmentDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf(ARG_ENTITY_UID to existingClazz.clazzUid)) {
            ClazzAssignmentDetailOverviewFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzAssignmentDetailOverviewScreen{

                title{
                    isDisplayed()
                    hasText("Test ClazzAssignment")
                }
            }


        }

    }

}