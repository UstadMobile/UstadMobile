package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ClazzAssignmentDetailStudentProgressScreen
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzAssignment screen tests")
class ClazzAssignmentDetailStudentProgressFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when ClazzAssignment clicked then navigate to ClazzAssignmentDetail")
    @Test
    fun givenClazzAssignmentListPresent_whenClickOnClazzAssignment_thenShouldNavigateToClazzAssignmentDetail() {
        val testEntity = ClazzAssignment().apply {
            clazzAssignmentName = "Test Name"
            caUid = dbRule.db.clazzDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ClazzAssignmentDetailStudentProgressFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzAssignmentDetailStudentProgressScreen{

                recycler{

                    childWith<ClazzAssignmentDetailStudentProgressScreen.ClazzAssignment>{
                        withDescendant { withTag(testEntity.caUid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.clazz_assignment_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}