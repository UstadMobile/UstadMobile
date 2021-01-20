package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.port.android.screen.ReportTemplateScreen
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ReportTemplate screen tests")
class ReportTemplateFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when ReportTemplate clicked then navigate to ReportEditScreen")
    @Test
    fun givenReportTemplateListPresent_whenClickOnReportTemplate_thenShouldNavigateToReportEdit() {
        val testEntity = Report().apply {
            reportTitle = "Test Name"
            reportDescription = "Test Description"
            isTemplate = true
            reportUid = dbRule.repo.reportDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ReportTemplateFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ReportTemplateScreen{

                recycler{

                    childWith<ReportTemplateScreen.ReportTemplate>{
                        withDescendant { withText(testEntity.reportTitle!!) }
                    }perform {
                        title {
                            hasText("Test Name")
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.report_edit_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}