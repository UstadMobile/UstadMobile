package com.ustadmobile.port.android.view

import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.test.port.android.util.clickOptionMenu
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test


@AdbScreenRecord("Report filter edit screen tests")
class ReportFilterEditFragmentTest: TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()


    @AdbScreenRecord("with no report present, fill all the fields and navigate to detail")
    @Test
    fun givenNoReportFilterPresentYet_whenPersonGenderFilledIn_thenShouldNavigateBackToReportEditScreen() {
        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App) {
            ReportFilterEditFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            fragmentScenario.clickOptionMenu(R.id.menu_done)

            Assert.assertEquals("After finishing edit report filter, it navigates to report edit view",
                    R.id.report_edit_dest, systemImplNavRule.navController.currentDestination?.id)
        }

    }

}