package com.ustadmobile.port.android.view

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import com.ustadmobile.port.android.screen.ClazzEnrollmentScreen
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzEnrollment
import com.ustadmobile.test.rules.SystemImplTestNavHostRule
import com.ustadmobile.test.rules.UmAppDatabaseAndroidClientRule
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

@AdbScreenRecord("ClazzEnrollment screen tests")
class ClazzEnrollmentFragmentTest : TestCase()  {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    var systemImplNavRule = SystemImplTestNavHostRule()

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()


    @AdbScreenRecord("Given list when ClazzEnrollment clicked then navigate to ClazzEnrollmentDetail")
    @Test
    fun givenClazzEnrollmentListPresent_whenClickOnClazzEnrollment_thenShouldNavigateToClazzEnrollmentDetail() {
        val testEntity = ClazzEnrollment().apply {
            clazzEnrollmentName = "Test Name"
            clazzEnrollmentUid = dbRule.db.clazzDao.insert(this)
        }

        val fragmentScenario = launchFragmentInContainer(themeResId = R.style.UmTheme_App,
                fragmentArgs = bundleOf()) {
            ClazzEnrollmentListFragment().also {
                it.installNavController(systemImplNavRule.navController)
            }
        }

        init{

        }.run{

            ClazzEnrollmentScreen{

                recycler{

                    childWith<ClazzEnrollmentScreen.ClazzEnrollment>{
                        withDescendant { withTag(testEntity.clazzEnrollmentUid) }
                    }perform {
                        title {
                            click()
                        }
                    }

                }

                flakySafely {
                    Assert.assertEquals("After clicking on item, it navigates to detail view",
                            R.id.clazz_enrollment_detail_dest, systemImplNavRule.navController.currentDestination?.id)
                }


            }

        }
    }

}