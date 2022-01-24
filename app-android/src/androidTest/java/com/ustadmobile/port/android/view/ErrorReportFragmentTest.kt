package com.ustadmobile.port.android.view

import android.content.Context
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.view.ErrorReportView
import com.ustadmobile.port.android.screen.ErrorReportScreen
import com.ustadmobile.port.android.util.ext.waitUntil2Blocking
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ErrorReportFragmentTest : TestCase() {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @Before
    fun setup() {

    }

    @Test
    fun givenErrorProvidedByArgs_whenCreated_thenShouldSaveToDb() {
        lateinit var fragmentScenario: FragmentScenario<ErrorReportFragment>

        val context = ApplicationProvider.getApplicationContext<Context>()

        init {
            val args = mapOf(
                ErrorReportView.ARG_ERR_CODE to "42",
                ErrorReportView.ARG_MESSAGE to "The meaning of life")

            fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = args.toBundle()) {
                ErrorReportFragment().also {
                    it.installNavController(systemImplNavRule.navController)
                    systemImplNavRule.navController.navigate(R.id.error_report_dest,
                        args.toBundle())
                }
            }
        }.run {
            ErrorReportScreen {
                errorCodeText {
                    containsText(context.getString(R.string.error_code, "42"))
                }
            }

            val errorReportList = dbRule.db.waitUntil2Blocking(setOf("ErrorReport"), 5000,
                {dbRule.db.errorReportDao.findByErrorCode(42)}) { !it.isNullOrEmpty() }
            Assert.assertEquals("Report list contains expected item", 42,
                errorReportList?.first()?.errorCode)
        }
    }

}