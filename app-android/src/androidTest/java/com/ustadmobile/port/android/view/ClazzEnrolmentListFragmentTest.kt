package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ClazzEnrolmentScreen
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.port.android.screen.ClazzEnrolmentListScreen
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzEnrolment screen tests")
class ClazzEnrolmentListFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when ClazzEnrolment clicked then navigate to ClazzEnrolmentDetail")
    @Test
    fun givenClazzEnrolmentListPresent_whenClickOnClazzEnrolment_thenShouldNavigateToClazzEnrolmentDetail() {
        val testEntity = ClazzEnrolment().apply {
            clazzEnrolmentName = "Test Name"
            clazzEnrolmentUid = dbRule.db.clazzDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ClazzEnrolmentListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzEnrolmentListScreen{

                recycler{

                    childWith<ClazzEnrolmentListScreen.ClazzEnrolment>{
                        withDescendant { withTag(testEntity.clazzEnrolmentUid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.clazz_Enrolment_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}