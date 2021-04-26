package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ClazzAssignmentDetailStudentProgressListScreen
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzAssignmentWithMetrics screen tests")
class ClazzAssignmentDetailStudentProgressListOverviewFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when ClazzAssignmentWithMetrics clicked then navigate to ClazzAssignmentWithMetricsDetail")
    @Test
    fun givenClazzAssignmentWithMetricsListPresent_whenClickOnClazzAssignmentWithMetrics_thenShouldNavigateToClazzAssignmentWithMetricsDetail() {
        val testEntity = ClazzAssignmentWithMetrics().apply {
            clazzAssignmentWithMetricsName = "Test Name"
            clazzAssignmentWithMetricsUid = dbRule.db.clazzDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ClazzAssignmentDetailStudentProgressListOverviewFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzAssignmentDetailStudentProgressListScreen{

                recycler{

                    childWith<ClazzAssignmentDetailStudentProgressListScreen.ClazzAssignmentWithMetrics>{
                        withDescendant { withTag(testEntity.clazzAssignmentWithMetricsUid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.clazz_assignment_with_metrics_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}