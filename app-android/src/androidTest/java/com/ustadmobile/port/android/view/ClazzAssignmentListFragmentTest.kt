package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ClazzAssignmentListScreen
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.test.port.android.util.installNavController
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("Assignment list screen tests")
class ClazzAssignmentListFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule()

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when Assignment clicked then navigate to AssignmentDetail")
    @Test
    fun givenAssignmentListPresent_whenClickOnAssignment_thenShouldNavigateToAssignmentDetail() {
        val testEntity = ClazzAssignment().apply {
            caTitle = "Test Name"
            caUid = dbRule.repo.clazzAssignmentDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ClazzAssignmentListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzAssignmentListScreen{

                recycler{

                    childWith<ClazzAssignmentListScreen.Assignment>{
                        withDescendant { withText(testEntity.caTitle!!) }
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
